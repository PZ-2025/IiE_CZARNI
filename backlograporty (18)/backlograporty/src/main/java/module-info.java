module com.example.silowniaprojekt {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;
    requires kernel;
    requires layout;
    requires java.desktop;
    requires io;

    // Dodanie wymaganych modułów JUnit dla testów
    requires org.junit.jupiter.api;
    requires org.apiguardian.api;
    requires org.opentest4j;
    requires org.junit.platform.commons;

    opens com.example.silowniaprojekt to javafx.fxml;
    exports com.example.silowniaprojekt;
}