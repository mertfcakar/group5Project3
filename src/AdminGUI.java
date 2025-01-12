import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//these are Placholder codes please write your own

public class AdminGUI {

    public static void display(Stage stage, String username) {
        // Create a simple layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        // Placeholder welcome message
        Label placeholderLabel = new Label("Admmin Dashboard Placeholder\nWelcome, " + username);

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            Main mainApp = new Main();
            mainApp.start(stage); // Return to login screen
        });

        // Add components to layout
        layout.getChildren().addAll(placeholderLabel, logoutButton);

        // Create and set the scene
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Cashier Dashboard");
        stage.show();
    }
}
