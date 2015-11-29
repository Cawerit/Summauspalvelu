package com.cawerit.summauspalvelu;

import com.cawerit.summauspalvelu.services.ConnectionService;

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

    /**
     * Pyrkii ottamaan vastaan palvelimen hyväksynnän yhteydenotosta.
     * @param timeout Yläraja vastaanotolle, eli kuinka monta millisekuntia palvelimen vastausta maksimissaan odotetaan.
     * @return Soketti, jolla on hyväksytty yhteys palvelimelle tai null, jos yhteyttä ei voitu muodostaa aikarajan sisällä
     */
    private Socket getConnection(ServerSocket from, int timeout){
        System.out.println("client: Waiting for the server to respond...");
        Socket response = null;
        try {
            from.setSoTimeout(timeout);//Merkataan odotettava aika
            response = from.accept();//Otetaan vastaus
            System.out.println("client: ...response received.");
        } catch (Exception e) {
        } finally {
            return response;
        }
    }


    /**
     * Sulkee avatut socketit
     */
    public void close(){
        super.close();
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
                this.setSocket(client);
                super.run();//Kun yhteys on muodostettu, voidaan aloittaa palvelimen seuranta
            }


        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
