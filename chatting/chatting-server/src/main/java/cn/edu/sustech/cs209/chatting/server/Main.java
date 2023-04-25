package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static List<String> userList;
    public static List<Socket> socketList;
    public static Map<Socket, String> userMap;
    public ServerSocket serverSocket;


    public Main() throws IOException {
        serverSocket = new ServerSocket(5533);
        userList = new ArrayList<>();
        socketList = new ArrayList<>();

        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            try {
                for (Socket socket:socketList){
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    Message message = new Message(null, null, null, null, 53);
                    oos.writeObject(message);
                }
                stopServer();
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
        }));

    }

    public void run() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            Handler hd = new Handler(socket);
            new Thread(hd).start();
            /*ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message message0 = (Message) ois.readObject();
            userList.add(message0.getSentBy());
            socketList.add(socket);*/

        }
    }

    public void stopServer() throws IOException {
        for (Socket socket :socketList){
            socket.close();
        }
        serverSocket.close();

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Main main = new Main();
        main.run();


    }
}

class Handler implements Runnable{
    public Socket socket;

    public Handler(Socket socket) {
        this.socket=socket;
    }


    public void sendAllUserListChange() throws IOException {
        for (int i =0; i<Main.socketList.size(); i++){
            ObjectOutputStream oos = new ObjectOutputStream(Main.socketList.get(i).getOutputStream());
            String j = "";
            for(int k = 0;k < Main.userList.size();k++) {
                    j = j + Main.userList.get(k) + ",";

            }
            Message message1 = new Message(null, null, null, j, 2);
            oos.writeObject(message1);


        }
    }

    @Override
    public void run() {
        try {
            while (true) {


                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();
                switch (message.getMessageType()) {
                    case 1: {
                        int flag = 0;
                        for (int i = 0; i < Main.userList.size(); i++) {
                            if (Main.userList.get(i).equals(message.getSentBy())) {
                                flag = 1;
                                break;
                            }

                        }
                        if (flag == 0) {
                            if (message.getSentBy() != null) {
                                Main.userList.add(message.getSentBy());
                                Main.socketList.add(socket);
                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                Message message1 = new Message(null, null, null, String.valueOf(Main.userList.size()), 0);
                                oos.writeObject(message1);
                                sendAllUserListChange();
                            }
                        }
                        if (flag == 1) {
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            Message message1 = new Message(null, null, null, null, -1);
                            oos.writeObject(message1);

                        }
                        break;

                    }

                    case 2: {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        String j = "";
                        for (int i = 0; i < Main.userList.size(); i++) {
                            if (!Main.userList.get(i).equals(message.getSentBy())){
                            j = j + Main.userList.get(i) + ",";
                            }
                        }
                        Message message1 = new Message(null, null, null, j, 2);
                        oos.writeObject(message1);
                        break;
                    }

                    case -5:{
                        sendAllUserListChange();
                        break;
                    }

                    case 6:{
                        Socket socket1 = null;
                        String sendTo = message.getSendTo();
                        for (int i=0; i<Main.userList.size(); i++){
                            if (Main.userList.get(i).equals(sendTo)){
                                socket1 = Main.socketList.get(i);
                                break;
                            }
                        }
                        if (socket1!=null){
                        ObjectOutputStream oos = new ObjectOutputStream(socket1.getOutputStream());
                        Message message1 = new Message(null, message.getSentBy(), sendTo,null,7);
                        oos.writeObject(message1);
                        }
                        break;
                    }

                    case 5:{

                            Socket socket1= null;
                            for (int i=0; i<Main.userList.size(); i++){
                                if (Main.userList.get(i).equals(message.getSendTo())){
                                    socket1 = Main.socketList.get(i);
                                    break;
                                }
                            }
                            if (socket1!=null){
                                try {
                                    ObjectOutputStream oos = new ObjectOutputStream(socket1.getOutputStream());
                                    oos.writeObject(message);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                            break;
                    }
                    case 13: {
                        String user = message.getSentBy();
                        for (int i = 0; i < Main.userList.size(); i++) {
                            if (user.equals(Main.userList.get(i))) {
                                Main.userList.remove(i);
                                Main.socketList.remove(i);
                                break;
                            }
                        }
                        sendAllUserListChange();
                        break;
                    }
                    case 9:{
                        List<String> lst = message.getGroupList();
                        for (int i=0; i<lst.size(); i++){
                            if (!lst.get(i).equals(message.getSentBy())){
                                for (int j=0; j<Main.userList.size(); j++){
                                    if (lst.get(i).equals(Main.userList.get(j))){
                                        Socket socket1 = Main.socketList.get(j);
                                        ObjectOutputStream oos = new ObjectOutputStream(socket1.getOutputStream());
                                        oos.writeObject(message);
                                    }

                                }

                            }

                        }

                        break;
                    }
                    case 17:{
                        List<String> lst = message.getGroupList();
                        for (int i=0; i<lst.size(); i++){
                            if (!lst.get(i).equals(message.getSentBy())){
                                for (int j=0; j<Main.userList.size(); j++){
                                    if (lst.get(i).equals(Main.userList.get(j))){
                                        Socket socket1 = Main.socketList.get(j);
                                        ObjectOutputStream oos = new ObjectOutputStream(socket1.getOutputStream());
                                        oos.writeObject(message);
                                    }

                                }

                            }

                        }
                        break;

                    }

                }
            }

        } catch (IOException | ClassNotFoundException e) {
            //throw new RuntimeException(e);
        }



    }
}



