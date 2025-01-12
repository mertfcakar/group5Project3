import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class ManagerGUI {
    private static AuthenticationManager dbFacade = new AuthenticationManager();
    private static String currentUsername;

    /**
     * Displays the manager interface
     * @param stage The primary stage
     * @param username The logged-in manager's username
     */
    public static void display(Stage stage, String username) {
        currentUsername = username;
        
        // Create main layout
        TabPane tabPane = new TabPane();
        
        // Create tabs
        Tab inventoryTab = createInventoryTab();
        Tab personnelTab = createPersonnelTab();
        Tab pricingTab = createPricingTab();
        Tab revenueTab = createRevenueTab();
        
        tabPane.getTabs().addAll(inventoryTab, personnelTab, pricingTab, revenueTab);
        
        // Add logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            stage.close();
            new Main().start(new Stage());
        });
        
        // Create layout with username display
        VBox mainLayout = new VBox(10);
        Label userLabel = new Label("Manager: " + username);
        userLabel.setStyle("-fx-font-weight: bold");
        
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.getChildren().addAll(userLabel, logoutBtn);
        
        mainLayout.getChildren().addAll(topBar, tabPane);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Group5 CinemaCenter - Manager Interface");
        stage.show();
    }
    // For Product table (in createInventoryTab method):
private static void setupInventoryTable(TableView<Product> table) {
    // Create and configure columns
    TableColumn<Product, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
    
    TableColumn<Product, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
    
    TableColumn<Product, Number> stockCol = new TableColumn<>("Stock");
    stockCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()));
    
    TableColumn<Product, Number> priceCol = new TableColumn<>("Price");
    priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()));
    priceCol.setCellFactory(tc -> new TableCell<Product, Number>() {
        @Override
        protected void updateItem(Number price, boolean empty) {
            super.updateItem(price, empty);
            if (empty) {
                setText(null);
            } else {
                setText(String.format("%.2f TL", price.doubleValue()));
            }
        }
    });

    // Add all columns to table
    table.getColumns().addAll(nameCol, typeCol, stockCol, priceCol);
}

// For User table (in createPersonnelTab method):
private static void setupPersonnelTable(TableView<User> table) {
    // Create and configure columns
    TableColumn<User, String> usernameCol = new TableColumn<>("Username");
    usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
    
    TableColumn<User, String> firstNameCol = new TableColumn<>("First Name");
    firstNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFirstName()));
    
    TableColumn<User, String> lastNameCol = new TableColumn<>("Last Name");
    lastNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
    
    TableColumn<User, String> roleCol = new TableColumn<>("Role");
    roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

    // Add all columns to table
    table.getColumns().addAll(usernameCol, firstNameCol, lastNameCol, roleCol);
}

