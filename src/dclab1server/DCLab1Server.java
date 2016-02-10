package dclab1server;

import com.sun.webkit.Timer;
import java.io.*;
import java.net.*;
import java.io.File;

public class DCLab1Server
{

    private static final int PORT = 8000; //Deafult port
    private ServerSocket serverSocket = null;
    private boolean shutdownServer = false; // this variable desigen to be changed from diffreant thread 

    Socket socket;

    BufferedReader br;

    BufferedOutputStream bos;
    PrintStream ps;

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

        log("Server is Started on port : " + port);

        try {
            SERVER_CONN:
            while (true) { //wait for connection 
                socket = null;
                try {
                    // waiting for a connection, only one connection at the time
                    log("Server is waiting for connection");
                    socket = serverSocket.accept(); //code block here until connection come

                    log("Got request from " + socket.getInetAddress());

                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    bos = new BufferedOutputStream(socket.getOutputStream()); //will be used to send files
                    ps = new PrintStream(bos); //will be used to send messages

                    while (true) { //wait for input from clinet
                        String request = null;
                        socket.setSoTimeout(120000); //wait 120 sec max
                        try {
                            log("Server is waiting for command");
                            long waitCommandTime = System.currentTimeMillis();

                            WAIT_COMMAND:
                            do {
                                Thread.sleep(10);
                                if (br.ready()) {
                                    request = br.readLine();
                                    waitCommandTime = System.currentTimeMillis();
                                    if (request.equals("AKG")) {
                                        log("got AKG");
                                        //ignore the response and take next command
                                        continue WAIT_COMMAND;
                                    } else {
                                        break WAIT_COMMAND;
                                    }
                                }
                                ping();
                            } while (true);

                        } catch (Exception e) {
                            System.out.println(e);
                            sendLine("connnection drop TimeOut over 120 sec no command");
                            continue SERVER_CONN; //wait for another connection 
                        }

                        //when request is null it mean the clinet socket closed
                        if (request == null) {
                            log("connection closed from clinet side");
                            continue SERVER_CONN; //take next connection
                        }

                        if (request.equals("cur")) {
                            log("got CUR");
                            ps.println("Current Folder \n" + file.getAbsolutePath());
                            ps.println();
                            ps.flush();
                            
                        } else if (request.equals("list")) {
                            log("got List");
                            File[] files = file.listFiles();
                            sendLine("Number of Files and Folder : " + files.length);
                            for (File f : files) {
                                if (f.isDirectory()) {
                                    sendLine("[" + f.getName() + "]");
                                } else {
                                    sendLine(f.getName() + "\t\t\t" + f.length() / 1024 + " KB");
                                }
                            }
                            sendLineTerminal();
                            ps.flush();
                            
                        } else if (request.startsWith("get ")) {
                            log("got get");
                            String filename = request.substring(4).trim();
                            System.out.println(filename);
                            File f = new File("." + File.separator + filename);
                            if (f.exists()) {
                                if (f.isFile()) {
                                    sendLine("COPYING " + f.length());
                                    ps.flush();

                                    FileInputStream fis = new FileInputStream(f);

                                    byte[] b;
                                    final int defBufferSize = 8192;
                                    if (f.length() < defBufferSize) {
                                        b = new byte[(int) f.length()];
                                    } else {
                                        b = new byte[defBufferSize]; //max of buffer
                                    }

                                    int r;
                                    while ((r = fis.read(b)) > 0) {
                                        bos.write(b, 0, r);
                                    }
                                    bos.flush();
                                    b = null;
                                } else { //its folder not file
                                    sendLine("its not filename");
                                    sendLineTerminal();
                                    ps.flush();
                                }
                            } else { //file not exists 
                                sendLine("file is not exists");
                                sendLineTerminal();
                                ps.flush();
                            }

                        } else { // unknown command
                            log("unknow command");
                            sendLine("Unknown command:" + request);
                            sendLineTerminal();
                            ps.flush();
                        }

                        Thread.sleep(10);
                        if (shutdownServer) { //this variable changed from another thread not yet coded
                            break SERVER_CONN;
                        }

                    } //waiting command loop 
                } catch (IOException e) {
                    System.out.println(e);

                }
            } //waiting new connection loop

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

    private void log(String msg)
    {
        System.out.println(msg);
    }

    long pingTimer;

    private void ping()
    {
        if (true) {
            return; //disable ping
        }
        final int interval = 2000;

        if (System.currentTimeMillis() - pingTimer > interval) {
            ps.println("PING"); //send hear beat
            ps.flush();
            log("send PING");
            pingTimer = System.currentTimeMillis();
        }

    }

    private void sendLine(String res)
    {
        ps.println(res);
    }

    private void sendLineTerminal()
    {
        ps.println();
    }

}
