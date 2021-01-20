package Lesson_6.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class MainServ {
    private Vector<ClientHandler> clients;
    public Vector<String> onlineList;

    public MainServ() throws SQLException {
        clients = new Vector<>();
        onlineList = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();

//            String res = AuthService.getNickByLoginAndPass("login1", "pass2");
//            System.out.println(res);

            server = new ServerSocket(8181);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился!");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AuthService.disconnect();
    }

    public void broadcastMsg(ClientHandler from, String msg) {
        for (ClientHandler o: clients) {
            if(!o.checkBlacklist(from.getNick())){
                o.sendMsg(msg);
            }
        }
    }

    public void whisper(ClientHandler from, String toNick, String msg) {
        for (ClientHandler c: clients) {
            if (c.getNick().equals(toNick)) {
                c.sendMsg("From " + from.getNick() + ": " + msg);
                from.sendMsg("to " + c.getNick() + ": " + msg);
                return;
            }
        }
        from.sendMsg("Такого пользователя нет!");
    }

    public void broadcastOnline() {
        StringBuilder makeMsg = new StringBuilder().append("/listOnline ");

        for (String nick:onlineList) {
           makeMsg.append(nick + " ");
        }

        for (ClientHandler o: clients) {
            o.sendMsg(makeMsg.toString());
        }
    }

    public void subscribe(ClientHandler client) {
        onlineList.add(client.getNick());
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        onlineList.remove(client.getNick());
        clients.remove(client);
    }


    public Boolean onlineClients(String currNick) {
        if (onlineList.contains(currNick))
        {
            return true;
        }
        return false;
    }

}
