package com.cawerit.summauspalvelu;

import com.cawerit.summauspalvelu.connectors.ConnectionStrategy;
import com.cawerit.summauspalvelu.connectors.UnpreparedConnection;
import com.cawerit.summauspalvelu.services.PortService;
import com.cawerit.summauspalvelu.services.SumService;
import com.cawerit.summauspalvelu.services.TestService;

import java.net.*;
import java.util.ArrayList;

/**
 * Vastaa palvelinten välisestä yhteydestä
 */
public class WorkManager {

    private PortGenerator portGen;
    private final Integer MANAGER_PORT;
    private final Integer SERVER_PORT;

    /**
     * @param serverPort Palvelimen portti
     * @param portGen Porttigeneraattori, jota käyttämällä voidaan luoda ohjelman sisällä uniikkeja porttinumeroita
     */
    public WorkManager(int serverPort, PortGenerator portGen){

        this.portGen = portGen;
        this.MANAGER_PORT = portGen.next();
        this.SERVER_PORT = serverPort;


        // #1: Muodostetaan yhteys palvelimeen
        //Luodaan ConnectionStrategy, joka yrittää 5 kertaa ottaa yhteyttä palvelimeen, kutsuen joka välissä metodia sendPort
        ConnectionStrategy connector = new UnpreparedConnection(MANAGER_PORT, 5000, 5) {
            @Override
            public void prepare() {//Tätä kutsutaan kun UnpreparedConnection yrittää muodostaa yhteyttä palvelimelle
                sendPort(SERVER_PORT);
            }
        };

        //Ylläoleva ei vielä itsessään suorita mitään, muodostetaan nopea kättely palvelimen kanssa
        connector.connect(null);

        //Kun yhteys on muodostettu, voidaan aloittaa palvelimen seuranta
        //(ConnectionStrategy huolehtii siitä että PortService aloittaa kutsut vasta kun portit on saatu
        new PortService(connector, portGen){
            /**
             * Tätä ylikirjoitettua metodia kutsutaan callbackina kun PortService on tehnyt tehtävänsä eli
             * luonut kaikki tarvittavat SumServicet. Välitetään luodut SumServicet TestServicelle tarkkailtavaksi.
             * @param created
             */
            @Override
            public void onComplete(ArrayList<SumService> created){
                super.onComplete(created);
                new TestService(connector, created).start();
            }

        }.start();

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

        } catch (UnknownHostException e) {//Datagram packetin luonti voi nostaa vain tämän virheen
            System.out.println("Exception occurred when creating a DatagramPacket");
            e.printStackTrace();
        } catch(Exception e){//Socketin luonti voi nostaa useita
            System.out.println("Exception occurred when creating a DatagramSocket");
            e.printStackTrace();
        }
    }

}
