package com.cawerit.summauspalvelu.services;

import com.cawerit.summauspalvelu.connectors.ConnectionStrategy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Ottaa palvelimelta vastaan kokonaislukuja ja kerää niiden summaa
 */
public class SumService extends ConnectionService {

    private int sum;
    private int calls;

    /**
     * Numero jolla luodun SumService-olion voi myöhemmin erottaa muista, vaikka ne sisältävää listaa uudelleenjärjestettäisiin.
     */
    public final int IDENTIFIER;

    public SumService(ConnectionStrategy connector, int identifier){
        super(connector);
        sum = calls = 0;
        this.IDENTIFIER = identifier;
    }

    public int getSum(){ System.out.println("client: Säikeen " + IDENTIFIER + " summalaskuria kutsutaan " + sum); return sum; }
    public int getCalls() { return calls; }

    @Override
    public int readInt() throws IOException, InterruptedException {
        int res = super.readInt();
        return res;
    }

    /**
     * Ottaa vastaan palvelimen summauspyynnöt.
     * @param message Palvelimen lähettämä summattava kokonaisluku
     */
    @Override
    public void answer(int message){
        //HUOM! Tässä ei tarkoituksella kutsuta super.answer, sillä palvelin ei odota suoraa vastausta summauspyyntöön
        if(message == 0){
            onComplete();
            interrupt();
        } else {
            System.out.println("client: Säie " + IDENTIFIER + " summaa lisää " + message);
            sum += message;
            calls++;
        }
    }


}
