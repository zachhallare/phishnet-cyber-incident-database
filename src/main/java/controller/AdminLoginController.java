package controller;

import model.Administrator;
import service.AdminAuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the Administrator login modal.
 * Handles admin login, password visibility toggle, and navigation
 * to sign-up or admin dashboard screens.
 */
public class AdminLoginController {

    private static final String AUTH_WINDOW_TITLE = "PhishNet - Cybersecurity Incident Reporting System";

    @FXML private TextField     emailField1;             // Input field for admin email
    @FXML private PasswordField passwordField1;          // Hidden password field
    @FXML private TextField     passwordVisibleField1;   // Visible password field
    @FXML private Button        togglePasswordButton1;   // Button to toggle password visibility
    @FXML private ImageView     togglePasswordImageView1;// Image for toggle button (show/hide)
    @FXML private Button        loginButton1;            // Button to login
    @FXML private Button        signUpButton;            // Button to navigate to sign up
    @FXML private Hyperlink     adminLoginLink;          // Link to login screen

    private final AdminAuthenticationService adminAuthService = new AdminAuthenticationService();
    private boolean passwordVisible = false;               // Tracks password visibility state

    /**
     * Initializes the controller.
     * Sets up listeners to synchronize password fields and toggle button visibility.
     */
    @FXML private void initialize() {
        System.out.println("AdminLoginController initialized");
        
        // Sync passwordField1 -> passwordVisibleField1 when hidden
        passwordField1.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!passwordVisible) {
                passwordVisibleField1.setText(newVal);
            }
            // Show toggle button only if password is not empty
            togglePasswordButton1.setVisible(newVal != null && newVal.length() > 0);
        });
        
        // Sync passwordVisibleField1 -> passwordField1 when visible
        passwordVisibleField1.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisible) {
                passwordField1.setText(newVal);
            }
        });
        
        // Initially hide the toggle button
        togglePasswordButton1.setVisible(false);
    }
    
    /**
     * Toggles the visibility of the password.
     * Switches between hidden (PasswordField) and visible (TextField) input.
     */
    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        
        if (passwordVisible) {
            // Show password
            passwordVisibleField1.setText(passwordField1.getText());
            passwordVisibleField1.setVisible(true);
            passwordField1.setVisible(false);
            // Change button icon to "hide"
            togglePasswordImageView1.setImage(new Image(getClass().getResourceAsStream("/SceneBuilder/assets/hidepassword.png")));
        } else {
            // Hide password
            passwordField1.setText(passwordVisibleField1.getText());
            passwordField1.setVisible(true);
            passwordVisibleField1.setVisible(false);
            // Change button icon to "show"
            togglePasswordImageView1.setImage(new Image(getClass().getResourceAsStream("/SceneBuilder/assets/showpassword.png")));
        }
    }

    /**
     * Handles admin login action.
     * Validates input, authenticates admin, and navigates to dashboard if successful.
     */
    @FXML
    private void handleLogin() {
        String email    = emailField1.getText().trim();
        String password = passwordVisible ? passwordVisibleField1.getText() : passwordField1.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                    "Please enter both email and password.");
            return;
        }

        try {
            System.out.println("Attempting admin login for: " + email);
            Administrator admin = adminAuthService.authenticate(email, password);
            if (admin != null) {
                System.out.println("Login successful for: " + admin.getName());
                showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                        "Welcome, " + admin.getName() + "!");
                loadAdminDashboard(admin);
            } else {
                System.err.println("Login failed - admin is null");
                showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "Invalid admin email or password.");
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Cannot connect to database.\n\nError: " + e.getMessage() +
                            "\n\nPlease check:\n" +
                            "- MySQL server is running\n" +
                            "- Database connection settings in DatabaseConnection.java");
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Login error: " + e.getMessage() + "\n\nCheck console for details.");
        }
    }

    /**
     * Loads the Admin Dashboard scene and passes the logged-in admin to its controller.
     *
     * @param admin The currently logged-in administrator
     */
    private void loadAdminDashboard(Administrator admin) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/SceneBuilder/fxml/admin/AdminDashboard.fxml"));
            Parent root = loader.load();

            // Get the controller and pass admin data
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass()
                            .getMethod("setCurrentAdmin", Administrator.class)
                            .invoke(controller, admin);
                } catch (Exception e) {
                    System.err.println("Warning: Could not set admin in dashboard: " + e.getMessage());
                }
            }

            Stage stage = (Stage) loginButton1.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 750));
            stage.setTitle("PhishNet – Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not load the admin dashboard.");
        }
    }

    /**
     * Displays an alert dialog.
     *
     * @param type    The type of alert
     * @param title   The alert title
     * @param message The alert message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles navigation to the Sign Up page.
     */
    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/fxml/auth/SignUp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton1.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle(AUTH_WINDOW_TITLE);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to navigate to sign up screen.");
        }
    }

    /**
     * Handles navigation to the regular Login page.
     */
    @FXML
    private void handleAdminLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/fxml/auth/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton1.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle(AUTH_WINDOW_TITLE);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to navigate to login screen.");
        }
    }
}