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
        InputStreamReader inStRe;
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

                    inStRe = new InputStreamReader(socket.getInputStream());
                    in = new BufferedReader(inStRe);
                    out = new BufferedOutputStream(socket.getOutputStream());
                    pout = new PrintStream(out);
                    while (true) { //wait for input from clinet
                        String request = null;
                        request = in.readLine();

                        //when request is null it mean the clinet socket closed
                        if (request == null) {
                            continue SERVER_CONN; //take next connection
                        }

                        if (request.startsWith("cur")) {
                            System.out.println("got CUR");
                            pout.println("Current Folder \n" + file.getAbsolutePath());
                        } else if (request.startsWith("list")) {
                            System.out.println("got List");
                            String[] l = file.list();
                            for (String s : l) {
                                pout.println(s);
                            }
                        } else if (request.startsWith("get")) {
                            System.out.println("got get");
                            String filename = request.substring(4);
                            File f = new File("." + File.separator + filename);
                            if (f.exists()) {
                                if (f.isFile()) {
                                    pout.println("COPYING");
                                    pout.flush();
                                    FileInputStream fis = new FileInputStream(f);
                                    final int bufferSize = 8192;
                                    byte[] buffer = new byte[bufferSize];
                                    int read;
                                    while ((read = fis.read(buffer, 0, bufferSize)) != -1) {
                                        out.write(buffer, 0, read);
                                    }
                                    out.flush();
                                } else {
                                    pout.println("its not filename");
                                }
                            } else {
                                pout.println("file is not exists");
                            }
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
                    System.out.println(e);

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
