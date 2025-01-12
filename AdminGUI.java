import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminGUI {
    public static void display(Stage stage, String username) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        // Welcome Label
        Label welcomeLabel = new Label("Welcome, Admin: " + username);

        // Buttons for Admin Operations
        Button addMovieButton = new Button("Add Movie");
        Button updateMovieButton = new Button("Update Movie");

        // Logout Button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            Main mainApp = new Main();
            mainApp.start(stage);
        });

        // Add buttons to layout
        layout.getChildren().addAll(welcomeLabel, addMovieButton, updateMovieButton, logoutButton);

        // Event Handlers
        addMovieButton.setOnAction(e -> addMovie(stage));
        updateMovieButton.setOnAction(e -> updateMovie(stage));

        // Set Scene
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }

    private static void addMovie(Stage stage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        // Fields for movie details
        TextField titleField = new TextField();
        titleField.setPromptText("Enter Movie Title");

        TextField genreField = new TextField();
        genreField.setPromptText("Enter Genre");

        TextArea summaryArea = new TextArea();
        summaryArea.setPromptText("Enter Summary");
        summaryArea.setPrefRowCount(4);

        TextField posterField = new TextField();
        posterField.setPromptText("Enter Poster URL");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String title = titleField.getText();
            String genre = genreField.getText();
            String summary = summaryArea.getText();
            String poster = posterField.getText();

            if (title.isEmpty() || genre.isEmpty() || summary.isEmpty() || poster.isEmpty()) {
                showAlert("Error", "All fields must be filled!", Alert.AlertType.ERROR);
            } else {
                try {
                    DatabaseFacade db = new DatabaseFacade();
                    String query = "INSERT INTO movies (title, genre, summary, poster) VALUES (?, ?, ?, ?)";
                    try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, title);
                        stmt.setString(2, genre);
                        stmt.setString(3, summary);
                        stmt.setString(4, poster);
                        stmt.executeUpdate();

                        showAlert("Success", "Movie added successfully!", Alert.AlertType.INFORMATION);
                        titleField.clear();
                        genreField.clear();
                        summaryArea.clear();
                        posterField.clear();
                    }
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to add movie: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> display(stage, "Admin"));

        layout.getChildren().addAll(new Label("Add New Movie"), titleField, genreField, summaryArea, posterField, saveButton, backButton);

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
    }

    private static void updateMovie(Stage stage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        TextField searchField = new TextField();
        searchField.setPromptText("Enter Movie Title to Search");

        Button searchButton = new Button("Search");
        VBox resultLayout = new VBox(10);
        resultLayout.setAlignment(Pos.CENTER);

        searchButton.setOnAction(e -> {
            String searchTitle = searchField.getText();
            if (searchTitle.isEmpty()) {
                showAlert("Error", "Please enter a movie title to search.", Alert.AlertType.ERROR);
            } else {
                try {
                    DatabaseFacade db = new DatabaseFacade();
                    String query = "SELECT * FROM movies WHERE title = ?";
                    try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, searchTitle);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            TextField titleField = new TextField(rs.getString("title"));
                            TextField genreField = new TextField(rs.getString("genre"));
                            TextArea summaryArea = new TextArea(rs.getString("summary"));
                            TextField posterField = new TextField(rs.getString("poster"));

                            Button saveButton = new Button("Save Changes");
                            saveButton.setOnAction(ev -> {
                                try {
                                    String updateQuery = "UPDATE movies SET title = ?, genre = ?, summary = ?, poster = ? WHERE title = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                        updateStmt.setString(1, titleField.getText());
                                        updateStmt.setString(2, genreField.getText());
                                        updateStmt.setString(3, summaryArea.getText());
                                        updateStmt.setString(4, posterField.getText());
                                        updateStmt.setString(5, searchTitle);
                                        updateStmt.executeUpdate();

                                        showAlert("Success", "Movie updated successfully!", Alert.AlertType.INFORMATION);
                                        display(stage, "Admin");
                                    }
                                } catch (SQLException ex) {
                                    showAlert("Error", "Failed to update movie: " + ex.getMessage(), Alert.AlertType.ERROR);
                                }
                            });

                            resultLayout.getChildren().setAll(new Label("Update Movie Details"), titleField, genreField, summaryArea, posterField, saveButton);
                        } else {
                            showAlert("Error", "Movie not found.", Alert.AlertType.ERROR);
                        }
                    }
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to search for movie: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> display(stage, "Admin"));

        layout.getChildren().addAll(new Label("Update Movie"), searchField, searchButton, resultLayout, backButton);

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
    }

    private static void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