private static Tab createInventoryTab() {
    Tab tab = new Tab("Inventory Management");
    tab.setClosable(false);

    VBox content = new VBox(10);
    content.setPadding(new Insets(10));

    // Create table for products
    TableView<Product> table = new TableView<>();
    setupInventoryTable(table);

    // Add controls for updating stock
    TextField quantityField = new TextField();
    quantityField.setPromptText("Enter quantity");
    Button updateStockBtn = new Button("Update Stock");

    // Add controls for adding new stock
    TextField productNameField = new TextField();
    productNameField.setPromptText("Product Name");
    TextField productTypeField = new TextField();
    productTypeField.setPromptText("Product Type");
    TextField productPriceField = new TextField();
    productPriceField.setPromptText("Product Price");
    TextField productStockField = new TextField();
    productStockField.setPromptText("Initial Stock Quantity");
    Button addStockBtn = new Button("Add Stock");

    // Add button for removing a product
    Button removeProductBtn = new Button("Remove Product");

    updateStockBtn.setOnAction(e -> {
        Product selectedProduct = table.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            try {
                int quantity = Integer.parseInt(quantityField.getText()); // Get the new stock value
                updateProductStock(selectedProduct.getId(), quantity);   // Update stock in the database
                refreshInventoryTable(table);                           // Refresh the inventory table
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid number for stock quantity");
            }
        } else {
            showAlert("Error", "Please select a product to update stock");
        }
    });
    
    addStockBtn.setOnAction(e -> {
        String name = productNameField.getText();
        String type = productTypeField.getText();
        String priceText = productPriceField.getText();
        String stockText = productStockField.getText();

        if (name.isEmpty() || type.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            showAlert("Error", "All fields are required to add a new product");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);
            addNewProduct(name, type, price, stock);
            refreshInventoryTable(table);

            // Clear fields after adding the product
            productNameField.clear();
            productTypeField.clear();
            productPriceField.clear();
            productStockField.clear();
        } catch (NumberFormatException ex) {
            showAlert("Error", "Invalid price or stock quantity. Please enter valid numbers");
        }
    });

    // Event handler for removing a product
    removeProductBtn.setOnAction(e -> {
        Product selectedProduct = table.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            try (Connection conn = dbFacade.connect()) {
                String query = "DELETE FROM products WHERE product_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedProduct.getId());
                stmt.executeUpdate();
                refreshInventoryTable(table);
                showAlert("Success", "Product removed successfully");
            } catch (SQLException ex) {
                showAlert("Error", "Failed to remove product: " + ex.getMessage());
            }
        } else {
            showAlert("Error", "Please select a product to remove");
        }
    });

    // Layout for updating stock
    HBox updateControls = new HBox(10);
    updateControls.getChildren().addAll(quantityField, updateStockBtn);

    // Layout for adding and removing stock
    GridPane addControls = new GridPane();
    addControls.setHgap(10);
    addControls.setVgap(10);
    addControls.addRow(0, new Label("Name:"), productNameField);
    addControls.addRow(1, new Label("Type:"), productTypeField);
    addControls.addRow(2, new Label("Price:"), productPriceField);
    addControls.addRow(3, new Label("Stock:"), productStockField);
    addControls.addRow(4, addStockBtn, removeProductBtn); // Add both buttons here

    content.getChildren().addAll(table, updateControls, addControls);
    tab.setContent(content);

    // Load initial data
    Platform.runLater(() -> refreshInventoryTable(table));

    return tab;
}


    private static void addNewProduct(String name, String type, double price, int stock) {
        try (Connection conn = dbFacade.connect()) {
            String query = "INSERT INTO products (name, type, price, stock_quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, type);
            stmt.setDouble(3, price);
            stmt.setInt(4, stock);
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Error", "Failed to add new product: " + e.getMessage());
        }
    }
    

    private static Tab createPersonnelTab() {
        Tab tab = new Tab("Personnel Management");
        tab.setClosable(false);
    
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
    
        TableView<User> table = new TableView<>();
        setupPersonnelTable(table);
    
        // Input fields
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s]*")) { // Allow only alphabetic characters and spaces
                firstNameField.setText(oldValue); // Revert to the old value
            }
        });
    
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s]*")) { // Allow only alphabetic characters and spaces
                lastNameField.setText(oldValue); // Revert to the old value
            }
        });
    
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
    
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
    
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.setPromptText("Role");
        roleCombo.getItems().addAll("cashier", "admin");
    
        // Buttons
        Button addBtn = new Button("Add Personnel");
        Button removeBtn = new Button("Remove Personnel");
    
        // Add personnel logic
        addBtn.setOnAction(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleCombo.getValue();
        
            // Ensure all fields are filled and username contains at least one letter
            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
                showAlert("Error", "All fields must be filled.");
                return;
            }
        
            if (!username.matches(".*[a-zA-Z].*")) { // Regex to check if username contains at least one letter
                showAlert("Error", "Username must contain at least one letter.");
                return;
            }
        
            try (Connection conn = dbFacade.connect()) {
                String query = "INSERT INTO users (username, password, first_name, last_name, role) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, firstName);
                stmt.setString(4, lastName);
                stmt.setString(5, role);
        
                stmt.executeUpdate();
                refreshPersonnelTable(table);
                showAlert("Success", "Personnel added successfully");
        
                // Clear fields
                firstNameField.clear();
                lastNameField.clear();
                usernameField.clear();
                passwordField.clear();
                roleCombo.setValue(null);
            } catch (SQLException ex) {
                showAlert("Error", "Failed to add personnel: " + ex.getMessage());
            }
        });
        
    
        // Remove personnel logic
        removeBtn.setOnAction(e -> {
            User selectedUser = table.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                showAlert("Error", "Please select a user to remove.");
                return;
            }
    
            try (Connection conn = dbFacade.connect()) {
                String query = "DELETE FROM users WHERE user_id = ? AND username != ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedUser.getId());
                stmt.setString(2, currentUsername); // Prevent self-deletion
    
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    refreshPersonnelTable(table);
                    showAlert("Success", "Personnel removed successfully.");
                } else {
                    showAlert("Error", "Failed to remove personnel.");
                }
            } catch (SQLException ex) {
                showAlert("Error", "Failed to remove personnel: " + ex.getMessage());
            }
        });
    
        // Layout for input fields
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.addRow(0, new Label("First Name:"), firstNameField);
        inputGrid.addRow(1, new Label("Last Name:"), lastNameField);
        inputGrid.addRow(2, new Label("Username:"), usernameField);
        inputGrid.addRow(3, new Label("Password:"), passwordField);
        inputGrid.addRow(4, new Label("Role:"), roleCombo);
    
        // Button layout
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addBtn, removeBtn);
    
        // Combine all components
        content.getChildren().addAll(table, inputGrid, buttonBox);
        tab.setContent(content);
    
        // Load initial data
        Platform.runLater(() -> refreshPersonnelTable(table));
    
        return tab;
    }
    

    private static Tab createPricingTab() {
        Tab tab = new Tab("Pricing Management");
        tab.setClosable(false);
    
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
    
        // Create fields for different pricing configurations
        TextField ticketPriceField = new TextField();
        TextField above60DiscountField = new TextField();
        TextField below18DiscountField = new TextField();
    
        Button updateBtn = new Button("Update Prices and Discounts");
    
        // Set button action
        updateBtn.setOnAction(e -> {
            try {
                double ticketPrice = Double.parseDouble(ticketPriceField.getText());
                double above60Discount = Double.parseDouble(above60DiscountField.getText());
                double below18Discount = Double.parseDouble(below18DiscountField.getText());
    
                updatePrices(ticketPrice, above60Discount, below18Discount);
    
                showAlert("Success", "Prices and discounts updated successfully.");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid input. Please enter valid numbers.");
            }
        });
    
        // Add components to grid
        grid.addRow(0, new Label("Base Ticket Price:"), ticketPriceField);
        grid.addRow(1, new Label("Above 60 Discount Rate (%):"), above60DiscountField);
        grid.addRow(2, new Label("Below 18 Discount Rate (%):"), below18DiscountField);
        grid.addRow(3, updateBtn);
    
        tab.setContent(grid);
    
        // Load current values
        Platform.runLater(() -> loadCurrentPricing(ticketPriceField, above60DiscountField, below18DiscountField));
    
        return tab;
    }
    
    
    private static void loadCurrentPricing(TextField ticketField, TextField above60Field, TextField below18Field) {
        try (Connection conn = dbFacade.connect()) {
            String query = "SELECT config_key, config_value FROM config WHERE config_key IN ('ticket_base_price', 'above_60_discount_rate', 'below_18_discount_rate')";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
    
            while (rs.next()) {
                String key = rs.getString("config_key");
                String value = rs.getString("config_value");
    
                if (key.equals("ticket_base_price")) {
                    ticketField.setText(value);
                } else if (key.equals("above_60_discount_rate")) {
                    above60Field.setText(value);
                } else if (key.equals("below_18_discount_rate")) {
                    below18Field.setText(value);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load current prices: " + e.getMessage());
        }
    }
    
    
    private static void UpdatePrices(double ticketPrice, double above60Discount, double below18Discount) {
        try (Connection conn = dbFacade.connect()) {
            conn.setAutoCommit(false); // Start a transaction
    
            String ticketQuery = "UPDATE config SET config_value = ? WHERE config_key = 'ticket_base_price'";
            String above60Query = "UPDATE config SET config_value = ? WHERE config_key = 'above_60_discount_rate'";
            String below18Query = "UPDATE config SET config_value = ? WHERE config_key = 'below_18_discount_rate'";
    
            try (PreparedStatement ticketStmt = conn.prepareStatement(ticketQuery);
                 PreparedStatement above60Stmt = conn.prepareStatement(above60Query);
                 PreparedStatement below18Stmt = conn.prepareStatement(below18Query)) {
    
                // Update ticket base price
                ticketStmt.setString(1, String.valueOf(ticketPrice));
                ticketStmt.executeUpdate();
    
                // Update discount rate for above 60
                above60Stmt.setString(1, String.valueOf(above60Discount));
                above60Stmt.executeUpdate();
    
                // Update discount rate for below 18
                below18Stmt.setString(1, String.valueOf(below18Discount));
                below18Stmt.executeUpdate();
    
                // Commit the transaction
                conn.commit();
    
                // Single success alert after successful update
                showAlert("Success", "Prices and discounts updated successfully.");
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction if any error occurs
                showAlert("Error", "Failed to update prices: " + e.getMessage());
            }
        } catch (SQLException e) {
            showAlert("Error", "Database connection issue: " + e.getMessage());
        }
    }
    

    private static Tab createRevenueTab() {
        Tab tab = new Tab("Revenue & Tax Information");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Create labels for displaying information
        Label totalRevenueLabel = new Label();
        Label ticketTaxLabel = new Label();
        Label productTaxLabel = new Label();
        Label totalTaxLabel = new Label();

        content.getChildren().addAll(
            totalRevenueLabel,
            ticketTaxLabel,
            productTaxLabel,
            totalTaxLabel
        );

        tab.setContent(content);
        
        // Load revenue data
        Platform.runLater(() -> updateRevenueInformation(
            totalRevenueLabel,
            ticketTaxLabel,
            productTaxLabel,
            totalTaxLabel
        ));
        
        return tab;
    }

    // Helper methods for database operations
    private static void updateProductStock(int productId, int newStockQuantity) {
        try (Connection conn = dbFacade.connect()) {
            String query = "UPDATE products SET stock_quantity = ? WHERE product_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, newStockQuantity); // Set the new stock value
            stmt.setInt(2, productId);        // Specify the product ID
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Error", "Failed to update stock: " + e.getMessage());
        }
    }

    private static void updatePrices(double ticketPrice, double above60Discount, double below18Discount) {
        String alertMessage = ""; // Message to display in the alert
        boolean success = false; // Flag to indicate success or failure
    
        try (Connection conn = dbFacade.connect()) {
            conn.setAutoCommit(false); // Start a transaction
    
            String ticketQuery = "UPDATE config SET config_value = ? WHERE config_key = 'ticket_base_price'";
            String above60Query = "UPDATE config SET config_value = ? WHERE config_key = 'above_60_discount_rate'";
            String below18Query = "UPDATE config SET config_value = ? WHERE config_key = 'below_18_discount_rate'";
    
            try (PreparedStatement ticketStmt = conn.prepareStatement(ticketQuery);
                 PreparedStatement above60Stmt = conn.prepareStatement(above60Query);
                 PreparedStatement below18Stmt = conn.prepareStatement(below18Query)) {
    
                // Update ticket base price
                ticketStmt.setString(1, String.valueOf(ticketPrice));
                ticketStmt.executeUpdate();
    
                // Update discount rate for above 60
                above60Stmt.setString(1, String.valueOf(above60Discount));
                above60Stmt.executeUpdate();
    
                // Update discount rate for below 18
                below18Stmt.setString(1, String.valueOf(below18Discount));
                below18Stmt.executeUpdate();
    
                // Commit the transaction
                conn.commit();
                success = true; // Mark as successful
                alertMessage = "Prices and discounts updated successfully.";
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction if any error occurs
                alertMessage = "Failed to update prices: " + e.getMessage();
            }
        } catch (SQLException e) {
            alertMessage = "Database connection issue: " + e.getMessage();
        }
    
        // Ensure only one alert is shown
        final String finalAlertMessage = alertMessage;
        final boolean isSuccess = success;
        Platform.runLater(() -> {
            if (isSuccess) {
                showAlert("Success", finalAlertMessage);
            } else {
                showAlert("Error", finalAlertMessage);
            }
        });
    }
    

    private static void loadCurrentPrices(TextField ticketField, TextField discountField) {
        try (Connection conn = dbFacade.connect()) {
            String query = "SELECT config_key, config_value FROM config WHERE config_key IN ('ticket_base_price', 'age_discount_rate')";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                String key = rs.getString("config_key");
                String value = rs.getString("config_value");
                
                if (key.equals("ticket_base_price")) {
                    ticketField.setText(value);
                } else if (key.equals("age_discount_rate")) {
                    discountField.setText(value);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load current prices: " + e.getMessage());
        }
    }

    private static void refreshInventoryTable(TableView<Product> table) {
        try (Connection conn = dbFacade.connect()) {
            String query = "SELECT * FROM products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
    
            ObservableList<Product> products = FXCollections.observableArrayList();
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getInt("stock_quantity"),
                    rs.getDouble("price")
                ));
            }
            table.setItems(products);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load inventory: " + e.getMessage());
        }
    }
    

    private static void refreshPersonnelTable(TableView<User> table) {
        try (Connection conn = dbFacade.connect()) {
            String query = "SELECT * FROM users WHERE username != ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<User> users = FXCollections.observableArrayList();
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("role")
                ));
            }
            table.setItems(users);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load personnel data: " + e.getMessage());
        }
    }

    private static void updateRevenueInformation(Label totalRevenue, Label ticketTax, Label productTax, Label totalTax) {
        try (Connection conn = dbFacade.connect()) {
            String query = "SELECT SUM(total_amount) as revenue, SUM(tax_amount) as tax FROM sales";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
    
            if (rs.next()) {
                double revenue = rs.getDouble("revenue");
                double tax = rs.getDouble("tax");
    
                totalRevenue.setText(String.format("Total Revenue: %.2f TL", revenue));
                totalTax.setText(String.format("Total Tax: %.2f TL", tax));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load revenue data: " + e.getMessage());
        }
    }
    

    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper classes for TableView
    private static class Product {
        private final int id;
        private final String name;
        private final String type;
        private final int stock;
        private final double price;

        public Product(int id, String name, String type, int stock, double price) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.stock = stock;
            this.price = price;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public int getStock() { return stock; }
        public double getPrice() { return price; }
    }

    private static class User {
        private final int id;
        private final String username;
        private final String firstName;
        private final String lastName;
        private final String role;

        public User(int id, String username, String firstName, String lastName, String role) {
            this.id = id;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getRole() { return role; }
    }
    
}
