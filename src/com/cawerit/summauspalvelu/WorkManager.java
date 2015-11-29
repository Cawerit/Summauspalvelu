package com.cawerit.summauspalvelu;

import com.cawerit.summauspalvelu.services.ConnectionService;
import com.cawerit.summauspalvelu.services.PortService;
import com.cawerit.summauspalvelu.services.SumService;
import com.cawerit.summauspalvelu.services.TestService;

import java.net.*;

/**
 * Vastaa palvelinten välisestä yhteydestä
 */
public class WorkManager extends ConnectionService {

    private PortGenerator portGen;
    private final Integer MANAGER_PORT;
    private final Integer SERVER_PORT;

    /**
     * @param serverPort Palvelimen portti
     * @param portGen Porttigeneraattori, jota käyttämällä voidaan luoda ohjelman sisällä uniikkeja porttinumeroita
     */
    public WorkManager(int serverPort, PortGenerator portGen){

        super();

        this.portGen = portGen;
        this.MANAGER_PORT = portGen.next();
        this.SERVER_PORT = serverPort;

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
            System.out.println("Portnumber sent to server.");

        } catch (UnknownHostException e) {//Datagram packetin luonti voi nostaa vain tämän virheen
            System.out.println("Exception occurred when creating a DatagramPacket");
            e.printStackTrace();
        } catch(Exception e){//Socketin luonti voi nostaa useita
            System.out.println("Exception occurred when creating a DatagramSocket");
            e.printStackTrace();
        }
    }



    @Override
    public void run(){

        //Huom! Tässä ei kutsuta metodia super.run, koska ServerConnection-luokan run
        //odottaa että yhteys palvelimeen on jo saavutettu! Kutsutaan yläluokan metodia siis
        //vasta myöhemmin.

        ServerSocket server;
        Socket client;
        int attemptsLeft = 5;

        try {

            server = new ServerSocket(MANAGER_PORT);

            do {
                // #1: Lähetetään palvelimelle portti johon se voi ottaa yhteyttä (MANAGER_PORT)
                sendPort(this.SERVER_PORT);

                // #2: Odotetaan että palvelin hyväksyy yhteyden
                client = getConnection(server, 5000);

                //Yritetään uudestaan kunnes palvelin vastaa, kuitenkin korkeintaan 5 kertaa
            } while (client == null && --attemptsLeft > 0);


            if(client == null) System.out.println("client: Server didn't respond. Shutting down.");
            else{

                //Kun yhteys on muodostettu, voidaan aloittaa palvelimen seuranta

                new PortService(client, portGen){

                    public void onComplete(SumService[] created){
                        System.out.println("Port service completed");
                        new TestService(getInputStream(), getOutputStream(), created).start();
                        super.onComplete(created);
                    }

                }.start();


            }


        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
