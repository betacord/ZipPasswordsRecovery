package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;


public class Controller implements Initializable {

    @FXML
    Button btnOpenFile = new Button();

    @FXML
    Button btnStart = new Button();

    @FXML
    Button btnStop = new Button();

    @FXML
    TextField tfFilePath = new TextField();

    @FXML
    TextField tfMaxLength = new TextField();

    @FXML
    TextField tfActualPass = new TextField();

    @FXML
    Label actualPassLabel = new Label();

    @FXML
    CheckBox checkShowActualPass = new CheckBox();

    @FXML
    CheckBox savePassToFile = new CheckBox();

    @FXML
    CheckBox chechaz = new CheckBox();

    @FXML
    CheckBox checkAZ = new CheckBox();

    @FXML
    CheckBox check09 = new CheckBox();

    @FXML
    CheckBox checkSpecialChars = new CheckBox();

    private String tmpDir;
    private boolean running;
    private int maxLengthOfPassword;

    @FXML
    public void openFile() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik do otwarcia");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archiwum ZIP", "*.zip"),
                new FileChooser.ExtensionFilter("Archiwum RAR", "*.rar"),
                new FileChooser.ExtensionFilter("Plik PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile == null) {
            return;
        }

        String extensionOfFile = selectedFile.getPath().substring(selectedFile.getPath().lastIndexOf("."));
        if (extensionOfFile.equals(".rar") || extensionOfFile.equals(".pdf")) {

            showDialog(Alert.AlertType.WARNING, "Funkcja niedostępna", null, "Wybrany format nie jest wspierany w tej wersji");
            return;
        }
        tfFilePath.setText(selectedFile.getPath());

        tmpDir = UUID.randomUUID().toString();
    }

    public void startForceButton() {

        String charSet = "";
        running = true;
        try {
            if (checkSpecialChars.isSelected())
                charSet += "!@#$%^&*(),.?-_=+";
            if (check09.isSelected())
                charSet += "0123456789";
            if (checkAZ.isSelected())
                charSet += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            if (chechaz.isSelected())
                charSet += "abcdefghijklmnopqrstuvwxyz";

            maxLengthOfPassword = Integer.parseInt(tfMaxLength.getText());
            if (maxLengthOfPassword > 20) {
                showDialog(Alert.AlertType.WARNING, "Błędna wartość", null, "Długość hasła nie może przekraczać 20 znaków");
                return;
            }
            if (tfFilePath.getText().length() == 0) {
                showDialog(Alert.AlertType.WARNING, "Brak pliku", null, "Plik nie został wczytany");
                return;
            }
            forcePassword(charSet.toCharArray(), tfFilePath.getText(), tmpDir);
            btnStart.setDisable(true);
            btnStop.setDisable(false);
            check09.setDisable(true);
            checkAZ.setDisable(true);
            checkSpecialChars.setDisable(true);
            chechaz.setDisable(true);
            tfMaxLength.setDisable(true);

        } catch (NumberFormatException ex) {
            showDialog(Alert.AlertType.INFORMATION, "Błędny format", null, "Wprowadzona wartość liczbowa ma błędny format");
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Błąd", "Wystąpił nieoczekiwany wyjątek", ex.toString());
        }
    }

    public void stopForceButton() {

        running = false;
        setControlsAsActive();
    }

    public void showAboutInfo() {

        showDialog(Alert.AlertType.INFORMATION, "O programie",
                "Program przeznaczony jest do odzyskiwania zapomnianych haseł z archiwów, oraz plików PDF. " +
                "Dozwolony użytek tylko i wyłącznie w granicach prawa!",
                "Kontakt: tomasz@krzywicki.pro | ©2017 Tomasz Krzywicki");
    }

    public void forcePassword(char[] charset, String filePath, String tmpDir) {

        actualPassLabel.setText("Trwa wyszukiwanie...");

        new Thread(new Runnable() {

            BruteForce bf = new BruteForce(charset, 1);
            String attempt = bf.toString();

            @Override
            public void run() {
                while (true) {

                    if (running == false) {
                        Platform.runLater(
                                () -> {
                                    tfActualPass.setText("");
                                    actualPassLabel.setText("Przerwano operację");
                                }
                        );
                        break;
                    }

                    if (PasswordHelper.checkPasswordZIP(filePath, attempt, tmpDir)) {

                        if (savePassToFile.isSelected()) {

                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(new StringSelection(attempt), null);
                        }
                        Platform.runLater(
                                () -> {
                                        actualPassLabel.setText("Znalezione hasło: ");
                                        setControlsAsActive();
                                }
                        );

                        break;
                    }
                    attempt = bf.toString();
                    if (attempt.length() - 1  == maxLengthOfPassword) {

                        running = false;
                        Platform.runLater(
                                () -> {
                                    actualPassLabel.setText("Nie znaleziono hasła");
                                    setControlsAsActive();
                                }
                        );
                        break;
                    }

                    if (checkShowActualPass.isSelected()) {

                        Platform.runLater(
                                () -> tfActualPass.setText(attempt)
                        );
                    }
                    bf.increment();
                }
                running = false;
            }
        }).start();
    }

    public void showDialog(Alert.AlertType type, String title, String header, String body) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);

        alert.showAndWait();
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        running = false;
        Platform.exit();
    }

    public void closeApplication() {

        running = false;
        Stage stage = (Stage) btnOpenFile.getScene().getWindow();
        stage.close();
    }

    private void setControlsAsActive() {

        btnStart.setDisable(false);
        btnStop.setDisable(true);
        check09.setDisable(false);
        checkAZ.setDisable(false);
        checkSpecialChars.setDisable(false);
        chechaz.setDisable(false);
        tfMaxLength.setDisable(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tfMaxLength.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    tfMaxLength.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }
}
