package Lesson_6.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private MainServ serv;

    private String nick;
    private Vector<String> blacklist = new Vector<>();

    public ClientHandler(MainServ serv, Socket socket) {
        try {
            this.serv = serv;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String currentNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);

                                if (currentNick != null){
                                    if (serv.onlineClients(currentNick)) {
                                        sendMsg("Данный пользователь уже в сети");
                                    } else {
                                        sendMsg("/authok");
                                        nick = currentNick;
                                        serv.subscribe(ClientHandler.this);
                                        serv.broadcastOnline();
                                        downloadBlacklist(AuthService.getBlacklist(nick));
                                        break;
                                    }
                                } else {
                                    sendMsg("неверный логин/пароль");
                                }
                            }

                            if (str.startsWith("/reg")){
                                String[] tokens = str.split(" ");

                                if(AuthService.RegistrationNewUser(tokens[1], tokens[2], tokens[3])){
                                    sendMsg("Регистрация прошла успешно");
                                } else {
                                    sendMsg("Логин или Никнейм уже существуют");
                                }
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.equalsIgnoreCase("/end")) {
                                    sendMsg("/clientClose");
                                    break;
                                }
                                if (str.startsWith("/w")) {
                                    StringBuilder whisperMsg = new StringBuilder().append(str);
                                    String[] tokens = str.split(" ");
                                    whisperMsg.delete(0,tokens[1].length() + 4);
                                    serv.whisper(ClientHandler.this, tokens[1],whisperMsg.toString());
                                }
                                if (str.startsWith("/block")) {
                                    String nickForBlock = str.split(" ")[1];
                                    if (!serv.onlineClients(nickForBlock)) {
                                        sendMsg("Пользователя не существует");
                                    } else {
                                        if (nickForBlock.equals(nick)) {
                                            sendMsg("Нельзя себя заблокировать!");
                                        } else {
                                            blacklist.add(nickForBlock);
                                            sendMsg("Пользователь заблокирован");
                                        }
                                    }
                                }
                            }
                            else {
                                serv.broadcastMsg(ClientHandler.this,nick + ": " + str);
                            }
                        }

                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        serv.unsubscribe(ClientHandler.this);
                        serv.broadcastOnline();
                        try {
                            AuthService.updateBlacklist(nick, makeStrBlacklist());
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick(){
        return this.nick;
    }

    public boolean checkBlacklist(String nickSender) {
        return blacklist.contains(nickSender);
    }

    public void downloadBlacklist(String strblacklist){
        if (strblacklist.equals("")) {
            return;
        }
        String[] tokens = strblacklist.split(" ");

        blacklist.addAll(Arrays.asList(tokens));
    }

    private String makeStrBlacklist() {
        StringBuilder list = new StringBuilder();

        for (String blocked: blacklist) {
            list.append(" ").append(blocked);
        }
        return list.toString();
    }
}
