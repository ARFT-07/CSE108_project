package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.util.Scanner;

public class SignupController {

    private final String FILE_NAME = "ValidAccountinfo.txt";

    // 🔹 Create Account
    public void CreateAccpunt(ActionEvent actionEvent) throws IOException {

        Scene scene = ((Node) actionEvent.getSource()).getScene();

        String username = ((TextField) scene.lookup("#name")).getText().trim();
        String password = ((PasswordField) scene.lookup("#pass")).getText().trim();
        String confirmPassword = ((PasswordField) scene.lookup("#confirmPass")).getText().trim();

        // 1️⃣ Check empty
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill all fields.");
            return;
        }

        // 2️⃣ Check password match
        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        File file = new File(FILE_NAME);

        // 3️⃣ Create file if not exists
        if (!file.exists()) {
            file.createNewFile();
        }

        // 4️⃣ Duplicate username check
        if (isDuplicateUsername(username)) {
            showAlert("Error", "Username already exists.");
            return;
        }

        // 5️⃣ Append user
        FileWriter writer = new FileWriter(file, true);
        writer.write(username + " " + password + System.lineSeparator());
        writer.close();

        showAlert("Success", "Account created successfully!");

        returnToPrevScene(actionEvent);
    }

    // 🔹 Check duplicate username
    private boolean isDuplicateUsername(String username) throws FileNotFoundException {

        File file = new File(FILE_NAME);
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(" ");
            if (parts.length > 0 && parts[0].equals(username)) {
                scanner.close();
                return true;
            }
        }

        scanner.close();
        return false;
    }

    // 🔹 Return to login scene
    public void returnToPrevScene(ActionEvent actionEvent) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/org/buet/fantasymanagerxi/fxml/loginsignupview.fxml"
                )
        );

        Parent root = loader.load();

        Stage stage = (Stage) ((Node) actionEvent.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

    // 🔹 Alert helper
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
