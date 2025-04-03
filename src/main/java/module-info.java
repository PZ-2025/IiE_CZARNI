module com.example.projektsilownia {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.projektsilownia to javafx.fxml;
    exports com.example.projektsilownia;
}