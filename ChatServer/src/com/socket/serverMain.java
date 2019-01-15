package com.socket;


public class serverMain {
    public static void main(String[] args) {
        int port = 8818;
        server server = new server(port);
        server.start();
    }
}
