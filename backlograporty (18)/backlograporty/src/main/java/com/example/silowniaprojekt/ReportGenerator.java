package com.example.silowniaprojekt;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Generator raportów dla systemu zarządzania siłownią.
 *
 * Klasa odpowiedzialna za generowanie różnych typów raportów w formacie PDF,
 * w tym raportów finansowych, produktowych, członkostwa i transakcji.
 * Wykorzystuje bibliotekę iText do tworzenia dokumentów PDF oraz
 * łączy się z bazą danych w celu pobrania aktualnych danych.
 *
 * Obsługiwane typy raportów:
 * - Raporty finansowe (przychody z produktów i karnetów)
 * - Raporty produktów (stan magazynowy i sprzedaż)
 * - Raporty karnetów (płatności członkowskie)
 * - Raporty transakcji (szczegółowe zestawienia sprzedaży)
 *
 * @author System zarządzania siłownią
 * @version 1.0
 * @since 1.0
 */
public class ReportGenerator {

    /** Kolor nagłówków w raportach - czerwony (Crimson) */
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(220, 20, 60);

    /** Kolor tła komórek tabeli - ciemny */
    private static final DeviceRgb BACKGROUND_COLOR = new DeviceRgb(26, 26, 26);

    /** Email użytkownika generującego raport */
    private static String userEmail;

