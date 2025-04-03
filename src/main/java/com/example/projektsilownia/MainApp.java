package com.example.projektsilownia;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginView();
    }

    public void showLoginView() {
        LoginView loginView = new LoginView();
        new LoginController(loginView, this);

        primaryStage.setTitle("Black Iron Gym - Login");
        primaryStage.setScene(loginView.getScene());
        primaryStage.show();
    }

    public void showEmployeeView() {
        EmployeeModel model = new EmployeeModel();
        EmployeeView employeeView = new EmployeeView();
        new EmployeeController(model, employeeView);

        primaryStage.setTitle("Black Iron Gym - Panel pracownika");
        primaryStage.setScene(employeeView.getScene());
        primaryStage.setMaximized(true); // Pełny ekran
    }

    public static void main(String[] args) {
        launch(args);
    }
}