package Lesson_6.Client;
import javafx.application.Platform;
//import com.sun.media.jfxmediaimpl.platform.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller {
    @FXML
    TextArea textArea;

    @FXML
    ListView<String> listOnline;

    @FXML
    TextField textField;

    @FXML
    Button btn1;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADRESS = "localhost";
    final int PORT = 8181;

    private boolean isAuthorized;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField loginField;

    @FXML
    TextField loginRegistrationField;

    @FXML
    TextField nicknameField;

    @FXML
    PasswordField passwordField;

    @FXML
    TextField passwordRegistrationField;

    private void setAuthrized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
        }
        textArea.clear();
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/authok")) {
                                setAuthrized(true);
                                break;
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if(str.startsWith("/")){
                                if (str.equalsIgnoreCase("/clientClose")) {
                                    listOnline.getItems().clear();
                                    break;
                                }
                                if (str.startsWith("/listOnline")) {
                                    String[] tokens = str.split(" ");

                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run () {
                                            listOnline.getItems().clear();

                                            for (int i = 1; i < tokens.length; i++) {
                                                listOnline.getItems().add(tokens[i]);
                                            }
                                        }
                                    });
                                }
                            }
                            else {
                                textArea.appendText(str + "\n");
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthrized(false);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Dispose() {
        System.out.println("Отправляем сообщение на сервер о завершении работы");
        try {
            if (out != null) {
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/reg " + loginRegistrationField.getText() + " " + passwordRegistrationField.getText() + " " + nicknameField.getText());
            loginRegistrationField.clear();
            passwordRegistrationField.clear();
            nicknameField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SetOnlineList (){

    }
}
