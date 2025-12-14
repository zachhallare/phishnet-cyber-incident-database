package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.VictimAuthenticationService;

import java.io.IOException;

/**
 * Controller for the Sign-Up screen.
 *
 * <p>Handles victim account registration and navigation between the login
 * and sign-up screens.</p>
 */
public class SignUpController {

    // FXML-injected UI fields
    @FXML private TextField nameField;                  // Input for full name
    @FXML private TextField emailField;                 // Input for email
    @FXML private PasswordField passwordField;         // Input for password
    @FXML private PasswordField confirmPasswordField;  // Input for password confirmation
    @FXML private Hyperlink adminLoginLink;            // Link to navigate to admin login

    // Service for victim registration
    private VictimAuthenticationService authService = new VictimAuthenticationService();

    /**
     * Handle the Sign-Up button click.
     * Validates input, registers the user, and navigates to login on success.
     */
    @FXML
    private void handleSignUp() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate empty fields
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        try {
            // Attempt registration via the authentication service
            boolean success = authService.register(name, email, password);

            if (success) {
                // Registration successful
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Account created successfully! You can now log in and report incidents.");

                // Clear input fields
                nameField.clear();
                emailField.clear();
                passwordField.clear();
                confirmPasswordField.clear();

                // Navigate to login screen
                navigateToLogin();
            } else {
                // Registration failed due to possible validation or database issues
                showAlert(Alert.AlertType.ERROR, "Registration Failed",
                        "Registration failed. Possible reasons:\n" +
                        "- Email may already be registered\n" +
                        "- Invalid email format\n" +
                        "- Password must be at least 6 characters\n" +
                        "- Database connection issue\n\n" +
                        "Please check the console for details.");
            }
        } catch (RuntimeException e) {
            // Catch runtime exceptions, show alert and print stack trace
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "An error occurred during registration:\n" + e.getMessage() + 
                    "\n\nPlease check the console for more details.");
            e.printStackTrace();
        } catch (Exception e) {
            // Catch unexpected exceptions
            showAlert(Alert.AlertType.ERROR, "Unexpected Error",
                    "An unexpected error occurred:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Back button click to navigate back to the login screen.
     */
    @FXML
    private void handleBackToLogin() {
        navigateToLogin();
    }

    /**
     * Handle Admin Login hyperlink click.
     * Navigates to the login screen (admin login handled on login screen).
     */
    @FXML
    private void handleAdminLogin() {
        navigateToLogin();
    }

    /**
     * Utility method to navigate to the login screen.
     * Loads the FXML and replaces the current scene.
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/fxml/auth/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to navigate to login screen.");
        }
    }

    /**
     * Show an alert dialog.
     *
     * @param type  The type of alert (ERROR, INFORMATION, WARNING)
     * @param title The title of the alert
     * @param msg   The message content
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