    /**
     * Generuje raport finansowy w formacie PDF.
     *
     * Raport zawiera zestawienie przychodów z sprzedaży produktów i płatności za karnety
     * w określonym okresie czasowym. Dane są pobierane z bazy danych i prezentowane
     * w formie tabel z podsumowaniem finansowym.
     *
     * @param period okres czasowy raportu (np. "Ostatni miesiąc", "Bieżący rok" lub zakres dat "YYYY-MM-DD:YYYY-MM-DD")
     * @param outputFile plik docelowy do zapisu raportu PDF
     * @param userEmail adres email użytkownika generującego raport
     * @throws IOException gdy wystąpi błąd podczas tworzenia pliku PDF
     * @throws SQLException gdy wystąpi błąd podczas pobierania danych z bazy
     */
    public static void generateFinancialReport(String period, File outputFile, String userEmail) throws IOException, SQLException {
        PdfWriter writer = new PdfWriter(outputFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        try {
            // Nagłówek raportu z emailem
            addReportHeader(document, "Raport Finansowy", period, userEmail);

            // Pobieranie danych z bazy
            List<TransactionData> transactions = getTransactionsData(period);
            List<MembershipData> memberships = getMembershipsData(period);

            // Sekcja transakcji (sprzedaż produktów)
            addTransactionsSection(document, transactions);

            // Sekcja karnetów (płatności za członkostwo)
            addMembershipsSection(document, memberships);

            // Podsumowanie finansowe
            addFinancialSummary(document, transactions, memberships);

            // Stopka
            addFooter(document);

        } finally {
            document.close();
        }
    }

    /**
     * Generuje raport produktów w formacie PDF.
     *
     * Raport zawiera stan magazynowy produktów oraz statystyki sprzedaży w określonym okresie.
     * Może być filtrowany według konkretnego produktu lub pokazywać wszystkie produkty.
     *
     * @param period okres czasowy raportu
     * @param outputFile plik docelowy do zapisu raportu PDF
     * @param userEmail adres email użytkownika generującego raport
     * @param selectedProduct nazwa produktu do filtrowania (null lub "Wszystkie" dla wszystkich produktów)
     * @throws IOException gdy wystąpi błąd podczas tworzenia pliku PDF
     * @throws SQLException gdy wystąpi błąd podczas pobierania danych z bazy
     */
    public static void generateProductsReport(String period, File outputFile, String userEmail, String selectedProduct) throws IOException, SQLException {
        PdfWriter writer = new PdfWriter(outputFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        try {
            // Nagłówek raportu z emailem
            String title = selectedProduct != null && !selectedProduct.equals("Wszystkie") ?
                    "Raport Produktów - " + selectedProduct : "Raport Produktów";
            addReportHeader(document, title, period, userEmail);

            // Pobieranie danych z bazy
            List<ProductData> products = getProductsData();
            List<TransactionData> transactions = getTransactionsData(period, selectedProduct);

            // Sekcja aktualnych stanów magazynowych
            if (selectedProduct == null || selectedProduct.equals("Wszystkie")) {
                addProductsInventorySection(document, products);
            } else {
                // Filtruj produkty jeśli wybrano konkretny
                List<ProductData> filteredProducts = products.stream()
                        .filter(p -> p.name.equals(selectedProduct))
                        .collect(java.util.stream.Collectors.toList());
                addProductsInventorySection(document, filteredProducts);
            }

            // Sekcja sprzedaży produktów
            addProductsSalesSection(document, transactions);

            // Podsumowanie
            addProductsSummary(document, products, transactions);

            // Stopka
            addFooter(document);

        } finally {
            document.close();
        }
    }

    /**
     * Pobiera dane transakcji z bazy danych dla określonego okresu i produktu.
     *
     * @param period okres czasowy do filtrowania transakcji
     * @param selectedProduct nazwa produktu do filtrowania (null dla wszystkich)
     * @return lista danych transakcji spełniających kryteria
     * @throws SQLException gdy wystąpi błąd podczas wykonywania zapytania SQL
     */
    private static List<TransactionData> getTransactionsData(String period, String selectedProduct) throws SQLException {
        List<TransactionData> transactions = new ArrayList<>();
        LocalDate[] dateRange = getDateRangeForPeriod(period);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        String query = """
        SELECT t.id, u.name AS client_name, p.name AS product_name, 
               t.transaction_date, t.amount 
        FROM transactions t
        JOIN users u ON t.client_id = u.id
        JOIN products p ON t.product_id = p.id
        WHERE DATE(t.transaction_date) BETWEEN ? AND ?
        """ + (selectedProduct != null && !selectedProduct.equals("Wszystkie") ? " AND p.name = ?" : "") + """
        ORDER BY t.transaction_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());

            if (selectedProduct != null && !selectedProduct.equals("Wszystkie")) {
                stmt.setString(3, selectedProduct);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionData(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getString("product_name"),
                            rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            rs.getDouble("amount")
                    ));
                }
            }
        }

        return transactions;
    }

    /**
     * Pobiera listę nazw wszystkich produktów z bazy danych.
     *
     * Używane do wypełniania filtrów produktów w interfejsie użytkownika.
     * Lista zawiera opcję "Wszystkie" jako pierwszą pozycję.
     *
     * @return lista nazw produktów, z "Wszystkie" jako pierwszą opcją
     * @throws SQLException gdy wystąpi błąd podczas wykonywania zapytania SQL
     */
    public static List<String> getProductNames() throws SQLException {
        List<String> productNames = new ArrayList<>();
        productNames.add("Wszystkie"); // Opcja domyślna

        String query = "SELECT DISTINCT name FROM products ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productNames.add(rs.getString("name"));
            }
        }

        return productNames;
    }

    /**
     * Generuje raport karnetów w formacie PDF.
     *
     * Raport zawiera szczegółowe informacje o płatnościach za karnety
     * w określonym okresie wraz z podsumowaniem statystycznym.
     *
     * @param period okres czasowy raportu
     * @param outputFile plik docelowy do zapisu raportu PDF
     * @param userEmail adres email użytkownika generującego raport
     * @throws IOException gdy wystąpi błąd podczas tworzenia pliku PDF
     * @throws SQLException gdy wystąpi błąd podczas pobierania danych z bazy
     */
    public static void generateMembershipReport(String period, File outputFile, String userEmail) throws IOException, SQLException {
        PdfWriter writer = new PdfWriter(outputFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        try {
            // Nagłówek raportu z emailem
            addReportHeader(document, "Raport Karnetów", period, userEmail);

            // Pobieranie danych z bazy
            List<MembershipData> memberships = getMembershipsData(period);

            // Sekcja płatności członkowskich
            addDetailedMembershipsSection(document, memberships);

            // Podsumowanie
            addMembershipSummary(document, memberships);

            // Stopka
            addFooter(document);

        } finally {
            document.close();
        }
    }

    /**
     * Generuje raport transakcji w formacie PDF.
     *
     * Raport zawiera szczegółową listę wszystkich transakcji w określonym okresie
     * wraz z analizą statystyczną i podsumowaniem.
     *
     * @param period okres czasowy raportu
     * @param outputFile plik docelowy do zapisu raportu PDF
     * @param userEmail adres email użytkownika generującego raport
     * @throws IOException gdy wystąpi błąd podczas tworzenia pliku PDF
     * @throws SQLException gdy wystąpi błąd podczas pobierania danych z bazy
     */
    public static void generateTransactionsReport(String period, File outputFile, String userEmail) throws IOException, SQLException {
        PdfWriter writer = new PdfWriter(outputFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        try {
            // Nagłówek raportu z emailem
            addReportHeader(document, "Raport Transakcji", period, userEmail);

            // Pobieranie danych z bazy
            List<TransactionData> transactions = getTransactionsData(period);

            // Sekcja szczegółowa wszystkich transakcji
            addDetailedTransactionsSection(document, transactions);

            // Podsumowanie
            addTransactionsSummary(document, transactions);

            // Stopka
            addFooter(document);

        } finally {
            document.close();
        }
    }

    /**
     * Tworzy czcionkę PDF z obsługą polskich znaków.
     *
     * Próbuje różne kodowania w kolejności priorytetów, aby zapewnić
     * prawidłowe wyświetlanie polskich znaków diakrytycznych.
     *
     * @return obiekt PdfFont z obsługą polskich znaków
     * @throws IOException gdy nie można załadować żadnej czcionki
     */
    private static PdfFont createFont() throws IOException {
        try {
            // Próbuj użyć wbudowanej czcionki z pełnym wsparciem dla polskich znaków
            return PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.CP1250, PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
        } catch (Exception e) {
            try {
                // Alternatywnie spróbuj z UTF-8
                return PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
            } catch (Exception e2) {
                try {
                    // Trzecia opcja - Windows-1250
                    return PdfFontFactory.createFont(StandardFonts.HELVETICA, "windows-1250", PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
                } catch (Exception e3) {
                    // Fallback do podstawowej czcionki
                    System.err.println("Nie można załadować czcionki z obsługą polskich znaków, używam podstawowej");
                    return PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
            }
        }
    }

    /**
     * Dodaje nagłówek raportu do dokumentu PDF.
     *
     * Nagłówek zawiera informacje o siłowni, tytuł raportu, email użytkownika,
     * okres raportu oraz datę wygenerowania.
     *
     * @param document dokument PDF do modyfikacji
     * @param title tytuł raportu
     * @param period okres czasowy raportu
     * @param userEmail email użytkownika generującego raport
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addReportHeader(Document document, String title, String period, String userEmail) throws IOException {
        PdfFont font = createFont();

        // Logo i informacje o siłowni
        Paragraph header = new Paragraph("SIŁOWNIA FITNESS CENTRUM")
                .setFont(font)
                .setFontSize(20)
                .setFontColor(HEADER_COLOR)
                .setBold();
        document.add(header);

        // Adres i dane kontaktowe
        Paragraph address = new Paragraph("ul. Sportowa 123, 35-340 Rzeszów\nTel: 17 123 45 67\nNIP: 123-456-78-90")
                .setFont(font)
                .setFontSize(10)
                .setMarginBottom(20);
        document.add(address);

        // Tytuł raportu
        Paragraph reportTitle = new Paragraph(title)
                .setFont(font)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(reportTitle);

        // Email użytkownika
        if (userEmail != null && !userEmail.isEmpty()) {
            Paragraph emailParagraph = new Paragraph("Wygenerowany dla: " + userEmail)
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(emailParagraph);
        }

        // Okres raportu
        String periodText = "Okres: " + getPeriodDescription(period);
        Paragraph periodParagraph = new Paragraph(periodText)
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(periodParagraph);

        // Data wygenerowania raportu
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        Paragraph dateParagraph = new Paragraph("Data wygenerowania: " + currentDate)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(30);
        document.add(dateParagraph);
    }

    /**
     * Dodaje sekcję transakcji (sprzedaż produktów) do dokumentu.
     *
     * Tworzy tabelę z listą transakcji zawierającą ID, datę, klienta, produkt i kwotę.
     *
     * @param document dokument PDF do modyfikacji
     * @param transactions lista danych transakcji do wyświetlenia
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addTransactionsSection(Document document, List<TransactionData> transactions) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Sprzedaż Produktów")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela transakcji
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 30, 25, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Data", font));
        table.addHeaderCell(createHeaderCell("Klient", font));
        table.addHeaderCell(createHeaderCell("Produkt", font));
        table.addHeaderCell(createHeaderCell("Kwota (PLN)", font));

        // Dodawanie danych
        for (TransactionData transaction : transactions) {
            table.addCell(createCell(String.valueOf(transaction.id), font));
            table.addCell(createCell(transaction.date, font));
            table.addCell(createCell(transaction.clientName, font));
            table.addCell(createCell(transaction.productName, font));
            table.addCell(createCell(String.format("%.2f", transaction.amount), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje sekcję płatności za karnety do dokumentu.
     *
     * Tworzy tabelę z listą płatności zawierającą ID, datę, klienta i kwotę.
     *
     * @param document dokument PDF do modyfikacji
     * @param memberships lista danych o płatnościach za karnety
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addMembershipsSection(Document document, List<MembershipData> memberships) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Płatności za Karnety")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela płatności za karnety
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 55, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Data", font));
        table.addHeaderCell(createHeaderCell("Klient", font));
        table.addHeaderCell(createHeaderCell("Kwota (PLN)", font));

        // Dodawanie danych
        for (MembershipData membership : memberships) {
            table.addCell(createCell(String.valueOf(membership.id), font));
            table.addCell(createCell(membership.date, font));
            table.addCell(createCell(membership.clientName, font));
            table.addCell(createCell(String.format("%.2f", membership.amount), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje szczegółową sekcję płatności za karnety do dokumentu.
     *
     * Podobna do addMembershipsSection, ale z dodatkowym opisem sekcji.
     *
     * @param document dokument PDF do modyfikacji
     * @param memberships lista danych o płatnościach za karnety
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addDetailedMembershipsSection(Document document, List<MembershipData> memberships) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Szczegółowe informacje o płatnościach za karnety")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela płatności za karnety
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 55, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Data", font));
        table.addHeaderCell(createHeaderCell("Klient", font));
        table.addHeaderCell(createHeaderCell("Kwota (PLN)", font));

        // Dodawanie danych
        for (MembershipData membership : memberships) {
            table.addCell(createCell(String.valueOf(membership.id), font));
            table.addCell(createCell(membership.date, font));
            table.addCell(createCell(membership.clientName, font));
            table.addCell(createCell(String.format("%.2f", membership.amount), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje sekcję stanu magazynowego produktów do dokumentu.
     *
     * Tworzy tabelę z listą produktów zawierającą ID, nazwę, ilość i cenę.
     *
     * @param document dokument PDF do modyfikacji
     * @param products lista danych produktów do wyświetlenia
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addProductsInventorySection(Document document, List<ProductData> products) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Stan Magazynowy Produktów")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela produktów
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 60, 15, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Nazwa Produktu", font));
        table.addHeaderCell(createHeaderCell("Ilość", font));
        table.addHeaderCell(createHeaderCell("Cena (PLN)", font));

        // Dodawanie danych
        for (ProductData product : products) {
            table.addCell(createCell(String.valueOf(product.id), font));
            table.addCell(createCell(product.name, font));
            table.addCell(createCell(String.valueOf(product.stock), font).setTextAlignment(TextAlignment.CENTER));
            table.addCell(createCell(String.format("%.2f", product.price), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje sekcję sprzedaży produktów z agregacją danych.
     *
     * Grupuje transakcje według produktów i tworzy podsumowanie sprzedaży
     * z ilością sprzedaną, łączną kwotą i średnią ceną.
     *
     * @param document dokument PDF do modyfikacji
     * @param transactions lista transakcji do analizy
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addProductsSalesSection(Document document, List<TransactionData> transactions) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Sprzedaż Produktów w Wybranym Okresie")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Agregacja danych - grupowanie po produktach
        Map<String, Integer> productCounts = new HashMap<>();
        Map<String, Double> productTotals = new HashMap<>();

        for (TransactionData transaction : transactions) {
            productCounts.merge(transaction.productName, 1, Integer::sum);
            productTotals.merge(transaction.productName, transaction.amount, Double::sum);
        }

        // Tabela sprzedaży z ilościami
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("Produkt", font));
        table.addHeaderCell(createHeaderCell("Ilość sprzedana", font));
        table.addHeaderCell(createHeaderCell("Łączna kwota (PLN)", font));
        table.addHeaderCell(createHeaderCell("Średnia cena", font));

        // Dodawanie danych zagregowanych
        for (Map.Entry<String, Integer> entry : productCounts.entrySet()) {
            String productName = entry.getKey();
            Integer count = entry.getValue();
            Double total = productTotals.get(productName);
            Double average = total / count;

            table.addCell(createCell(productName, font));
            table.addCell(createCell(count.toString(), font).setTextAlignment(TextAlignment.CENTER));
            table.addCell(createCell(String.format("%.2f", total), font).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(createCell(String.format("%.2f", average), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje szczegółową sekcję wszystkich transakcji do dokumentu.
     *
     * Tworzy pełną listę transakcji bez agregacji, pokazując każdą transakcję osobno.
     *
     * @param document dokument PDF do modyfikacji
     * @param transactions lista transakcji do wyświetlenia
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addDetailedTransactionsSection(Document document, List<TransactionData> transactions) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Szczegółowe informacje o transakcjach")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela transakcji
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 30, 25, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Data", font));
        table.addHeaderCell(createHeaderCell("Klient", font));
        table.addHeaderCell(createHeaderCell("Produkt", font));
        table.addHeaderCell(createHeaderCell("Kwota (PLN)", font));

        // Dodawanie danych
        for (TransactionData transaction : transactions) {
            table.addCell(createCell(String.valueOf(transaction.id), font));
            table.addCell(createCell(transaction.date, font));
            table.addCell(createCell(transaction.clientName, font));
            table.addCell(createCell(transaction.productName, font));
            table.addCell(createCell(String.format("%.2f", transaction.amount), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje podsumowanie finansowe do dokumentu.
     *
     * Oblicza i wyświetla łączne przychody z sprzedaży produktów i karnetów
     * oraz sumę całkowitą wszystkich przychodów.
     *
     * @param document dokument PDF do modyfikacji
     * @param transactions lista transakcji do analizy
     * @param memberships lista płatności za karnety do analizy
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addFinancialSummary(Document document, List<TransactionData> transactions, List<MembershipData> memberships) throws IOException {
        PdfFont font = createFont();

        // Obliczanie sum
        double transactionsTotal = transactions.stream().mapToDouble(t -> t.amount).sum();
        double membershipsTotal = memberships.stream().mapToDouble(m -> m.amount).sum();
        double total = transactionsTotal + membershipsTotal;

        // Tabela podsumowania
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(null);

        // Wiersz - suma ze sprzedaży produktów
        table.addCell(createSummaryLabelCell("Przychody ze sprzedaży produktów:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", transactionsTotal), font));

        // Wiersz - suma z płatności za karnety
        table.addCell(createSummaryLabelCell("Przychody z karnetów:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", membershipsTotal), font));

        // Wiersz - suma całkowita
        table.addCell(createSummaryLabelCell("SUMA PRZYCHODÓW:", font).setBold());
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", total), font).setBold());

        Div summaryDiv = new Div()
                .setMarginTop(30)
                .add(new Paragraph("PODSUMOWANIE").setFont(font).setFontSize(14).setBold().setMarginBottom(10))
                .add(table);

        document.add(summaryDiv);
    }

    /**
     * Dodaje podsumowanie produktów do dokumentu.
     *
     * Oblicza i wyświetla wartość magazynu, sprzedaż w okresie,
     * liczbę sprzedanych produktów i liczbę różnych produktów.
     *
     * @param document dokument PDF do modyfikacji
     * @param products lista produktów do analizy
     * @param transactions lista transakcji do analizy
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addProductsSummary(Document document, List<ProductData> products, List<TransactionData> transactions) throws IOException {
        PdfFont font = createFont();

        // Obliczanie wartości magazynu
        double inventoryValue = products.stream().mapToDouble(p -> p.price * p.stock).sum();

        // Obliczanie sprzedaży całkowitej
        double salesTotal = transactions.stream().mapToDouble(t -> t.amount).sum();

        // Obliczanie ilości sprzedanych produktów
        int totalProductsSold = transactions.size();

        // Obliczanie ilości różnych produktów sprzedanych
        long uniqueProductsSold = transactions.stream()
                .map(t -> t.productName)
                .distinct()
                .count();

        // Tabela podsumowania
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(null);

        // Wiersz - wartość magazynu
        table.addCell(createSummaryLabelCell("Wartość stanu magazynowego:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", inventoryValue), font));

        // Wiersz - sprzedaż w okresie
        table.addCell(createSummaryLabelCell("Sprzedaż w wybranym okresie:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", salesTotal), font));

        // Wiersz - liczba sprzedanych produktów
        table.addCell(createSummaryLabelCell("Łączna ilość sprzedanych produktów:", font));
        table.addCell(createSummaryValueCell(String.valueOf(totalProductsSold), font));

        // Wiersz - liczba różnych produktów sprzedanych
        table.addCell(createSummaryLabelCell("Liczba różnych produktów sprzedanych:", font));
        table.addCell(createSummaryValueCell(String.valueOf(uniqueProductsSold), font));

        Div summaryDiv = new Div()
                .setMarginTop(30)
                .add(new Paragraph("PODSUMOWANIE").setFont(font).setFontSize(14).setBold().setMarginBottom(10))
                .add(table);

        document.add(summaryDiv);
    }

    /**
     * Dodaje podsumowanie karnetów do dokumentu.
     *
     * Oblicza i wyświetla liczbę klientów z karnetami oraz łączne przychody z karnetów.
     *
     * @param document dokument PDF do modyfikacji
     * @param memberships lista płatności za karnety do analizy
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addMembershipSummary(Document document, List<MembershipData> memberships) throws IOException {
        PdfFont font = createFont();

        // Obliczanie sum
        double membershipsTotal = memberships.stream().mapToDouble(m -> m.amount).sum();
        int clientCount = (int) memberships.stream().map(m -> m.clientName).distinct().count();

        // Tabela podsumowania
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(null);

        // Wiersz - liczba klientów z karnetami
        table.addCell(createSummaryLabelCell("Liczba klientów z karnetami:", font));
        table.addCell(createSummaryValueCell(String.valueOf(clientCount), font));

        // Wiersz - suma z płatności za karnety
        table.addCell(createSummaryLabelCell("Przychody z karnetów:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", membershipsTotal), font));

        Div summaryDiv = new Div()
                .setMarginTop(30)
                .add(new Paragraph("PODSUMOWANIE").setFont(font).setFontSize(14).setBold().setMarginBottom(10))
                .add(table);

        document.add(summaryDiv);
    }

    /**
     * Dodaje podsumowanie transakcji do dokumentu.
     *
     * Oblicza i wyświetla liczbę transakcji, klientów, różnych produktów
     * oraz łączną sumę wszystkich transakcji.
     *
     * @param document dokument PDF do modyfikacji
     * @param transactions lista transakcji do analizy
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addTransactionsSummary(Document document, List<TransactionData> transactions) throws IOException {
        PdfFont font = createFont();

        // Obliczanie sum
        double transactionsTotal = transactions.stream().mapToDouble(t -> t.amount).sum();
        int clientCount = (int) transactions.stream().map(t -> t.clientName).distinct().count();
        int productCount = (int) transactions.stream().map(t -> t.productName).distinct().count();

        // Tabela podsumowania
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(null);

        // Wiersz - liczba transakcji
        table.addCell(createSummaryLabelCell("Liczba transakcji:", font));
        table.addCell(createSummaryValueCell(String.valueOf(transactions.size()), font));

        // Wiersz - liczba klientów
        table.addCell(createSummaryLabelCell("Liczba klientów:", font));
        table.addCell(createSummaryValueCell(String.valueOf(clientCount), font));

        // Wiersz - liczba sprzedanych produktów
        table.addCell(createSummaryLabelCell("Liczba różnych produktów:", font));
        table.addCell(createSummaryValueCell(String.valueOf(productCount), font));

        // Wiersz - suma transakcji
        table.addCell(createSummaryLabelCell("Suma transakcji:", font).setBold());
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", transactionsTotal), font).setBold());

        Div summaryDiv = new Div()
                .setMarginTop(30)
                .add(new Paragraph("PODSUMOWANIE").setFont(font).setFontSize(14).setBold().setMarginBottom(10))
                .add(table);

        document.add(summaryDiv);
    }

    /**
     * Dodaje stopkę do dokumentu PDF.
     *
     * Stopka zawiera nazwę systemu zarządzania siłownią.
     *
     * @param document dokument PDF do modyfikacji
     * @throws IOException gdy wystąpi błąd podczas tworzenia czcionki
     */
    private static void addFooter(Document document) throws IOException {
        PdfFont font = createFont();

        Paragraph footer = new Paragraph("Siłownia Fitness Centrum - System Zarządzania")
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);
        document.add(footer);
    }

    /**
     * Tworzy komórkę nagłówka tabeli o określonym stylu.
     *
     * @param text tekst do wyświetlenia w komórce
     * @param font czcionka do użycia
     * @return sformatowana komórka nagłówka
     */
    private static Cell createHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setBold())
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
    }

    /**
     * Tworzy standardową komórkę tabeli o określonym stylu.
     *
     * @param text tekst do wyświetlenia w komórce
     * @param font czcionka do użycia
     * @return sformatowana komórka danych
     */
    private static Cell createCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font))
                .setBackgroundColor(BACKGROUND_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(5);
    }

    /**
     * Tworzy komórkę etykiety w tabeli podsumowania.
     *
     * @param text tekst etykiety
     * @param font czcionka do użycia
     * @return sformatowana komórka etykiety
     */
    private static Cell createSummaryLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font))
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    /**
     * Tworzy komórkę wartości w tabeli podsumowania.
     *
     * @param text tekst wartości
     * @param font czcionka do użycia
     * @return sformatowana komórka wartości wyrównana do prawej
     */
    private static Cell createSummaryValueCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5);
    }

    /**
     * Pobiera dane transakcji z bazy danych dla określonego okresu.
     *
     * @param period okres czasowy do filtrowania
     * @return lista danych transakcji
     * @throws SQLException gdy wystąpi błąd podczas wykonywania zapytania SQL
     */
    private static List<TransactionData> getTransactionsData(String period) throws SQLException {
        List<TransactionData> transactions = new ArrayList<>();
        LocalDate[] dateRange = getDateRangeForPeriod(period);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        String query = """
            SELECT t.id, u.name AS client_name, p.name AS product_name, 
                   t.transaction_date, t.amount 
            FROM transactions t
            JOIN users u ON t.client_id = u.id
            JOIN products p ON t.product_id = p.id
            WHERE DATE(t.transaction_date) BETWEEN ? AND ?
            ORDER BY t.transaction_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionData(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getString("product_name"),
                            rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            rs.getDouble("amount")
                    ));
                }
            }
        }

        return transactions;
    }

    /**
     * Pobiera dane płatności za karnety z bazy danych dla określonego okresu.
     *
     * @param period okres czasowy do filtrowania
     * @return lista danych o płatnościach za karnety
     * @throws SQLException gdy wystąpi błąd podczas wykonywania zapytania SQL
     */
    private static List<MembershipData> getMembershipsData(String period) throws SQLException {
        List<MembershipData> memberships = new ArrayList<>();
        LocalDate[] dateRange = getDateRangeForPeriod(period);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        String query = """
            SELECT mp.id, u.name AS client_name, mp.amount, mp.payment_date
            FROM membership_payments mp
            JOIN users u ON mp.client_id = u.id
            WHERE DATE(mp.payment_date) BETWEEN ? AND ?
            ORDER BY mp.payment_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    memberships.add(new MembershipData(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("payment_date").toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    ));
                }
            }
        }

        return memberships;
    }

    /**
     * Pobiera dane wszystkich produktów z bazy danych.
     *
     * @return lista danych produktów
     * @throws SQLException gdy wystąpi błąd podczas wykonywania zapytania SQL
     */
    private static List<ProductData> getProductsData() throws SQLException {
        List<ProductData> products = new ArrayList<>();

        String query = "SELECT id, name, price, stock FROM products ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new ProductData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        }

        return products;
    }

    /**
     * Konwertuje string okresu na zakres dat.
     *
     * Obsługuje predefiniowane okresy (np. "Ostatni miesiąc") oraz
     * niestandardowe zakresy dat w formacie "YYYY-MM-DD:YYYY-MM-DD".
     *
     * @param period opis okresu czasowego
     * @return tablica dwóch dat [data_początkowa, data_końcowa]
     */
    private static LocalDate[] getDateRangeForPeriod(String period) {
        // Sprawdź czy period zawiera konkretne daty (format: "YYYY-MM-DD:YYYY-MM-DD")
        if (period.contains(":")) {
            String[] dates = period.split(":");
            try {
                LocalDate startDate = LocalDate.parse(dates[0]);
                LocalDate endDate = LocalDate.parse(dates[1]);
                return new LocalDate[] { startDate, endDate };
            } catch (Exception e) {
                // Fallback do domyślnego zachowania
            }
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "Ostatni tydzień":
                startDate = endDate.minusWeeks(1);
                break;
            case "Ostatni miesiąc":
                startDate = endDate.minusMonths(1);
                break;
            case "Ostatni kwartał":
                startDate = endDate.minusMonths(3);
                break;
            case "Bieżący rok":
                startDate = LocalDate.of(endDate.getYear(), 1, 1);
                break;
            case "Wszystkie":
                startDate = LocalDate.of(2000, 1, 1);
                break;
            case "Bieżący stan":
                startDate = endDate;
                break;
            default:
                startDate = endDate.minusMonths(1);
        }

        return new LocalDate[] { startDate, endDate };
    }

    /**
     * Konwertuje string okresu na czytelny opis dla użytkownika.
     *
     * @param period opis okresu czasowego
     * @return sformatowany opis okresu do wyświetlenia w raporcie
     */
    private static String getPeriodDescription(String period) {
        // Sprawdź czy period zawiera konkretne daty (format: "YYYY-MM-DD:YYYY-MM-DD")
        if (period.contains(":")) {
            String[] dates = period.split(":");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            try {
                LocalDate startDate = LocalDate.parse(dates[0]);
                LocalDate endDate = LocalDate.parse(dates[1]);
                return "Od " + startDate.format(formatter) + " do " + endDate.format(formatter);
            } catch (Exception e) {
                return period;
            }
        }

        LocalDate[] dateRange = getDateRangeForPeriod(period);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        if (period.equals("Bieżący stan")) {
            return "Stan na dzień " + endDate.format(formatter);
        } else {
            return "Od " + startDate.format(formatter) + " do " + endDate.format(formatter);
        }
    }

    /**
     * Klasa reprezentująca dane o transakcjach w raportach.
     * Przechowuje zagregowane informacje o transakcjach, takie jak
     * łączna kwota transakcji, liczba transakcji, średnia wartość
     * transakcji i rozkład transakcji w czasie.
     */
    public static class TransactionData {
        /**
         * Unikalny identyfikator transakcji.
         */
        public final int id;

        /**
         * Imię i nazwisko klienta, który dokonał transakcji.
         */
        public final String clientName;

        /**
         * Nazwa produktu, którego dotyczy transakcja.
         */
        public final String productName;

        /**
         * Data wykonania transakcji w formacie tekstowym.
         */
        public final String date;

        /**
         * Kwota transakcji wyrażona w złotych.
         */
        public final double amount;

        /**
         * Tworzy nowy obiekt danych transakcji z podanymi parametrami.
         *
         * @param id Unikalny identyfikator transakcji
         * @param clientName Imię i nazwisko klienta
         * @param productName Nazwa zakupionego produktu
         * @param date Data transakcji
         * @param amount Kwota transakcji
         */
        public TransactionData(int id, String clientName, String productName, String date, double amount) {
            this.id = id;
            this.clientName = clientName;
            this.productName = productName;
            this.date = date;
            this.amount = amount;
        }
    }

    /**
     * Klasa reprezentująca dane o członkostwach w raportach.
     * Przechowuje zagregowane informacje o karnetach, takie jak
     * liczba aktywnych karnetów, typy karnetów, przychody z karnetów
     * i statystyki odnowień.
     */
    public static class MembershipData {
        /**
         * Unikalny identyfikator wpłaty za karnet.
         */
        public final int id;

        /**
         * Imię i nazwisko klienta, którego dotyczy karnet.
         */
        public final String clientName;

        /**
         * Kwota wpłaty za karnet wyrażona w złotych.
         */
        public final double amount;

        /**
         * Data wpłaty za karnet w formacie tekstowym.
         */
        public final String date;

        /**
         * Tworzy nowy obiekt danych członkostwa z podanymi parametrami.
         *
         * @param id Unikalny identyfikator wpłaty
         * @param clientName Imię i nazwisko klienta
         * @param amount Kwota wpłaty
         * @param date Data wpłaty
         */
        public MembershipData(int id, String clientName, double amount, String date) {
            this.id = id;
            this.clientName = clientName;
            this.amount = amount;
            this.date = date;
        }
    }

    /**
     * Klasa reprezentująca dane o produktach w raportach.
     * Przechowuje zagregowane informacje o sprzedaży produktów, takie jak
     * liczba sprzedanych produktów, przychody ze sprzedaży, najpopularniejsze
     * produkty i trendy sprzedażowe.
     */
    public static class ProductData {
        /**
         * Unikalny identyfikator produktu.
         */
        public final int id;

        /**
         * Nazwa produktu.
         */
        public final String name;

        /**
         * Cena jednostkowa produktu wyrażona w złotych.
         */
        public final double price;

        /**
         * Ilość produktu dostępna w magazynie.
         */
        public final int stock;

        /**
         * Tworzy nowy obiekt danych produktu z podanymi parametrami.
         *
         * @param id Unikalny identyfikator produktu
         * @param name Nazwa produktu
         * @param price Cena jednostkowa produktu
         * @param stock Ilość w magazynie
         */
        public ProductData(int id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }
    }
}