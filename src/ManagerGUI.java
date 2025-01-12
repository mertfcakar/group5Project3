import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ManagerGUI {

    public static void display(Stage stage, String username) {
        // Create a layout for the Manager GUI
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        // Welcome message
        Label welcomeLabel = new Label("Welcome, " + username + " (Manager)");
        layout.getChildren().add(welcomeLabel);

        // Buttons for Manager operations
        Button viewInventoryButton = new Button("View Inventory");
        Button manageEmployeesButton = new Button("Manage Employees");
        Button adjustPricesButton = new Button("Adjust Prices");
        Button logoutButton = new Button("Logout");

        layout.getChildren().addAll(viewInventoryButton, manageEmployeesButton, adjustPricesButton, logoutButton);

        // Event handlers for buttons
        viewInventoryButton.setOnAction(e -> viewInventory());
        manageEmployeesButton.setOnAction(e -> manageEmployees());
        adjustPricesButton.setOnAction(e -> adjustPrices());
        logoutButton.setOnAction(e -> {
            Main mainApp = new Main();
            mainApp.start(stage); // Return to login screen
        });

        // Set the scene and display
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Manager Dashboard");
        stage.show();
    }

    private static void viewInventory() {
        System.out.println("View Inventory clicked!"); // Replace with actual inventory logic
    }

    private static void manageEmployees() {
        System.out.println("Manage Employees clicked!"); // Replace with actual employee management logic
    }

    private static void adjustPrices() {
        System.out.println("Adjust Prices clicked!"); // Replace with actual price adjustment logic
    }
}
