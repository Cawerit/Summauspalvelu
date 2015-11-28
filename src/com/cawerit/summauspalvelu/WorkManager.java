package com.cawerit.summauspalvelu;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Vastaa palvelinten välisestä yhteydestä
 */
public class WorkManager{

    private PortGenerator portGen;
    private final Integer MANAGER_PORT;

    /**
     * @param serverPort Palvelimen portti
     * @param portGen Porttigeneraattori, jota käyttämällä voidaan luoda ohjelman sisällä uniikkeja porttinumeroita
     */
    public WorkManager(int serverPort, PortGenerator portGen){
        super();
        this.portGen = portGen;
        this.MANAGER_PORT = portGen.next();

        // #1: Lähetetään palvelimelle portti johon se voi ottaa yhteyttä (MANAGER_PORT)
        sendPort(serverPort);

    }


    private void sendPort(int serverPort) {
        byte[] data = MANAGER_PORT.toString().getBytes();
        DatagramPacket packet;
        try {
            //Luodaan UDP-paketti lähetettäväksi palvelimelle
            packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), serverPort);
            //Lähetetään se annettuun palvelimen porttiin
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            System.out.println("Lähetys onnistui");

        } catch (UnknownHostException e) {//Datagram packetin luonti voi nostaa vain tämän virheen
            System.out.println("Virhe UDP paketin luonnissa!");
            e.printStackTrace();
        } catch(Exception e){//Socketin luonti voi nostaa useita
            System.out.println("Virhe socketin luonnissa!");
            e.printStackTrace();
        }
    }


    /**
     * Sulkee avatut socketit
     */
    public void close(){

    }




}
