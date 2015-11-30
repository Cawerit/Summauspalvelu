package com.cawerit.summauspalvelu.services;

import com.cawerit.summauspalvelu.PortGenerator;
import com.cawerit.summauspalvelu.connectors.ConnectionStrategy;
import com.cawerit.summauspalvelu.connectors.ExpectedConnection;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Vastaa summauspalveluiden porttien jakamisesta palvelimelle
 */
public class PortService extends ConnectionService{

    PortGenerator portGen;

    public PortService(ConnectionStrategy connector, PortGenerator portGen){
        super(connector);
        this.portGen = portGen;
    }

    /**
     * Vastaa palvelimen porttikyselyihin luomalla messagen osoittaman määrän PortServicejä ja vastaamalla
     * palvelimelle niiden porttinumerot.
     * @param message Palvelimen pyytämien porttien määrä (palvelin lähettää)
     */
    @Override
    public void answer(int message){
        System.out.println("client:\tServer sent us: " + message);

        SumService[] services = new SumService[message];

        try {
            for (int i = 0; i < message; i++) {
                int port = portGen.next();

                super.answer(port);
                SumService created = new SumService(new ExpectedConnection(port, 5000), i+1);
                created.start();
                services[i] = created;
            }

            this.onComplete(services);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Tämän callbackin voi halutessaan ylikirjoittaa. Sitä kutsutaan kun kaikki SumServicet on luotu.
     * @param created Lista luoduista summaimista
     */
    public void onComplete(SumService[] created){
        super.onComplete();
    }

}
