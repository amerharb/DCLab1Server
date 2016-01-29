package dclab1server;

import java.io.*;
import java.net.*;
import java.io.File;

public class DCLab1Server
{

    private static final int PORT = 8000;
    private ServerSocket serverSocket;

    public static void main(String[] args)
    {
        int port = PORT;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        new DCLab1Server(port);
    }

    public DCLab1Server(int port)
    {
        File file = new File(".");

        // create a server socket
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Error in creation of the server socket");
            System.exit(0);
        }

        System.out.println("Server is Started ...");

        while (true) {
            try {
                // listen for a connection
                Socket socket = serverSocket.accept();

                System.out.println("Got request from " + socket.getInetAddress());
                
                // create data input/output streams
                DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

                //outputToClient.writeChars("Test");
                outputToClient.writeUTF("Test");

            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

}
