/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raftclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * used to established between client and server
 * @author joao
 */
public class Comunication {
    private InetAddress ipAddress = null;
    private DatagramSocket sendSocket;
    private DatagramSocket receiveSocket;
    private int serialNumber = 1;
    private int clusterSize = 11;
    public FileWriter electionTimeFile;
    
    public long electionTimeMeasurement = 0;
    
    public Comunication() {
        
        openFileElectionTime();
        
        //EACH COMAND HAS ASSOCIATED A SERIAL NUMBER
        //USED TO AVOID REPETITION
        serialNumber = 1;
        
        try {
            ipAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Comunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //CREAT UPSTREAM SOCKET
        try {
            sendSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Comunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //CREAT DOWNSTREAM SOCKET
        try {
            receiveSocket = new DatagramSocket(4100, ipAddress);
        } catch (SocketException ex) {
            Logger.getLogger(Comunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param command - command to add to state machine
     * @return null when timeout is reached, returns string other wise
     */
    public String broadcastCommand(String command){
        
        //serialNumber++;
        
        command = "4100-0-100-" + Integer.toString(serialNumber) + "-" + command + "-";
        
        
        //SEND COMMAND IN BROADCAST AND WAIT FOR RESPONSE
        byte receiveData[] = new byte[1024];
        
        for(int i=4000; i < 4000 + clusterSize; i++){
            DatagramPacket sendPacket;
            if (electionTimeMeasurement == 0){
                electionTimeMeasurement = System.nanoTime();
            }
            sendPacket = new DatagramPacket(command.getBytes(), command.length(), ipAddress, i);
            try {
                sendSocket.send(sendPacket);
            } catch (IOException ex) {
                Logger.getLogger(RaftClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        while(true){
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
            //SET TIMOUT WHILE WAITING FOR RESPONSE
            try {
                receiveSocket.setSoTimeout(300);
            } catch (SocketException ex) {
                Logger.getLogger(Comunication.class.getName()).log(Level.SEVERE, null, ex);
            }
            //RECEIVE DATA
            try {   
                receiveSocket.receive(receivePacket);
            } catch (IOException ex) {
                Logger.getLogger(Comunication.class.getName()).log(Level.SEVERE, null, ex);
                serialNumber++;
                return null;
            }
        
            String commandReceived = new String(receivePacket.getData());
            String[] parts = commandReceived.split("-");
            
            
            if(Integer.parseInt(parts[1]) == serialNumber){
                serialNumber++;
                saveReplicationTime(System.nanoTime() - electionTimeMeasurement);
                electionTimeMeasurement = 0;
                return commandReceived;
            }
        }   
    }
    
    public void openFileElectionTime(){
        try {
            File file = new File("logReplication.txt");
            electionTimeFile = new FileWriter(file, true);
        } catch (IOException ex) {
            Logger.getLogger(Comunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveReplicationTime(long time){
        BufferedWriter out;
        out = new BufferedWriter(electionTimeFile);
        try {
            out.write(Long.toString(time) + "\n");
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Comunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
