package cn.edu.sustech.cs209.chatting.client;
import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class Controller implements Initializable {

    public static ObservableList<String> userList;
    @FXML
    public Label currentUsername;
    public Label currentOnlineCnt;
    public ListView<String> chatList;
    public String userSendTo;
    public ListView<String> groupChatList;
    public Map<String, List<String>> groupSelectMap;
    public Map<String, List<Message>> userMessageMap;
    public ObservableList<String> userUIList;

    public TextArea inputArea;
    public Thread listenerThread;
    public int sendMode;
    @FXML
    ListView<Message> chatContentList;
    Socket socket;
    String username;




    public void serverClose(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Server is closed!");
        alert.setHeaderText(null);
        alert.setContentText("Server is closed!");
        alert.showAndWait();
        System.exit(0);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userList = FXCollections.observableArrayList();
        userMessageMap=new HashMap<>();

        groupSelectMap = new HashMap<>();

        userSendTo = "";
        sendMode = 0;
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();


        if (input.isPresent() && !input.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
            int flag = 0;
            while (flag == 0) {

            username = input.get();


            try {
                socket = new Socket("localhost", 5533);
                Message message0 = new Message(null, username, null, null, 1);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(message0);

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message1 = (Message) ois.readObject();

                flag = message1.getMessageType()+1;
                if(flag==0){

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Invalid username!");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid username!");
                    alert.showAndWait();
                    dialog = new TextInputDialog();
                    dialog.setTitle("Login");
                    dialog.setHeaderText(null);
                    dialog.setContentText("Username:");
                    input = dialog.showAndWait();
                }



            } catch (IOException | ClassNotFoundException | NoSuchElementException e) {
                //throw new RuntimeException(e);
            }
            }
            userUIList = FXCollections.observableArrayList();


            Listener listener = new Listener(socket,username,this);
            listenerThread= new Thread(listener);
            listenerThread.start();

            currentUsername.setText("CURRENT USER:" + username);

            Message message0 = new Message(null, username, null, null, -5);

            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(message0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            Platform.exit();

        }

        chatContentList.setCellFactory(new MessageCellFactory());




    }

    public void stopThread() throws IOException {
        Platform.runLater(()->{

            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                Message message = new Message(null,username,null,"Quit",13);
                oos.writeObject(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            listenerThread.interrupt();

        });


    }

    public void updateGroupChatList(){
        if(sendMode==2){
            groupChatList.getItems().clear();
            if(!groupSelectMap.get(userSendTo).isEmpty()){
                List<String> GPLST = groupSelectMap.get(userSendTo);
                List<String> fnLSt = new ArrayList<>();


                for (int i=0;i<GPLST.size();i++){
                    int flag = 0;
                    if(userList.size()==0){
                        fnLSt.add(username);
                        break;
                    }
                    else{
                        for (int j =0; j< userList.size(); j++){
                            if(GPLST.get(i).equals(userList.get(j))||GPLST.get(i).equals(username)){
                                flag=1;
                                break;
                            }
                        }
                    }

                    if(flag==1){
                        fnLSt.add(GPLST.get(i));
                    }
                }
                ObservableList<String> obs = FXCollections.observableArrayList(fnLSt);
                groupChatList.setItems(obs);
            }
        }
        else{
            List<String> lst = new ArrayList<>();
            int flag=0;
            for(int i=0;i<userList.size();i++){
                if(userList.get(i).equals(userSendTo));
                flag=1;
                break;
            }
            if(flag==1){
                lst.add(userSendTo);
            }
            if(!lst.isEmpty()){
                ObservableList<String> obs = FXCollections.observableArrayList(lst);
                groupChatList.setItems(obs);
            }
            else{
                groupChatList.getItems().clear();
            }

        }
    }

    public void notifyMessage(Message message){
        String USB = message.getSentBy();
        String UST = message.getSendTo();
        String txt = "";
        switch (message.getMessageType()){
            case 5:{
                txt = USB+" says: "+message.getData();
                break;

            }
            case 17: {
                txt = UST + ": " + USB + " says: " + message.getData();
                break;
            }
        }

        final Stage[] stage = {new Stage()};
        stage[0].setWidth(250);
        stage[0].setHeight(100);
        Label label = new Label(txt);
        label.setFont(new Font(15));
        Scene scene = new Scene(label);
        label.setAlignment(Pos.CENTER);
        stage[0].setScene(scene);


        stage[0].show();
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e->{
            stage[0].close();
            stage[0] =null;
        });
        delay.play();



    }

    @FXML
    public void createPrivateChat() throws IOException, ClassNotFoundException {
        AtomicReference<String> user = new AtomicReference<>();
        /*Message message0 = new Message(null,username,null,null,2);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(message0);*/

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>(userList);


        // FIXME: get the user list from server, the current user's name should be filtered out
        //userSel.getItems().addAll("Item 1", "Item 2", "Item 3");

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
            String u = user.get();
            if(u!=null) {
                int flag = 0;
                for (int i = 0; i < userUIList.size(); i++) {
                    if (u.equals(userUIList.get(i))) {
                        flag = 1;
                        chatList.getSelectionModel().select(userUIList.get(i));
                        userSendTo = u;
                        chatContentList.getItems().clear();
                        if (!userMessageMap.get(userSendTo).isEmpty()) {
                            ObservableList<Message> obs = FXCollections.observableArrayList();
                            for (int j = 1; j < userMessageMap.get(userSendTo).size(); j++) {
                                obs.add(userMessageMap.get(userSendTo).get(j));
                            }
                            chatContentList.setItems(FXCollections.observableArrayList(obs));
                        }
                        break;
                    }
                }
                if(flag == 0) {
                    if(u != null) {
                        userUIList.add(u);
                        List<Message> UML = new ArrayList<>();
                        UML.add(new Message(null,null,null,"Anchor",-10));
                        userMessageMap.put(u, UML);
                        chatList.setItems(userUIList);
                        chatList.setCellFactory(new userCellFactory(this));
                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            Message message = new Message(null,username,u,null,6);
                            oos.writeObject(message);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                    }
                }
            }

        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();




        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
        //AtomicReference<String> user = new AtomicReference<>();
        VBox checkBoxes = new VBox();
        Stage stage = new Stage();
        List<String> selectedItems = new ArrayList<>();

        for(int i =0;i< userList.size();i++) {
            String item = userList.get(i);
            CheckBox checkBox = new CheckBox(item);
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    selectedItems.add(item);
                } else {
                    selectedItems.remove(item);
                }
            });
            checkBoxes.getChildren().add(checkBox);
        }

            Button okBtn = new Button("OK");
            okBtn.setOnAction(e -> {
                //user.set(selectedItems.toString());
                stage.close();
                if(!selectedItems.isEmpty()){
                    selectedItems.add(username);
                    List<String> finalItems = selectedItems.stream().sorted().collect(Collectors.toList());
                    String str="";
                    if(finalItems.size()<3){
                        str = str+ finalItems.get(0)+", "+finalItems.get(1)+" (2)";

                    }
                    else{
                        str = str+ finalItems.get(0)+", "+finalItems.get(1)+", "+finalItems.get(2)+"..."+" ("+finalItems.size()+")";
                    }
                    Message message = new Message(null,null,null,"Anchor",-10);
                    List<Message> UML = new ArrayList<>();
                    UML.add(message);
                    userUIList.add(str);
                    userMessageMap.put(str,UML);
                    groupSelectMap.put(str,finalItems);
                    Message message1 = new Message(null,username,str,"Group",9);
                    message1.setGroupList(finalItems);
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(message1);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    chatList.setItems(userUIList);
                    chatList.setCellFactory(new userCellFactory(this));
                    //ObservableList<String> obs = FXCollections.observableArrayList(finalItems);
                    //groupChatList.setItems(obs);

                }

            });



        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(checkBoxes, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();




    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() throws IOException {
        String msg = inputArea.getText();
        inputArea.clear();
        if(!msg.isEmpty()&&!msg.trim().isEmpty()){
            switch (sendMode){
                case 1:{
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    Message message = new Message(null,username,userSendTo,msg,5);
                    oos.writeObject(message);

                    if(!userMessageMap.get(userSendTo).isEmpty()){
                        userMessageMap.get(userSendTo).add(message);
                        ObservableList<Message> obs = FXCollections.observableArrayList();
                        for(int i=1;i<userMessageMap.get(userSendTo).size();i++){
                            obs.add(userMessageMap.get(userSendTo).get(i));
                        }
                        chatContentList.setItems(FXCollections.observableArrayList(obs));
                    }
                    break;
                }

                case 2:{
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    Message message = new Message(null,username,userSendTo,msg,17);
                    message.setGroupList(groupSelectMap.get(userSendTo));
                    oos.writeObject(message);

                    if(!userMessageMap.get(userSendTo).isEmpty()){
                        userMessageMap.get(userSendTo).add(message);
                        ObservableList<Message> obs = FXCollections.observableArrayList();
                        for(int i=1;i<userMessageMap.get(userSendTo).size();i++){
                            obs.add(userMessageMap.get(userSendTo).get(i));
                        }
                        chatContentList.setItems(FXCollections.observableArrayList(obs));
                    }

                    break;

                }

            }





        }



        // TODO
    }

    public void updateUserListCell(){
        chatList.setCellFactory(new userCellFactory(this));
    }



    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    public class userCellFactory implements Callback<ListView<String>,ListCell<String>>{
        public Controller controller;

        public userCellFactory(Controller controller){
            this.controller=controller;

        }
        @Override
        public ListCell<String> call(ListView<String> param) {
            ListCell<String> cell = new ListCell<String>(){

                @Override
                public void updateItem(String str, boolean bln){
                    super.updateItem(str,bln);
                    setText(null);

                    if (str != null) {
                        HBox hBox = new HBox();
                        Text name = new Text(str);
                        hBox.getChildren().addAll(name);
                        hBox.setAlignment(Pos.CENTER_LEFT);
                        setGraphic(hBox);
                    }
                }
            };
            cell.setOnMouseClicked(event -> {
                if(!cell.isEmpty()){
                    if(controller.groupSelectMap.containsKey(cell.getItem())){
                        controller.userSendTo=cell.getItem();
                        controller.sendMode=2;
                        controller.updateGroupChatList();
                    }
                    else{
                        controller.userSendTo=cell.getItem();
                        controller.sendMode=1;
                        controller.updateGroupChatList();
                    }


                    controller.chatContentList.getItems().clear();
                    if(!controller.userMessageMap.get(controller.userSendTo).isEmpty()){
                        ObservableList<Message> obs = FXCollections.observableArrayList();
                            for(int i=1;i<controller.userMessageMap.get(controller.userSendTo).size();i++){
                                obs.add(controller.userMessageMap.get(controller.userSendTo).get(i));
                            }
                            controller.chatContentList.setItems(FXCollections.observableArrayList(obs));
                        }
                    }



            });

            return cell;
        }
    }



}
