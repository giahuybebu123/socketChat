import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 9999;
    private static final Map<Socket, String> clientMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Tạo một luồng mới để xử lý kết nối của client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("Enter your username:");
                username = in.readLine();
                broadcast(username + " has joined the chat.", null);
                clientMap.put(clientSocket, username);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    broadcast(username + ": " + inputLine, clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientMap.remove(clientSocket);
                broadcast(username + " has left the chat.", null);
            }
        }

        private void broadcast(String message, Socket excludeSocket) {
            for (Socket socket : clientMap.keySet()) {
                if (socket != excludeSocket) {
                    PrintWriter writer;
                    try {
                        writer = new PrintWriter(socket.getOutputStream(), true);
                        writer.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
