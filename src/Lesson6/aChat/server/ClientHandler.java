package Lesson6.aChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

class ClientHandler {

    private MainServer server;
    private Socket socket;

    private DataOutputStream out;
    private DataInputStream in;

    private String nick;

    String getNick() {
        return nick;
    }

    private Vector<String> blackList;

    boolean checkBlackList(String nick) {
        return blackList.contains(nick);
    }

    private boolean isNickBusy (Vector<ClientHandler> vch, String nick) {
        for (ClientHandler ch : vch) {
            if (ch.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    ClientHandler(MainServer server, Socket socket) {

        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new Vector<>();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // цикл для авторизации
                        while (true) {
                            String str = in.readUTF();
                            if(str.startsWith("/auth")) {
                                String[] tokens = str.split("\\s");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                if(newNick != null) {
                                    if (isNickBusy(server.getClients(), newNick)) {
                                        sendMsg("Пользователь уже авторизован!");
                                    } else {
                                        sendMsg("/authok");
                                        nick = newNick;
                                        server.subscribe(ClientHandler.this);
                                        server.broadCastMsg(ClientHandler.this, nick + " зашёл в чат");
                                        break;
                                    }
                                } else {
                                    sendMsg("Неверный логин/пароль!");
                                }
                            }
                        }
                        // цикл для работы
                        while (true) {
                            String str = in.readUTF();
                            // блок служебных команд
                            if (str.startsWith("/")) {
                                if(str.equals("/end")) {
                                    out.writeUTF("/serverClosed");
                                    break;
                                }
                                if (str.startsWith("/w ")) {
                                    String[] tokens = str.split("\\s", 3);
                                    server.privateMsg(ClientHandler.this, tokens[1], tokens[2]);
                                }
                                if (str.startsWith("/blacklist ")) {
                                    String[] tokens = str.split("\\s");
                                    blackList.add(tokens[1]);
                                    sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                                }
                            } else {
                                server.broadCastMsg(ClientHandler.this,nick + ": " + str);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        //e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                        server.broadCastMsg(ClientHandler.this, nick + " покинул чат");
                        System.out.println(nick + " покинул чат");
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
