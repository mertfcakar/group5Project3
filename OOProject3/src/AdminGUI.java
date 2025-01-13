import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminGUI {

    private static AuthenticationManager dbFacade = new AuthenticationManager();

    public static void display(Stage stage, String username) {
        // Main layout
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Admin Dashboard\nWelcome, " + username);
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Buttons for different admin functionalities
        Button addMovieButton = new Button("Add New Movie");
        Button updateMovieButton = new Button("Update Existing Movie");
        Button scheduleButton = new Button("Create Monthly Schedule");
        Button logoutButton = new Button("Logout");

        addMovieButton.setOnAction(e -> showAddMovieDialog());
        updateMovieButton.setOnAction(e -> showUpdateMovieDialog());
        scheduleButton.setOnAction(e -> showScheduleDialog());
        logoutButton.setOnAction(e -> {
            Main mainApp = new Main();
            mainApp.start(stage);
        });

        layout.getChildren().addAll(welcomeLabel, addMovieButton, updateMovieButton, scheduleButton, logoutButton);

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }

    private static void showAddMovieDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Movie");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField titleField = new TextField();
        titleField.setPromptText("Movie Title");
        TextField posterField = new TextField();
        posterField.setPromptText("Poster Path");
        TextField genreField = new TextField();
        genreField.setPromptText("Genre");
        TextArea summaryArea = new TextArea();
        summaryArea.setPromptText("Summary");

        Button addButton = new Button("Add Movie");
        addButton.setOnAction(e -> {
            String title = titleField.getText();
            String poster = posterField.getText();
            String genre = genreField.getText();
            String summary = summaryArea.getText();

            if (title.isEmpty() || poster.isEmpty() || genre.isEmpty() || summary.isEmpty()) {
                showAlert("Error", "All fields must be filled.");
                return;
            }

            try (Connection conn = dbFacade.connect()) {
                String query = "INSERT INTO movies (title, poster, genre, summary) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, title);
                stmt.setString(2, poster);
                stmt.setString(3, genre);
                stmt.setString(4, summary);
                stmt.executeUpdate();
                showAlert("Success", "Movie added successfully!");
                dialog.close();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to add movie: " + ex.getMessage());
            }
        });

        grid.addRow(0, new Label("Title:"), titleField);
        grid.addRow(1, new Label("Poster:"), posterField);
        grid.addRow(2, new Label("Genre:"), genreField);
        grid.addRow(3, new Label("Summary:"), summaryArea);
        grid.add(addButton, 1, 4);

        Scene scene = new Scene(grid, 400, 300);
        dialog.setScene(scene);
        dialog.show();
    }

    private static void showUpdateMovieDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Update Existing Movie");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Fields for movie selection and update
        ComboBox<String> movieComboBox = new ComboBox<>();
        TextField posterField = new TextField();
        posterField.setPromptText("New Poster Path");
        TextField genreField = new TextField();
        genreField.setPromptText("New Genre");
        TextArea summaryArea = new TextArea();
        summaryArea.setPromptText("New Summary");

        Button updateButton = new Button("Update Movie");

        // Load movie titles into ComboBox
        try (Connection conn = dbFacade.connect()) {
            String query = "SELECT title FROM movies";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                movieComboBox.getItems().add(rs.getString("title"));
            }
        } catch (SQLException ex) {
            showAlert("Error", "Failed to load movies: " + ex.getMessage());
        }

        updateButton.setOnAction(e -> {
            String selectedMovie = movieComboBox.getValue();
            String newPoster = posterField.getText();
            String newGenre = genreField.getText();
            String newSummary = summaryArea.getText();

            if (selectedMovie == null || newPoster.isEmpty() || newGenre.isEmpty() || newSummary.isEmpty()) {
                showAlert("Error", "All fields must be filled.");
                return;
            }

            try (Connection conn = dbFacade.connect()) {
                String query = "UPDATE movies SET poster = ?, genre = ?, summary = ? WHERE title = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, newPoster);
                stmt.setString(2, newGenre);
                stmt.setString(3, newSummary);
                stmt.setString(4, selectedMovie);
                stmt.executeUpdate();
                showAlert("Success", "Movie updated successfully!");
                dialog.close();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update movie: " + ex.getMessage());
            }
        });

        grid.addRow(0, new Label("Select Movie:"), movieComboBox);
        grid.addRow(1, new Label("New Poster:"), posterField);
        grid.addRow(2, new Label("New Genre:"), genreField);
        grid.addRow(3, new Label("New Summary:"), summaryArea);
        grid.add(updateButton, 1, 4);

        Scene scene = new Scene(grid, 400, 300);
        dialog.setScene(scene);
        dialog.show();
    }

    private static void showScheduleDialog() {
        // Implementation for creating a schedule
    }

    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}