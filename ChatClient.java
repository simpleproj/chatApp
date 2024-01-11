import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChatClient extends JFrame implements Runnable {
    protected Socket socket;
    protected DataInputStream inStream;
    protected DataOutputStream outStream;
    protected JTextArea outTextArea;
    protected JTextField inTextField;
    protected boolean isOn;

    public ChatClient(String title, Socket s, DataInputStream dis, DataOutputStream dos) {
        super(title);
        socket = s;
        inStream = dis;
        outStream = dos;

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(BorderLayout.CENTER, outTextArea = new JTextArea());
        outTextArea.setEditable(false);
        cp.add(BorderLayout.SOUTH, inTextField = new JTextField());

        inTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    outStream.writeUTF(inTextField.getText());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    isOn = false;
                }
                inTextField.setText("");
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                isOn = false;
                try {
                    outStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setVisible(true);
        inTextField.requestFocus();
        (new Thread(this)).start();

    }

    public void run() {
        isOn = true;
        try {
            while (isOn) {
                String line = inStream.readUTF();
                outTextArea.append(line + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            inTextField.setVisible(false);
            validate();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new RuntimeException("Syntax: ChatClient <host> <port>");
        }
        Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            new ChatClient("Chat " + args[0] + ":" + args[1], socket, dis, dos);
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                if (dis != null) {
                    dos.close();
                }
            } catch (IOException ex2) {
                ex.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException ex3) {
                ex3.printStackTrace();
            }
        }
    }
}