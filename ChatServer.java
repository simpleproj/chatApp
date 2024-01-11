import java.net.*;
import java.io.*;

public class ChatServer {
    public ChatServer(int port) throws IOException {
        ServerSocket service = new ServerSocket(port);
        try {
            while (true) {
                Socket s = service.accept();
                System.out.println("Accepted from " + s.getInetAddress());
                ChatHandler handler = new ChatHandler(s);
                handler.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            service.close();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("Syntax: ChatServer <port>");
        }
        new ChatServer(Integer.parseInt(args[0]));
    }
}