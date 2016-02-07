package dclab1server;

import com.sun.webkit.Timer;
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
        if (args.length > 0) {
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
            System.out.println(e);
            System.exit(-1);
        }

        System.out.println("Server is Started on port : " + port);

        Socket socket;

        InputStreamReader isr;
        BufferedReader br;

        BufferedOutputStream bos;
        PrintStream ps;

        try {
            SERVER_CONN:
            while (true) { //wait for connection 
                socket = null;
                try {
                    // waiting for a connection, only one connection at the time
                    System.out.println("Server is waiting for connection");
                    socket = serverSocket.accept();

                    long lastRespons = System.currentTimeMillis(); // used for server clinet heart beat

                    System.out.println("Got request from " + socket.getInetAddress());

                    isr = new InputStreamReader(socket.getInputStream());
                    br = new BufferedReader(isr);
                    bos = new BufferedOutputStream(socket.getOutputStream());
                    ps = new PrintStream(bos);

                    while (true) { //wait for input from clinet
                        String request = null;
                        socket.setSoTimeout(20000); //wait 120 sec max
                        try {
                            System.out.println("Server is waiting for command");
                            request = br.readLine();
                            lastRespons = System.currentTimeMillis(); // used for server clinet heart beat
                        } catch (Exception e) {
                            System.out.println(e);
                            ps.println("connnection drop TimeOut over 120 sec no command");
                            continue SERVER_CONN; //wait for another connection 
                        }

                        //when request is null it mean the clinet socket closed
                        if (request == null) {
                            System.out.println("connection closed from clinet side");
                            continue SERVER_CONN; //take next connection
                        }

                        if (request.startsWith("cur")) {
                            System.out.println("got CUR");
                            ps.println("Current Folder \n" + file.getAbsolutePath());
                        } else if (request.startsWith("list")) {
                            System.out.println("got List");
//                            String[] l = file.list();
//                            for (String s : l) {
//                                pout.println(s);
//                            }
                            File[] files = file.listFiles();
                            for (File f : files) {
                                if (f.isDirectory()) {
                                    ps.println("[" + f.getName() + "]");
                                } else {
                                    ps.println(f.getName() + "                " + f.length() / 1024 + " KB");
                                }
                            }
                        } else if (request.startsWith("get")) {
                            System.out.println("got get");
                            String filename = request.substring(4);
                            File f = new File("." + File.separator + filename);
                            if (f.exists()) {
                                if (f.isFile()) {
                                    ps.println("COPYING");
                                    FileInputStream fis = new FileInputStream(f);

                                    byte[] b = new byte[8192];
                                    int r;
                                    while ((r = fis.read(b)) > 0) {
                                        bos.write(b, 0, r);
                                    }
                                    bos.flush();
                                } else {
                                    ps.println("its not filename");
                                }
                            } else {
                                ps.println("file is not exists");
                            }
                        } else {
                            System.out.println("unknow command");
                            ps.println("Unknown command:" + request);
                        }
                        ps.flush();

                        Thread.sleep(10);
                        if (shutdownServer) { //this variable changed from another thread not yet coded
                            break SERVER_CONN;
                        }

                    }
                } catch (IOException e) {
                    System.out.println(e);

                }
            }

            //shutdown server
            ps.close();
            bos.close();
            br.close();
            socket.close();
            ps = null;
            bos = null;
            br = null;
            socket = null;

        } catch (IOException e) {
            System.out.println("error");
            System.err.println(e);
        }

    }

}
