package com.example.silowniaprojekt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Klasa obsługująca połączenie z bazą danych MySQL.
 * Dostarcza metody statyczne do uzyskiwania połączenia z bazą danych
 * wykorzystywaną przez aplikację siłowni.
 * Implementuje wzorzec Singleton dla połączenia z bazą danych.
 */
public class DatabaseConnection {
    /**
     * URL połączenia do bazy danych MySQL.
     * Wskazuje na lokalną bazę danych gym_system2 z ustawionym strefą czasową UTC.
     */
    private static final String URL = "jdbc:mysql://localhost:3306/gym_system2?serverTimezone=UTC";
    
    /**
     * Nazwa użytkownika bazy danych.
     * Domyślna wartość to "root", powinna być dostosowana do konfiguracji serwera.
     */
    private static final String USER = "root";             // Zmień na odpowiedniego użytkownika
    
    /**
     * Hasło do bazy danych.
     * Pozostawione puste dla lokalnego środowiska, powinno być ustawione dla produkcji.
     */
    private static final String PASSWORD = "";    // Zmień na rzeczywiste hasło

    /**
     * Metoda do pobrania połączenia z bazą danych.
     * 
     * Tworzy i zwraca nowe połączenie z bazą danych na podstawie 
     * skonfigurowanych parametrów URL, USER i PASSWORD. W przypadku 
     * błędu połączenia, wyświetla komunikat w konsoli i zwraca null.
     * 
     * @return Obiekt Connection reprezentujący połączenie z bazą danych
     *         lub null w przypadku błędu połączenia
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Połączenie z bazą danych zostało nawiązane.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas łączenia z bazą danych: " + e.getMessage());
        }
        return connection;
    }
    
    /**
     * Metoda testowa do sprawdzenia połączenia z bazą danych.
     * 
     * Próbuje nawiązać połączenie z bazą i zamknąć je, wyświetlając
     * odpowiednie komunikaty w konsoli. Służy do szybkiego sprawdzenia
     * poprawności konfiguracji bazy danych.
     * 
     * @param args Standardowe argumenty wiersza poleceń (nieużywane)
     */
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            // Połączenie zostało nawiązane – tutaj można wykonać zapytania
            try {
                conn.close();
                System.out.println("Połączenie zostało zamknięte.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
