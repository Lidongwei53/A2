package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Listener implements Runnable{
    public Socket socket;
    public String username;
    public Controller controller;
    public volatile boolean running = true;

    public Listener(Socket socket, String username,Controller controller){
        this.socket=socket;
        this.username=username;
        this.controller = controller;

    }

    public void shouldStop(){
        running=false;
    }

    @Override
    public void run() {


        /*Thread socketCheckThread = new Thread(()->{
            int flag = 0;
            while(flag==0){
                try {
                    Thread.sleep(100);
                    if(!socket.isConnected()){
                        Platform.runLater(()->{
                            controller.serverClose();
                        });
                        flag=1;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
            Thread.interrupted();
        });
        socketCheckThread.start();*/

        while((!Thread.interrupted())&&(!socket.isClosed())){
            try {

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();
                switch (message.getMessageType()){


                    case 2:{
                        controller.userList.clear();
                        String j = message.getData();
                        String[]t = j.split(",");
                        for (int k =0; k<t.length; k++){
                            if (!username.equals(t[k])){
                            controller.userList.add(t[k]);
                            }
                        }
                        Platform.runLater(()->{
                            controller.currentOnlineCnt.setText("Online: "+t.length);
                            controller.updateGroupChatList();

                        });
                        break;
                    }

                    case 5:{
                        Platform.runLater(()->{
                            if (!controller.userMessageMap.isEmpty()) {
                                List<Message> lst;
                                lst = controller.userMessageMap.get(message.getSentBy());
                                lst.add(message);
                                ObservableList<Message> obs = FXCollections.observableArrayList();
                                for (int i = 1; i < lst.size(); i++) {
                                    obs.add(lst.get(i));
                                }
                                controller.userMessageMap.put(message.getSentBy(),lst);
                                if (message.getSentBy().equals(controller.userSendTo)){
                                    controller.chatContentList.setItems(FXCollections.observableArrayList(obs));
                                }

                            }
                            controller.notifyMessage(message);
                        });
                        break;
                    }
                    case 7:{
                        if (message.getSentBy()!=null){
                            Platform.runLater(()->{
                                /*controller.userUIList.add(message.getSentBy());
                                controller.chatList.setItems(controller.userUIList);
                                controller.updateUserListCell();*/
                                int flag =0;
                                for (int i=0; i<controller.userUIList.size(); i++){
                                    if (controller.userUIList.get(i).equals(message.getSentBy())){
                                        flag=1;
                                        break;
                                    }
                                }
                                if (flag==0){
                                    controller.userUIList.add(message.getSentBy());
                                    List<Message> UML = new ArrayList<>();
                                    UML.add(new Message(null,null,null,"Anchor",-10));
                                    controller.userMessageMap.put(message.getSentBy(), UML);
                                    controller.chatList.setItems(controller.userUIList);
                                    controller.updateUserListCell();
                                    System.out.println(controller.userUIList);
                                }

                            });
                        }
                        break;
                    }
                    case 9:{
                        Platform.runLater(()-> {
                            String str = message.getSendTo();
                            List<String> lst = message.getGroupList();
                            controller.userUIList.add(message.getSendTo());
                            List<Message> UML = new ArrayList<>();
                            UML.add(new Message(null,null,null,"Anchor",-10));
                            controller.userMessageMap.put(str,UML);
                            controller.groupSelectMap.put(str,lst);
                            controller.chatList.setItems(controller.userUIList);
                            controller.updateUserListCell();
                            controller.updateGroupChatList();
                        });


                        break;

                    }
                    case 17:{
                        Platform.runLater(()->{
                            if (!controller.userMessageMap.isEmpty()) {
                                String str = message.getSendTo();
                                List<Message> lst;
                                lst = controller.userMessageMap.get(str);
                                lst.add(message);
                                ObservableList<Message> obs = FXCollections.observableArrayList();
                                for (int i = 1; i < lst.size(); i++) {
                                    obs.add(lst.get(i));
                                }
                                controller.userMessageMap.put(str,lst);
                                if (message.getSendTo().equals(controller.userSendTo)){
                                    controller.chatContentList.setItems(FXCollections.observableArrayList(obs));
                                }

                            }
                            controller.notifyMessage(message);
                        });
                        break;


                    }
                    case 53:{
                        Platform.runLater(()-> controller.serverClose());
                        break;
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                //throw new RuntimeException(e);
            }


        }


    }
}
