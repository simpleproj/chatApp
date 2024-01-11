import java.net.*;
import java.io.*;
import java.util.*;

public class ChatHandler extends Thread {
    protected Socket socket;
    protected DataInputStream inStream;
    protected DataOutputStream outStream;
    protected boolean isOn;
    protected static List<ChatHandler> handlers = Collections.synchronizedList(new ArrayList<ChatHandler>());

    public ChatHandler(Socket s) throws IOException {
        socket = s;
        inStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        outStream = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    public void run() {
        isOn = true;
        try {
            handlers.add(this);
            while (isOn) {
                String msg = inStream.readUTF();
                broadcast(msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            handlers.remove(this);
            try {
                outStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected static void broadcast(String message) {
        synchronized (handlers) {
            Iterator<ChatHandler> itr = handlers.iterator();
            while (itr.hasNext()) {
                ChatHandler currentClient = itr.next();
                try {
                    synchronized (currentClient.outStream) {
                        currentClient.outStream.writeUTF(message);
                    }
                    currentClient.outStream.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    currentClient.isOn = false;
                }
            }
        }
    }
}