package client;

/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

/**
* @author Jordy Evans 
* Implementation of RFC913 Protocol
*/

import java.io.*;
import java.net.*;

class TCPClient {

    // global variables to be used

    static Socket clientSocket;
    static BufferedReader inFromUser;
    static DataOutputStream outToServer;
    static BufferedReader inFromServer;

    // binary mode 
    static DataInputStream binaryInFromSever;
    static DataOutputStream binaryOutToServer;

    // initialize the current directory
    static String currentDirectory = System.getProperty("user.dir") + "/files";

    // file to retrieve
    static String fileToRetr;
    static int fileToRetrSize;
    static Boolean retrieval = false;

    static String sendType = "B";

    public static void main(String argv[]) throws Exception {
        String sentence;
        String sentenceFromServer;
        Boolean run = true;

        inFromUser = new BufferedReader(new InputStreamReader(System.in));

        clientSocket = new Socket("localhost", 6789);

        outToServer = new DataOutputStream(clientSocket.getOutputStream());

        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        binaryInFromSever = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        binaryOutToServer = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

        System.out.println("+Welcome to the server");

        while (run) {

            // read user input 
            sentence = inFromUser.readLine();
            String[] clientCmd = sentence.split(" ");

            // send command to server
            outToServer.writeBytes(sentence + "\n");

            // only commands that require action from client side are listed in this case statement
            switch (clientCmd[0]) {
                case "DONE":
                    run = false;
                    break;
                case "TYPE":
                    sendType = clientCmd[1];
                    break;
                case "RETR":
                    fileToRetr = clientCmd[1];
                    retrieval = true;
                    break;
                case "SEND":
                    SENDclient();
                    break;
                case "STOR":
                    STORclient(clientCmd[2]);
                default:
                    // commands not listed above do not require any action from client side
                    break;
            }

            sentenceFromServer = readIncoming();

            // handles a RETR rejection
            if ("-".equals(sentenceFromServer.substring(0, 1)) && retrieval){
                retrieval = false;
            }

            // read size sent back from server in reply to RETR command
            if (retrieval) {
                fileToRetrSize = Integer.parseInt(sentenceFromServer);
                retrieval = false;
            }

            // print server reply for user to see
            System.out.println("FROM SERVER: " + sentenceFromServer);
        }

        // DONE command breaks while loop
        clientSocket.close();
    }

    // SEND command handles client side sending requirements
    private static void SENDclient() {
        try {
            File file = new File(currentDirectory + "/" + fileToRetr);
            
            if ("A".equals(sendType)) { // ASCII mode
                try (BufferedOutputStream bufStream = new BufferedOutputStream(new FileOutputStream(file, false))) {
                    for (int i = 0; i < fileToRetrSize; i++) {
                        bufStream.write(inFromServer.read());
                    }
                    bufStream.flush();
                    bufStream.close();
                    retrieval = false;
                }
            } else { // B or C mode
                try (FileOutputStream fStream = new FileOutputStream(file, false)) {
                    int e;
                    int i = 0;
                    byte[] bytes = new byte[(int) fileToRetrSize];
                    while (i < fileToRetrSize) {
                        e = binaryInFromSever.read(bytes);
                        fStream.write(bytes, 0, e);
                        i += e;
                    }
                    fStream.flush();
                    fStream.close();
                    retrieval = false;
                }
            }
        } catch (FileNotFoundException n) {
            System.out.println("ERROR: Local FTP directory does not exist.");
        } catch (SocketException s) {
            System.out.println("Server connection was closed before file finshed transfer.");
        } catch (Exception e) {

        }
        return;
    }

    // STOR command handles client side requirements 
    private static void STORclient(String userFileName) throws IOException {
        File file = new File(currentDirectory + "/" + userFileName);

        if (!file.isFile()) {
            System.out.println(currentDirectory + "/" + userFileName);
            System.out.println("File doesnt exist. Restart server");
            return;
        }

        // STOR command was sent to server in main while loop
        // so now listen for the SIZE reply from the server
        String serverResponse = readIncoming();

        // server responds ready to send
        if ("+".equals(serverResponse.substring(0, 1))) {
            System.out.println("FROM SERVER: " + serverResponse);
            outToServer.writeBytes("SIZE " + file.length() + '\n');
            // potentially caused the issue before
            System.out.println("SIZE " + file.length() + '\n');
            serverResponse = readIncoming();
            System.out.println("FROM SERVER: " + serverResponse);

            // check server still allowing store from second response
            if ("+".equals(serverResponse.substring(0, 1))) {
                byte[] bytes = new byte[(int) file.length()];
                try {
                    if ("A".equals(sendType)) { // ASCII
                        try (BufferedInputStream bufStream = new BufferedInputStream(new FileInputStream(file))) {
                            outToServer.flush();
                            int p = 0;
                            while ((p = bufStream.read(bytes)) >= 0) {
                                outToServer.write(bytes, 0, p);
                            }
                            bufStream.close();
                            outToServer.flush();
                        } catch (IOException e) {
                            clientSocket.close();
                        }
                    } else { // B or C
                        try (FileInputStream fStream = new FileInputStream(file)) {
                            binaryOutToServer.flush();
                            int e;
                            while ((e = fStream.read()) >= 0) {
                                binaryOutToServer.write(e);
                            }
                            fStream.close();
                            binaryOutToServer.flush();
                        } catch (IOException e) {
                            clientSocket.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    ////////////////////////////////////////////////////////////// helper

    // workaround 
    private static String readIncoming() {
        String text = "";
        int c = 0;

        while (true) {
            try {
                c = inFromServer.read();
           } catch (IOException e) {
                e.printStackTrace();
            }

            if ((char) c == '\0' && text.length() > 0){
                break;
            }
                
            if ((char) c != '\0') {
                text = text + (char) c;
            }
                
        }
        return text;
    }

}
