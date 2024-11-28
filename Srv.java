import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;


public class Srv {
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static Connection connection;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5555)) {
            System.out.println("Waiting for connection...");
            initializeDatabase();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test_bd", "root", "pass");
            try (Statement stmt = connection.createStatement()) {
                String createTable = "CREATE TABLE IF NOT EXISTS messages (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "message TEXT NOT NULL);";
                stmt.execute(createTable);
            }
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void Message(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
        saveMessageToDatabase(message);
    }

    private static void saveMessageToDatabase(String message) {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO messages (message) VALUES (?);")) {
            pstmt.setString(1, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Welcome server!");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    Srv.Message(message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                System.out.println("Client disconnected.");
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
