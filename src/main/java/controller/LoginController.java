package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Victim;
import service.VictimAuthenticationService;

import java.io.IOException;

/**
 * Controller for the Login page.
 * Handles victim authentication, login functionality, and navigation to other scenes.
 */
public class LoginController {

    private static final String AUTH_WINDOW_TITLE = "PhishNet - Cybersecurity Incident Reporting System";

    @FXML
    private TextField emailField;  // Input field for victim email

    @FXML
    private PasswordField passwordField;  // Hidden password field

    @FXML
    private TextField passwordVisibleField;  // Visible password field for toggle functionality

    @FXML
    private Button togglePasswordButton;  // Button to show/hide password

    @FXML
    private ImageView togglePasswordImageView;  // Icon representing show/hide password

    @FXML
    private Button loginButton;  // Login button

    @FXML
    private Button signUpButton;  // Sign up button

    @FXML
    private Hyperlink adminLoginLink;  // Link to admin login

    private VictimAuthenticationService authService;  // Service to handle victim authentication
    private Victim currentVictim;  // Stores currently logged-in victim
    private boolean passwordVisible = false;  // Tracks password visibility state

    /**
     * Initializes the controller.
     * Sets up listeners for password field synchronization and toggle button visibility.
     */
    @FXML
    public void initialize() {
        authService = new VictimAuthenticationService();
        System.out.println("LoginController initialized");

        // Sync hidden and visible password fields based on passwordVisible state
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!passwordVisible) {
                passwordVisibleField.setText(newVal);
            }
            // Show toggle button only if there is some text in the password field
            togglePasswordButton.setVisible(newVal != null && newVal.length() > 0);
        });

        passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisible) {
                passwordField.setText(newVal);
            }
        });

        // Initially hide the password toggle button
        togglePasswordButton.setVisible(false);
    }

    /**
     * Toggles the visibility of the password.
     * Switches between masked and visible password fields and updates the icon.
     */
    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Show password in visible field
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordField.setVisible(false);
            // Update icon to 'hide' symbol
            togglePasswordImageView.setImage(new Image(getClass().getResourceAsStream("/SceneBuilder/assets/hidepassword.png")));
        } else {
            // Hide password in hidden field
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordVisibleField.setVisible(false);
            // Update icon to 'show' symbol
            togglePasswordImageView.setImage(new Image(getClass().getResourceAsStream("/SceneBuilder/assets/showpassword.png")));
        }
    }

    /**
     * Handles the login action.
     * Validates input, authenticates the victim, and navigates to the dashboard if successful.
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordVisible ? passwordVisibleField.getText() : passwordField.getText();

        // Validate email input
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your email address");
            return;
        }

        // Validate password input
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your password");
            return;
        }

        // Attempt to log in using the authentication service
        Victim victim = authService.login(email, password);

        if (victim != null) {
            currentVictim = victim;
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Welcome, " + victim.getName() + "!\nYou can now report incidents.");

            loadVictimDashboard(victim);  // Navigate to Victim Dashboard
        } else {
            // Login failed
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid email or password. Please try again.");
            passwordField.clear();
            passwordVisibleField.clear();
        }
    }

    /**
     * Loads the Victim Dashboard scene and passes the logged-in victim to its controller.
     *
     * @param victim The currently logged-in victim
     */
    private void loadVictimDashboard(Victim victim) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/fxml/victim/VictimDashboard.fxml"));
            Parent root = loader.load();

            // Pass victim data to dashboard controller
            VictimDashboardController dashboardCtrl = loader.getController();
            dashboardCtrl.setCurrentVictim(victim);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 750));
            stage.setTitle("PhishNet – Victim Dashboard");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load the victim dashboard.");
        }
    }

    /**
     * Handles navigation to the Sign Up page.
     */
    @FXML
    private void handleSignUp() {
        navigateToScene("/SceneBuilder/fxml/auth/SignUp.fxml");
    }

    /**
     * Handles navigation to the Admin Login page.
     */
    @FXML
    private void handleAdminLogin() {
        navigateToScene("/SceneBuilder/fxml/auth/AdminLogin.fxml");
    }

    /**
     * Generic method to navigate to a specified FXML scene.
     *
     * @param fxmlPath Path to the FXML file
     */
    private void navigateToScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle(AUTH_WINDOW_TITLE);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to navigate to: " + fxmlPath + "\nError: " + e.getMessage());
        }
    }

    /**
     * Displays an alert dialog.
     *
     * @param alertType The type of the alert
     * @param title     The alert title
     * @param content   The content/message of the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}