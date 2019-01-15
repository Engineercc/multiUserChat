package com.socket;

import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class chatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    private ArrayList<userStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<messageListener> messageListeners = new ArrayList<>();

    public chatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        chatClient client = new chatClient("localhost", 8818);
        client.addUserStatusListener(new userStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("online: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("offline: " + login);
            }
        });

        client.addMessageListener(new messageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println(  fromLogin +"'dan gelen mesaj "+ " -->" + msgBody);
            }
        });

        if (!client.connect()) {
            System.err.println("Bağlantı Başarısız.!");
        } else {
            System.out.println("Bağlantı Başarılı.!");

            if (client.login("Tugce", "Tugce")) {
                System.out.println("Giriş Başarılı.");

                client.msg("Yunus", "Hello World!");
            } else {
                System.err.println("Giriş Başarısız.!!");
            }

        }
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line:" + response);

        if ("Lets Login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for(messageListener listener : messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(userStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(userStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(userStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(userStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(messageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(messageListener listener) {
        messageListeners.remove(listener);
    }

}
