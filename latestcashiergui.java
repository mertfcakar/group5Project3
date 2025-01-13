import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CashierGUI {

    private static Stage primaryStage;
    private static Connection connection;
    private static Map<String, MovieDetails> movieDatabase;
    private static ShoppingCart shoppingCart;

    // Entry point to display the Cashier GUI
    public static void display(Stage stage, String username) {
        primaryStage = stage;
        movieDatabase = new HashMap<>();
        shoppingCart = new ShoppingCart();

        // Establish database connection using DatabaseFacade
        try {
            DatabaseFacade dbFacade = new DatabaseFacade();
            connection = dbFacade.connect();
            loadMovieDatabase();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database: " + e.getMessage());
            return;
        }

        showSearchInterface();
    }

    // Loads movie details from the database into a local map
    private static void loadMovieDatabase() throws SQLException {
        String query = "SELECT title, summary_path, genre, poster_path FROM movies";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String title = rs.getString("title");
                String summary = rs.getString("summary_path");
                String genre = rs.getString("genre");
                String posterPath = rs.getString("poster_path");
                movieDatabase.put(title, new MovieDetails(title, summary, genre, posterPath));
            }
        }
    }

    // Displays the search interface where users can search for movies
    private static void showSearchInterface() {
        primaryStage.setTitle("Cashier Operations - Search Movies");

        Label searchLabel = new Label("Search for a Movie:");
        TextField searchField = new TextField();
        ComboBox<String> genreComboBox = new ComboBox<>();
        genreComboBox.getItems().addAll(getUniqueGenres());
        Button searchButton = new Button("Search");

        ListView<String> resultsList = new ListView<>();
        // Search functionality: filters by title and genre
        searchButton.setOnAction(e -> {
            String searchTerm = searchField.getText().toLowerCase();
            String selectedGenre = genreComboBox.getValue();
            resultsList.getItems().clear();

            for (MovieDetails movie : movieDatabase.values()) {
                if ((searchTerm.isEmpty() || movie.getTitle().toLowerCase().contains(searchTerm)) &&
                        (selectedGenre == null || movie.getGenre().equalsIgnoreCase(selectedGenre))) {
                    resultsList.getItems().add(movie.getTitle());
                }
            }
        });

        Button confirmButton = new Button("Confirm");
        // Proceeds to display movie details upon selection
        confirmButton.setOnAction(e -> {
            String selectedMovie = resultsList.getSelectionModel().getSelectedItem();
            if (selectedMovie == null) {
                showAlert(Alert.AlertType.ERROR, "Selection Error", "Please select a movie.");
            } else {
                showMovieDetails(selectedMovie);
            }
        });

        VBox layout = new VBox(10, searchLabel, searchField, genreComboBox, searchButton, resultsList, confirmButton);
        primaryStage.setScene(new Scene(layout, 400, 300));
        primaryStage.show();
    }

    // Displays details about the selected movie, including poster and summary
    private static void showMovieDetails(String movieTitle) {
        MovieDetails movie = movieDatabase.get(movieTitle);
        if (movie == null) return;

        Label titleLabel = new Label("Title: " + movie.getTitle());
        Label genreLabel = new Label("Genre: " + movie.getGenre());
        Label summaryLabel = new Label("Summary: " + movie.getSummary());
        ImageView posterView = new ImageView(movie.getPosterPath());

        Button confirmButton = new Button("Approve");
        confirmButton.setOnAction(e -> showDaySessionSelection(movieTitle));

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showSearchInterface());

        VBox layout = new VBox(10, titleLabel, genreLabel, summaryLabel, posterView, confirmButton, backButton);
        primaryStage.setScene(new Scene(layout, 400, 400));
    }

    // Allows the user to select the day and session for the chosen movie
    private static void showDaySessionSelection(String movieTitle) {
        Label movieLabel = new Label("Movie: " + movieTitle);
        Label dayLabel = new Label("Select Day:");
        ComboBox<String> dayComboBox = new ComboBox<>();
        dayComboBox.getItems().addAll("2025-01-13", "2025-01-14", "2025-01-15", "2025-01-16", "2025-01-17");

        Label sessionLabel = new Label("Select Session:");
        ComboBox<String> sessionComboBox = new ComboBox<>();
        Label vacantSeatsLabel = new Label();

        try {
            String query = "SELECT start_time FROM sessions WHERE movie_title = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, movieTitle);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sessionComboBox.getItems().add(rs.getString("start_time"));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load session times: " + e.getMessage());
        }

        // Displays the number of vacant seats for the selected session
        sessionComboBox.setOnAction(e -> {
            String selectedSession = sessionComboBox.getValue();
            if (selectedSession != null) {
                vacantSeatsLabel.setText("Vacant seats: " + getVacantSeats(movieTitle, selectedSession));
            }
        });

        Button confirmButton = new Button("Confirm");
        // Proceeds to seat selection after confirming day and session
        confirmButton.setOnAction(e -> {
            String day = dayComboBox.getValue();
            String session = sessionComboBox.getValue();
            if (day == null || session == null) {
                showAlert(Alert.AlertType.ERROR, "Selection Error", "Please make all selections.");
            } else {
                showSeatSelection(movieTitle, day, session);
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showMovieDetails(movieTitle));

        VBox layout = new VBox(10, movieLabel, dayLabel, dayComboBox, sessionLabel, sessionComboBox, vacantSeatsLabel, confirmButton, backButton);
        primaryStage.setScene(new Scene(layout, 400, 400));
    }

    // Allows the user to select seats for the chosen session
    private static void showSeatSelection(String movieTitle, String day, String session) {
        Label seatLabel = new Label("Select Seats:");
        GridPane seatGrid = new GridPane();

        try {
            String query = "SELECT seat_number, is_available FROM available_seats WHERE movie_title = ? AND session_time = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, movieTitle);
            stmt.setString(2, session);
            ResultSet rs = stmt.executeQuery();

            int row = 0;
            int col = 0;
            while (rs.next()) {
                String seatNumber = rs.getString("seat_number");
                boolean isAvailable = rs.getBoolean("is_available");
                Button seatButton = new Button(seatNumber);
                seatButton.setDisable(!isAvailable);

                seatButton.setOnAction(e -> {
                    shoppingCart.addItem("Seat: " + seatNumber, 50.0); // Example seat price
                    showAlert(Alert.AlertType.INFORMATION, "Seat Added", "Seat " + seatNumber + " added to cart.");
                });

                seatGrid.add(seatButton, col, row);
                col++;
                if (col == 5) { // Limit to 5 seats per row for better visualization
                    col = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load seat data: " + e.getMessage());
        }

        Button proceedButton = new Button("Proceed to Checkout");
        proceedButton.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION, "Checkout", "Proceeding to checkout."));

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showDaySessionSelection(movieTitle));

        VBox layout = new VBox(10, seatLabel, seatGrid, proceedButton, backButton);
        primaryStage.setScene(new Scene(layout, 400, 400));
    }

    // Retrieves the number of vacant seats for a specific session and movie
    private static int getVacantSeats(String movieTitle, String session) {
        try {
            String query = "SELECT COUNT(*) AS vacant_seats FROM available_seats WHERE session_time = ? AND movie_title = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, session);
            stmt.setString(2, movieTitle);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("vacant_seats");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not retrieve vacant seats: " + e.getMessage());
        }
        return 0;
    }

    // Displays an alert with the specified type, title, and message
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Encapsulates movie-related data
    private static class MovieDetails {
        private final String title;
        private final String summary;
        private final String genre;
        private final String posterPath;

        public MovieDetails(String title, String summary, String genre, String posterPath) {
            this.title = title;
            this.summary = summary;
            this.genre = genre;
            this.posterPath = posterPath;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String getGenre() {
            return genre;
        }

        public String getPosterPath() {
            return posterPath;
        }
    }

    // Retrieves unique genres from the movie database
    private static String[] getUniqueGenres() {
        return movieDatabase.values().stream()
                .map(MovieDetails::getGenre)
                .distinct()
                .toArray(String[]::new);
    }
}
