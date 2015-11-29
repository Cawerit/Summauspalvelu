package com.cawerit.summauspalvelu.services;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Ottaa palvelimelta vastaan kokonaislukuja ja kerää niiden summaa
 */
public class SumService extends ConnectionService {

    private int sum;
    private int calls;

    public SumService(ServerSocket socket){
        super(socket);
        sum = calls = 0;
    }

    public int getSum(){ return sum; }
    public int getCalls() { return calls; }

    /**
     * Ottaa vastaan palvelimen summauspyynnöt.
     * @param message Palvelimen lähettämä summattava kokonaisluku
     */
    @Override
    public void answer(int message){
        //HUOM! Tässä ei tarkoituksella kutsuta super.answer, sillä palvelin ei odota suoraa vastausta summauspyyntöön
        if(message == 0){
            System.out.println("Nyt pitäis lopettaa");
        } else {
            sum += message;
            calls++;
        }
    }

}
