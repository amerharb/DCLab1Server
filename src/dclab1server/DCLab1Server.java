package dclab1server;

import java.io.*;
import java.net.*;
import java.io.File;

public class DCLab1Server
{

    private static final int PORT = 8000;
    private ServerSocket serverSocket = null;
    private boolean shutdownServer = false; // this variable desigen to be changed from diffreant thread 

    public static void main(String[] args) throws InterruptedException
    {
        int port = PORT;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        new DCLab1Server(port);
    }

    public DCLab1Server(int port) throws InterruptedException
    {
        File file = new File(".");

        // create a server socket
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Error in creation of the server socket");
            System.exit(-1);
        }

        System.out.println("Server is Started on port : " + port);

        Socket socket;
        BufferedReader in;
        OutputStream out;
        PrintStream pout;

        try {
            SERVER_CONN:
            while (true) { //wait for connection 
                socket = null;
                try {
                    // listen for a connection, only to 1 connection at the time
                    socket = serverSocket.accept();
                    System.out.println("Got request from " + socket.getInetAddress());

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new BufferedOutputStream(socket.getOutputStream());
                    pout = new PrintStream(out);

                    while (true) { //wait for input from clinet

                        while (!in.ready()) { //sleep until clinet send data or close socket
                            if (socket.isClosed() || socket.) {
                                continue SERVER_CONN; //wait for next connection
                            }
                            Thread.sleep(2000);
                        }

                        String request = null;
                        if (in.ready()) {
                            request = in.readLine();
                        }

                        if (request == null) {
                            continue; //take next request
                        }

                        if (request.startsWith("cur")) {
                            System.out.println("got CUR");
                            pout.println("you send me CUR");
                        } else if (request.startsWith("list")) {
                            System.out.println("got List");
                            pout.println("you send me list");
                        } else if (request.startsWith("get")) {
                            System.out.println("got get");
                            pout.println("you send me get");
                        } else {
                            System.out.println("unknow command");
                            pout.println("Unknown command:" + request);
                        }
                        pout.flush();
                        out.flush();

                        Thread.sleep(10);
                        if (shutdownServer) { //this variable changed from another thread
                            break SERVER_CONN;
                        }

                    }
                } catch (IOException e) {
                    System.out.println("error in connection");
                }
            }
            //shutdown server
            pout.close();
            out.close();
            in.close();
            socket.close();
            pout = null;
            out = null;
            in = null;
            socket = null;

        } catch (IOException e) {
            System.out.println("error");
            System.err.println(e);
        }

    }

}
