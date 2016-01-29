package dclab1server;

import java.io.*;
import java.net.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

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

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream out = new BufferedOutputStream(socket.getOutputStream());
                PrintStream pout = new PrintStream(out);

                //read first line of request (ignore the rest)
                while (!in.ready()){
                    try {
                        //nothing
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DCLab1Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                String request = in.readLine();
                if (request == null) {
                    continue;
                }

                while (true) {
                    String x = in.readLine();
                    if (x == null || x.length() == 0) {
                        break;
                    }
                }

                if (request.startsWith("cur")) {
                    System.out.println("got CUR");
                    pout.print("you send me CUR");
                    //out.write("you send me CUR".getBytes());
                } else {
                    System.out.println("got something else");
                    out.write("got something else".getBytes());
                }
                out.flush();

            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

}
