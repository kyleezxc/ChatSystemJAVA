import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    Socket socket;
    String username;
    DataInputStream dis;
    DataOutputStream dos;
    String currentRoom = "PUBLIC";

    public ClientHandler(Socket socket, String username, DataInputStream dis, DataOutputStream dos) {
        this.socket = socket;
        this.username = username;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {

        try {

            while (true) {

                String msg = dis.readUTF();

                if (msg == null) {
                    
                } else {

                    for (ClientHandler client : Server.clients) {

                        if (client.currentRoom.equals(currentRoom)) {

                            client.dos.writeUTF(
                                    "[" + currentRoom + "] " +
                                            username + ": " + msg);
                        }
                    }
                }
            }

        } catch (IOException e) {

            System.out.println(username + " disconnected");

            Server.clients.remove(this);
        }
    }
}