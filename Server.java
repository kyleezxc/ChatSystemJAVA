import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    static ArrayList<ClientHandler> clients = new ArrayList<>();
    static Map<String, ArrayList<ClientHandler>> rooms = new HashMap<>();

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(5000);

        System.out.println("Chat Server Started...");

        while (true) {

            Socket socket = serverSocket.accept();

            System.out.println("New client connected");

            DataInputStream dis =
                    new DataInputStream(socket.getInputStream());

            DataOutputStream dos =
                    new DataOutputStream(socket.getOutputStream());

            String username = dis.readUTF();

            ClientHandler client =
                    new ClientHandler(socket, username, dis, dos);

            clients.add(client);

            Thread t = new Thread(client);
            t.start();

            broadcast("SERVER: " + username + " joined chat");
        }
    }

    public static void broadcast(String message) {

        for (ClientHandler client : clients) {

            try {
                client.dos.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ClientHandler findClient(String username) {

        for (ClientHandler client : clients) {

            if (client.username.equals(username)) {
                return client;
            }
        }

        return null;
    }
}