package com.cawerit.summauspalvelu;

import java.net.*;
import java.rmi.server.RMIClientSocketFactory;

/**
 * Vastaa palvelinten välisestä yhteydestä
 */
public class WorkManager{

    private PortGenerator portGen;
    private final Integer MANAGER_PORT;
    private Socket socket;

    /**
     * @param serverPort Palvelimen portti
     * @param portGen Porttigeneraattori, jota käyttämällä voidaan luoda ohjelman sisällä uniikkeja porttinumeroita
     */
    public WorkManager(int serverPort, PortGenerator portGen){

        this.portGen = portGen;
        this.MANAGER_PORT = portGen.next();

        ServerSocket server;
        Socket client;
        int attemptsLeft = 5;

        try {

            server = new ServerSocket(MANAGER_PORT);

            do {
                // #1: Lähetetään palvelimelle portti johon se voi ottaa yhteyttä (MANAGER_PORT)
                sendPort(serverPort);

                // #2: Odotetaan että palvelin hyväksyy yhteyden
                client = getConnection(server, 5000);

                //Yritetään uudestaan kunnes palvelin vastaa, kuitenkin korkeintaan 5 kertaa
            } while (client == null && --attemptsLeft > 0);


            if(client == null) System.out.println("Palvelin ei vastannut. Ohjelma lopetetaan.");

            this.socket = client;//Sallitaan

        } catch (Exception e){
            System.out.println("Virhe ohjelman suorituksessa");
            e.printStackTrace();
        }


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
            System.out.println("Porttinumero lähetetty palvelimelle.");

        } catch (UnknownHostException e) {//Datagram packetin luonti voi nostaa vain tämän virheen
            System.out.println("Virhe UDP paketin luonnissa!");
            e.printStackTrace();
        } catch(Exception e){//Socketin luonti voi nostaa useita
            System.out.println("Virhe socketin luonnissa!");
            e.printStackTrace();
        }
    }

    /**
     * Pyrkii ottamaan vastaan palvelimen hyväksynnän yhteydenotosta.
     * @param timeout Yläraja vastaanotolle, eli kuinka monta millisekuntia palvelimen vastausta maksimissaan odotetaan.
     * @return Soketti, jolla on hyväksytty yhteys palvelimelle tai null, jos yhteyttä ei voitu muodostaa aikarajan sisällä
     */
    private Socket getConnection(ServerSocket from, int timeout){
        System.out.println("Odotetaan vastausta");
        Socket response = null;
        try {
            from.setSoTimeout(timeout);//Merkataan odotettava aika
            response = from.accept();//Otetaan vastaus
            System.out.println("Vastaus saatiin!");
        } catch (Exception e) {
            System.out.println("Virhe hakiessa vastausta");
        } finally {
            return response;
        }
    }


    /**
     * Sulkee avatut socketit
     */
    public void close(){

    }




}
