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

        ArrayList<SumService> services = new ArrayList<SumService>(message);

        try {
            for (int i = 0; i < message; i++) {
                int port = portGen.next();

                super.answer(port);
                SumService created = new SumService(new ExpectedConnection(port, 5000), i+1);
                created.start();
                services.add(created);
            }

            this.onComplete(services);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }


    public void onComplete(ArrayList<SumService> created){
        super.onComplete();
    }

}
