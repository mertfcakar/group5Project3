import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Group5 CinemaCenter");

        // Create login interface
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));

        Label userLabel = new Label("Username:");
        loginGrid.add(userLabel, 0, 1);

        TextField userTextField = new TextField();
        loginGrid.add(userTextField, 1, 1);

        Label pwLabel = new Label("Password:");
        loginGrid.add(pwLabel, 0, 2);

        PasswordField pwBox = new PasswordField();
        loginGrid.add(pwBox, 1, 2);

        Button loginButton = new Button("Login");
        loginGrid.add(loginButton, 1, 3);

        Label messageLabel = new Label();
        loginGrid.add(messageLabel, 1, 4);

        Scene loginScene = new Scene(loginGrid, 400, 300);

        loginButton.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwBox.getText();

            String role = AuthenticationManager.authenticate(username, password);
            if (role != null) {
                openMainInterface(primaryStage, username, role);
            } else {
                messageLabel.setTextFill(Color.RED);
                messageLabel.setText("Invalid username or password.");
            }
        });

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void openMainInterface(Stage stage, String username, String role) {
        switch (role) {
            case "manager":
                ManagerGUI.display(stage, username);
                break;
            case "cashier":
                CashierGUI.display(stage, username);
                break;
            case "admin":
                AdminGUI.display(stage, username);
                break;
            default:
                // Handle unexpected roles (e.g., show error or return to login)
                System.out.println("Unknown role: " + role);
                start(stage);
                break;
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}