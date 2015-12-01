package com.cawerit.summauspalvelu.services;

import com.cawerit.summauspalvelu.connectors.ConnectionStrategy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Ottaa palvelimelta vastaan kokonaislukuja ja kerää niiden summaa
 */
public class SumService extends ConnectionService {

    public final Deposit deposit;


    public SumService(ConnectionStrategy connector, Deposit deposit){
        super(connector);
        this.deposit = deposit;
    }

    /**
     * Ottaa vastaan palvelimen summauspyynnöt.
     * @param message Palvelimen lähettämä summattava kokonaisluku
     */
    @Override
    public void answer(int message){
        //HUOM! Tässä ei tarkoituksella kutsuta super.answer, sillä palvelin ei odota suoraa vastausta summauspyyntöön
        if(message == 0){
            System.out.println("client: Server asked a SumService " + deposit.IDENTIFIER + " to quit");
            onComplete();
            interrupt();
        } else {
            deposit.add(message);
        }
    }

}
