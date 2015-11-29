package com.cawerit.summauspalvelu.services;

import com.cawerit.summauspalvelu.PortGenerator;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Vastaa summauspalveluiden porttien jakamisesta palvelimelle
 */
public class PortService extends ConnectionService{

    PortGenerator portGen;

    public PortService(Socket socket, PortGenerator portGen){
        super(socket);
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
                SumService created = new SumService(new ServerSocket(port));
                created.start();
                services[i] = created;
            }

            this.onComplete(services);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }


    public void onComplete(SumService[] created){
        super.onComplete();
    }

}
