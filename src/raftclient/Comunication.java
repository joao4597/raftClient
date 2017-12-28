/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raftclient;

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
    private int serialNumber = 0;
    
    public Comunication() {
        
        //EACH COMAND HAS ASSOCIATED A SERIAL NUMBER
        //USED TO AVOID REPETITION
        serialNumber = 0;
        
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
        
        serialNumber++;
        
        command = "4100-100-" + Integer.toString(serialNumber) + "-" + command + "-";
        
        
        //SEND COMMAND IN BROADCAST AND WAIT FOR RESPONSE
        byte receiveData[] = new byte[1024];
        
        for(int i=4000; i < 4005; i++){
            DatagramPacket sendPacket;
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
                return null;
            }
        
            String commandReceived = new String(receivePacket.getData());
            String[] parts = commandReceived.split("-");
            
            
            if(Integer.parseInt(parts[1]) == serialNumber){
                return commandReceived;
            }
        }
        
        
    }
}
