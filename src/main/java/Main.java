// No package - default package
import util.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main entry point for the PhishNet - Cybersecurity Incident Reporting System.
 * Starts the JavaFX application, checks the database connection, loads the login screen,
 * and handles clean shutdown.
 */
public class Main extends Application {

    // Path to the app logo (the one without text in the name)
    private static final String APP_ICON_PATH = "/SceneBuilder/assets/ccinfom phishnet logo no name.png";

    /**
     * Called by JavaFX when the app starts. Sets up the main window,
     * tests the database, loads Login.fxml, and shows the login screen.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Make sure the whole app quits when the last window is closed
            Platform.setImplicitExit(true);
            
            // Add shutdown hook to ensure clean exit when IDE closes
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown hook triggered - cleaning up...");
                DatabaseConnection.closeConnection();
                Platform.exit();
            }));
            
            // When the user clicks the X button, close everything properly
            primaryStage.setOnCloseRequest(e -> {
                stop();              // close DB connection
                Platform.exit();     // stop JavaFX
                System.exit(0);      // terminate JVM
            });
            
            // Early check: can we actually reach the database?
            if (!DatabaseConnection.testConnection()) {
                showErrorAndExit("Database Connection Failed",
                        "Could not connect to the database.\n" +
                                "Please ensure MySQL is running and the database is configured correctly.");
                return; // abort startup if DB is down
            }

            System.out.println("Database connection successful!");

            // Load the victim's login screen (Login.fxml)
            java.net.URL fxmlUrl = Main.class.getClassLoader().getResource("SceneBuilder/fxml/auth/Login.fxml");
            if (fxmlUrl == null) {
                // Sometimes a leading slash is needed - try that too
                fxmlUrl = Main.class.getClassLoader().getResource("/SceneBuilder/fxml/auth/Login.fxml");
            }
            if (fxmlUrl == null) {
                // Still can't find it - give a clear error message
                throw new IOException("FXML file not found. Searched for:\n" +
                        "- SceneBuilder/fxml/auth/Login.fxml\n" +
                        "- /SceneBuilder/fxml/auth/Login.fxml\n" +
                        "Make sure file exists in src/resources/ and run 'mvn clean compile'");
            }
            
            System.out.println("Loading FXML from: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();   // build the UI from the FXML file

            // Basic stage/window configuration
            primaryStage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            setApplicationIcon(primaryStage);          // put the logo in the title bar
            primaryStage.setScene(new Scene(root, 600, 450));
            primaryStage.setResizable(false);          // keep the size fixed
            primaryStage.show();                       // finally show the window

            System.out.println("Victim login screen loaded successfully!");

        } catch (IOException e) {
            // Problem loading the FXML - give the user a helpful message
            e.printStackTrace();
            String errorMsg = "Failed to load Login.fxml.\n\n";
            errorMsg += "Expected path: /SceneBuilder/fxml/auth/Login.fxml\n";
            errorMsg += "Check that:\n";
            errorMsg += "1. File exists in src/resources/SceneBuilder/fxml/auth/\n";
            errorMsg += "2. Resources are copied to target/classes/\n";
            errorMsg += "3. Run 'mvn clean compile' to rebuild\n\n";
            errorMsg += "Error: " + e.getMessage();
            showErrorAndExit("Application Error", errorMsg);
            
        } catch (Exception e) {
            // Catch-all for anything unexpected
            e.printStackTrace();
            showErrorAndExit("Unexpected Error",
                    "An unexpected error occurred:\n" + e.getMessage());
        }
    }

    /**
     * Sets the PhishNet logo as the window icon.
     * If the icon file is missing, it just prints a warning and continues.
     */
    private void setApplicationIcon(Stage stage) {
        java.net.URL iconUrl = Main.class.getResource(APP_ICON_PATH);
        if (iconUrl == null) {
            System.err.println("Warning: Application icon not found at " + APP_ICON_PATH);
            return;
        }

        Image appIcon = new Image(iconUrl.toExternalForm());
        stage.getIcons().add(appIcon);   // add the icon to the stage
    }

    /**
     * Called when the application is shutting down.
     * Closes the database connection so we don't leave it hanging.
     */
    @Override
    public void stop() {
        // Clean up database connection
        DatabaseConnection.closeConnection();
        System.out.println("Application closed. Database connection terminated.");
    }

    /**
     * Shows an error dialog with the given title/message and then exits the program.
     * Used for critical failures that prevent the app from running.
     */
    private void showErrorAndExit(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();   // wait for the user to close the dialog
        System.exit(1);        // non-zero means error
    }

    /**
     * Standard main method - just launches the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

