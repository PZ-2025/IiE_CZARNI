package com.example.projektsilownia;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

public class EmployeeView {
    private Scene scene;
    private TabPane tabPane;
    private Button[] menuButtons;

    private TableView<Client> clientsTable;
    private TableView<Equipment> equipmentTable;
    private TableView<Membership> membershipsTable;
    private TableView<ScheduleItem> scheduleTable;

    private Button addClientButton;
    private Button addEquipmentButton;
    private Button addMembershipButton;
    private Button addScheduleButton;
    public EmployeeView() {
        createMainView();
    }

    private void createMainView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a0a;");

        // Menu boczne - teraz z ikonami i lepszym stylem
        VBox sideMenu = createSideMenu();
        root.setLeft(sideMenu);

        // Panel zakładek
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #1a1a1a;");

        // Dodaj zakładki z pełną zawartością
        tabPane.getTabs().addAll(
                createDashboardTab(),
                createClientsTab(),
                createEquipmentTab(),
                createPricingTab(),
                createScheduleTab()
        );

        root.setCenter(tabPane);
        this.scene = new Scene(root, 1200, 800);
    }
    private void applyStyles() {
        scene.getStylesheets().add(getClass().getResource("/styles1.css").toExternalForm());
    }

    private VBox createSideMenu() {
        VBox sideMenu = new VBox(10);
        sideMenu.setStyle("-fx-background-color: #111; -fx-padding: 20;");
        sideMenu.setPrefWidth(200);

        Label header = new Label("Panel pracownika");
        header.setStyle("-fx-text-fill: #dc143c; -fx-font-size: 18; -fx-font-weight: bold;");
        header.setPadding(new Insets(0, 0, 20, 0));

        // Przyciski menu z ikonami
        menuButtons = new Button[] {
                createMenuButton("Pulpit", "🏠"),
                createMenuButton("Klienci", "👥"),
                createMenuButton("Sprzęt", "🏋️"),
                createMenuButton("Cennik", "💰"),
                createMenuButton("Harmonogram", "📅")
        };

        sideMenu.getChildren().addAll(header);
        sideMenu.getChildren().addAll(menuButtons);
        return sideMenu;
    }

    private Button createMenuButton(String text, String icon) {
        Button button = new Button(icon + "  " + text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14;");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.BASELINE_LEFT);
        button.setContentDisplay(ContentDisplay.LEFT);
        return button;
    }

    private Tab createDashboardTab() {
        Tab tab = new Tab("Pulpit");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));

        // Karty statystyk
        grid.add(createDashboardCard("Aktywni klienci", "248", "↑ 12% (m/m)"), 0, 0);
        grid.add(createDashboardCard("Obecni na siłowni", "47", "↑ 3% (wczoraj)"), 1, 0);
        grid.add(createDashboardCard("Dzienna sprzedaż", "2,450 zł", "↓ 5% (wczoraj)"), 2, 0);
        grid.add(createDashboardCard("Dziś zaplanowano", "18 zajęć", "3 wolne miejsca"), 3, 0);

        tab.setContent(grid);
        return tab;
    }

    private VBox createDashboardCard(String title, String value, String change) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 15; -fx-border-color: #dc143c;");
        card.setPrefWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #dc143c; -fx-font-size: 24; -fx-font-weight: bold;");

        Label changeLabel = new Label(change);
        changeLabel.setStyle(change.contains("↑") ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #ff4444;");

        card.getChildren().addAll(titleLabel, valueLabel, changeLabel);
        return card;
    }

    private Tab createClientsTab() {
        Tab tab = new Tab("Klienci");

        clientsTable = new TableView<>();
        clientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Client, String> nameCol = new TableColumn<>("Imię i nazwisko");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Client, String> birthDateCol = new TableColumn<>("Data urodzenia");
        birthDateCol.setCellValueFactory(new PropertyValueFactory<>("birthDate"));

        TableColumn<Client, String> membershipCol = new TableColumn<>("Typ karnetu");
        membershipCol.setCellValueFactory(new PropertyValueFactory<>("membershipType"));

        clientsTable.getColumns().addAll(nameCol, birthDateCol, membershipCol);

        addClientButton = new Button("+ Dodaj klienta");
        addClientButton.setStyle("-fx-background-color: #dc143c; -fx-text-fill: white;");

        VBox content = new VBox(clientsTable);
        content.getStyleClass().add("tab-content");
        tab.setContent(content);
        return tab;
    }

    private Tab createEquipmentTab() {
        Tab tab = new Tab("Sprzęt");

        equipmentTable = new TableView<>();
        equipmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Equipment, String> nameCol = new TableColumn<>("Nazwa");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Equipment, Integer> quantityCol = new TableColumn<>("Ilość");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Equipment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        equipmentTable.getColumns().addAll(nameCol, quantityCol, statusCol);

        addEquipmentButton = new Button("+ Dodaj sprzęt");
        addEquipmentButton.setStyle("-fx-background-color: #dc143c; -fx-text-fill: white;");

        VBox content = new VBox(10, equipmentTable, addEquipmentButton);
        content.setPadding(new Insets(15));
        tab.setContent(content);
        return tab;
    }

    private Tab createPricingTab() {
        Tab tab = new Tab("Cennik");

        membershipsTable = new TableView<>();
        membershipsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Membership, String> nameCol = new TableColumn<>("Nazwa");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Membership, Double> priceCol = new TableColumn<>("Cena");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Membership, Boolean> activeCol = new TableColumn<>("Aktywny");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        membershipsTable.getColumns().addAll(nameCol, priceCol, activeCol);

        addMembershipButton = new Button("+ Dodaj karnet");
        addMembershipButton.setStyle("-fx-background-color: #dc143c; -fx-text-fill: white;");

        VBox content = new VBox(10, membershipsTable, addMembershipButton);
        content.setPadding(new Insets(15));
        tab.setContent(content);
        return tab;
    }

    private Tab createScheduleTab() {
        Tab tab = new Tab("Harmonogram");

        scheduleTable = new TableView<>();
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ScheduleItem, String> timeCol = new TableColumn<>("Godzina");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));

        TableColumn<ScheduleItem, String> dayCol = new TableColumn<>("Dzień");
        dayCol.setCellValueFactory(new PropertyValueFactory<>("day"));

        TableColumn<ScheduleItem, String> typeCol = new TableColumn<>("Zajęcia");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<ScheduleItem, String> trainerCol = new TableColumn<>("Trener");
        trainerCol.setCellValueFactory(new PropertyValueFactory<>("trainer"));

        scheduleTable.getColumns().addAll(timeCol, dayCol, typeCol, trainerCol);

        addScheduleButton = new Button("+ Dodaj zajęcia");
        addScheduleButton.setStyle("-fx-background-color: #dc143c; -fx-text-fill: white;");

        VBox content = new VBox(10, scheduleTable, addScheduleButton);
        content.setPadding(new Insets(15));
        tab.setContent(content);
        return tab;
    }

    // Gettery dla kontrolera
    public TableView<Client> getClientsTable() { return clientsTable; }
    public TableView<Equipment> getEquipmentTable() { return equipmentTable; }
    public TableView<Membership> getMembershipsTable() { return membershipsTable; }
    public TableView<ScheduleItem> getScheduleTable() { return scheduleTable; }

    public Button getAddClientButton() { return addClientButton; }
    public Button getAddEquipmentButton() { return addEquipmentButton; }
    public Button getAddMembershipButton() { return addMembershipButton; }
    public Button getAddScheduleButton() { return addScheduleButton; }

    public Scene getScene() { return scene; }
    public Button[] getMenuButtons() { return menuButtons; }
    public TabPane getTabPane() { return tabPane; }
}