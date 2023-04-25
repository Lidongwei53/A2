package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.Socket;

public class Main extends Application {
    public Controller controller;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));

        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
        this.controller = fxmlLoader.getController();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (controller!=null){
                    try {
                        controller.stopThread();
                        Platform.runLater(()-> {
                            try {
                                controller.socket.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        });

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
    }
}
