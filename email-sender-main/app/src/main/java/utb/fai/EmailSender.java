package utb.fai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class EmailSender {

    private Socket socket;
    private BufferedReader reader;
    private OutputStream output;
    public EmailSender(String host, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = socket.getOutputStream();
        
        readServerResponse();
    }

    public void send(String from, String to, String subject, String text) throws IOException {

        sendToServer("EHLO " + from + "\r\n");
        readServerResponse();

        sendToServer("MAIL FROM:<" + from + ">\r\n");
        readServerResponse();

        sendToServer("RCPT TO:<" + to + ">\r\n");
        readServerResponse();

        sendToServer("DATA\r\n");
        readServerResponse();

        String emailContent = 
            "From: " + from + "\r\n" +
            "To: " + to + "\r\n" +
            "Subject: " + subject + "\r\n" +
            "\r\n" +
            text + "\r\n" +
            ".\r\n";
        
        sendToServer(emailContent);
        readServerResponse();
    }

    public void close() {
        try {
            if (output != null && socket != null && !socket.isClosed()) {
                sendToServer("QUIT\r\n");
                readServerResponse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToServer(String message) throws IOException {
        output.write(message.getBytes("UTF-8"));
        output.flush();
    }

    private void readServerResponse() throws IOException {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        while (reader.ready()) {
            String line = reader.readLine();
            if (line != null && line.length() >= 4 && line.charAt(3) == ' ') {
                break;
            }
        }
    }
}