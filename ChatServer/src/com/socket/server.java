package com.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class server extends Thread {
    private final int serverPort;
    private ArrayList<serverWorker> workerList = new ArrayList<>();

    public server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<serverWorker> getWorkerList() {
        return workerList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true) {
                System.out.println("Kabul edilen bağlantı...");
                Socket clientSocket = serverSocket.accept();
                System.out.println(" bağlantının kimden geldiği: " + clientSocket);
                serverWorker worker = new serverWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(serverWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
