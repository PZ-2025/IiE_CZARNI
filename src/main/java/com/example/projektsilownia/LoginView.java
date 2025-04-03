package com.example.projektsilownia;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;


public class LoginView {
    private Scene scene;
    private TextField usernameField;
    private PasswordField passwordField;
    private CheckBox rememberMe;
    private Hyperlink forgotPassword;
    private Button loginButton;
    private Hyperlink registerLink;
    private Hyperlink instagramLink;
    private Hyperlink facebookLink;
    private Hyperlink appleLink;

    public LoginView() {
        StackPane root = new StackPane();

        ImageView background = new ImageView(new Image("https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e"));
        background.setFitWidth(800);
        background.setFitHeight(600);
        background.setPreserveRatio(false);

        Rectangle overlay = new Rectangle(800, 600);
        overlay.setFill(Color.rgb(0, 0, 0, 0.6));

        VBox loginContainer = createLoginContainer();

        root.getChildren().addAll(background, overlay, loginContainer);
        this.scene = new Scene(root, 800, 600);
    }

    private VBox createLoginContainer() {
        VBox loginContainer = new VBox(20);
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setPadding(new Insets(30, 40, 30, 40));
        loginContainer.setMaxWidth(420);
        loginContainer.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.85),
                new CornerRadii(10),
                Insets.EMPTY)));
        loginContainer.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 255, 255, 0.1),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1))));

        Label title = createTitle();

        usernameField = createTextField("Login");
        passwordField = createPasswordField("Hasło");

        HBox optionsBox = createOptionsBox();

        loginButton = createLoginButton();

        HBox registerBox = createRegisterBox();

        Label socialLabel = new Label("Zaloguj");
        socialLabel.setTextFill(Color.rgb(255, 255, 255, 0.7));

        HBox socialIcons = createSocialIcons();

        loginContainer.getChildren().addAll(
                title,
                usernameField,
                passwordField,
                optionsBox,
                loginButton,
                registerBox,
                socialLabel,
                socialIcons
        );

        return loginContainer;
    }

    private Label createTitle() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(220, 20, 60, 0.5));
        glow.setOffsetX(0);
        glow.setOffsetY(0);
        glow.setRadius(10);

        Label title = new Label("BLACK IRON");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.CRIMSON);
        title.setEffect(glow);
        return title;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); " +
                "-fx-background-radius: 25; " +
                "-fx-border-color: rgba(255, 255, 255, 0.1); " +
                "-fx-border-radius: 25; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 0 20 0 20; " +
                "-fx-pref-height: 50;");
        field.setPrefWidth(340);
        return field;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); " +
                "-fx-background-radius: 25; " +
                "-fx-border-color: rgba(255, 255, 255, 0.1); " +
                "-fx-border-radius: 25; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 0 20 0 20; " +
                "-fx-pref-height: 50;");
        field.setPrefWidth(340);
        return field;
    }

    private HBox createOptionsBox() {
        HBox optionsBox = new HBox(10);
        optionsBox.setAlignment(Pos.CENTER_LEFT);

        rememberMe = new CheckBox("Zapamiętaj");
        rememberMe.setTextFill(Color.WHITE);
        rememberMe.setStyle("-fx-text-fill: white; -fx-font-size: 14.5;");

        forgotPassword = new Hyperlink("Zapomniałeś hasła?");
        forgotPassword.setTextFill(Color.CRIMSON);
        forgotPassword.setStyle("-fx-font-size: 14.5; -fx-font-weight: bold;");

        optionsBox.getChildren().addAll(rememberMe, forgotPassword);
        HBox.setHgrow(forgotPassword, Priority.ALWAYS);
        forgotPassword.setAlignment(Pos.CENTER_RIGHT);

        return optionsBox;
    }

    private Button createLoginButton() {
        Button button = new Button("Zaloguj się");
        button.setStyle("-fx-background-color: crimson; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 16; " +
                "-fx-background-radius: 25; " +
                "-fx-pref-height: 45; " +
                "-fx-pref-width: 340;");
        return button;
    }

    private HBox createRegisterBox() {
        HBox registerBox = new HBox();
        registerBox.setAlignment(Pos.CENTER);
        Label registerLabel = new Label("Nie masz konta?");
        registerLabel.setTextFill(Color.WHITE);
        registerLink = new Hyperlink("Zarejestruj się");
        registerLink.setTextFill(Color.CRIMSON);
        registerLink.setStyle("-fx-font-weight: bold;");
        registerBox.getChildren().addAll(registerLabel, registerLink);
        return registerBox;
    }

    private HBox createSocialIcons() {
        HBox socialIcons = new HBox(20);
        socialIcons.setAlignment(Pos.CENTER);

        instagramLink = createSocialLink("Instagram");
        facebookLink = createSocialLink("Facebook");
        appleLink = createSocialLink("Apple");

        socialIcons.getChildren().addAll(instagramLink, facebookLink, appleLink);
        return socialIcons;
    }

    private Hyperlink createSocialLink(String text) {
        Hyperlink link = new Hyperlink(text);
        link.setTextFill(Color.WHITE);
        link.setStyle("-fx-font-size: 24;");
        return link;
    }

    public Scene getScene() {
        return scene;
    }

    // Gettery dla kontrolek
    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public CheckBox getRememberMe() {
        return rememberMe;
    }

    public Hyperlink getForgotPassword() {
        return forgotPassword;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Hyperlink getRegisterLink() {
        return registerLink;
    }

    public Hyperlink getInstagramLink() {
        return instagramLink;
    }

    public Hyperlink getFacebookLink() {
        return facebookLink;
    }

    public Hyperlink getAppleLink() {
        return appleLink;
    }
}