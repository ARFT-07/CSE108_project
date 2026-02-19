package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;


public class LoginController {
    @FXML
//    TextField name;
//    TextField pass;

    public boolean checkMatchingPairs(String name, String pass) {

        try (Scanner scanner = new Scanner(
                getClass().getResourceAsStream(
                        "/org/buet/fantasymanagerxi/data/ValidLoginInfo.txt"
                )
        )) {


            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(" ");
                String firstpart = parts[0];
                String secondpart = parts[1];

                if (firstpart.equals(name) && secondpart.equals(pass)) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void checkValidLogin(ActionEvent actionEvent) throws IOException {
        String input1 = ((TextField) ((Node) actionEvent.getSource()).getScene().lookup("#name")).getText();
        String input2 = ((TextField) ((Node) actionEvent.getSource()).getScene().lookup("#pass")).getText();
        // String input2 = pass.getText();
        if (!Objects.equals(input1, "") && !Objects.equals(input2, "")) {
            if (checkMatchingPairs(input1, input2)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/buet/fantasymanagerxi/fxml/hello-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                throw new IOException("Invalid Login");
            }
        } else {
            throw new IOException("Please Fillup the Necessary Information");
        }
    }
}
