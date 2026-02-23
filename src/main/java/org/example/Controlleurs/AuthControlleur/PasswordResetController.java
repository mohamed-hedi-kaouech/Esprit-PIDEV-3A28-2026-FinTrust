package org.example.Controlleurs.AuthControlleur;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Service.UserService.UserService;
import org.example.Service.UserService.PasswordResetResult;
import org.example.Service.EmailService;

public class PasswordResetController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label codeLabel;
    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();
    private String sentCode;

    @FXML
    private void handleSendCode() {
        String email = emailField.getText();
        sentCode = emailService.sendPasswordResetCode(email);
        codeLabel.setText("Code envoyé à " + email);
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText();
        String code = codeField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (!code.equals(sentCode)) {
            messageLabel.setText("Code incorrect.");
            return;
        }
        PasswordResetResult result = userService.resetPassword(email, newPassword, confirmPassword);
        if (!result.isSuccess()) {
            messageLabel.setText(result.getMessage());
            return;
        }
        messageLabel.setText("Mot de passe réinitialisé avec succès. Vous pouvez vous connecter.");
        // Optionally, navigate to login page
    }
}
