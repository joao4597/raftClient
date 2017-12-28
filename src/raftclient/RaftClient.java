/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raftclient;

import gui.RaftClientGui;
import java.net.UnknownHostException;

import java.net.*;



/**
 *
 * @author joao
 */
public class RaftClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, SocketException {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RaftClientGui().setVisible(true);
            }
        });
    }
    
}
