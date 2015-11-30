package com.cawerit.summauspalvelu.connectors;

import com.cawerit.summauspalvelu.services.ConnectionService;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class ConnectionStrategy {

    private ArrayList<Consumer<Connection>> pending;

    protected Connection activeConnection;

    private boolean occupied = false;
    private boolean closed = false;


    public ConnectionStrategy(){
        this.pending = new ArrayList<>();
    }

    /**
     * Metodi jota kutsutaan säettä suorittaessa palvelinyhteyden saavuttamiseksi.
     * Huom! whenConnected-metodia kutsutaan vasta kun isOccupied() palauttaa false.
     */
    public void connect(Consumer<Connection> whenConnected){
        if(!isClosed()) {

            if(activeConnection == null){
                try {
                    this.setConnection();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }

            pending.add(whenConnected);
            if (!isOccupied()){
                runNextCallback();
            }
        }
    }

    /**
     * Lopullisesti sulkee yhteyden ja siivoaa jäljet.
     * Huom! Tämä on eri asia kuin release, joka luovuttaa yhteyden käyttöoikeuden muualle.
     */
    public void close(){
        this.closed = true;
        try {
            this.activeConnection.output.close();
            this.activeConnection.input.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    };


    public void release(){
        this.occupied = false;

    }

    public boolean isClosed(){
        return closed;
    }

    /**
     * Yhteyden voi saada käytettäväkseen vain yksi käyttäjä kerrallaan, jotta säästytään
     * ongelmilta mahdollisen yhtäaikaisen Streamien käytön kanssa. Tämä metodi kertoo onko
     * ConnectionStrategy "varattu".
     * @return
     */
    public boolean isOccupied(){
        return occupied;
    }

    protected abstract void setConnection() throws Exception;


    private void runNextCallback(){
        if(pending.size() > 0) {
            Consumer<Connection> next = pending.remove(0);
            if(next != null){
                occupied = true;//Varataan soketti tälle callbackille
                next.accept(this.activeConnection);//Suoritetaan annettu callback
            }
        }
    }

    public static class Connection {


        private ObjectOutputStream output;
        private ObjectInputStream input;


        public Connection(InputStream input, OutputStream output){
            try {
                this.input = new ObjectInputStream(input);
                this.output = new ObjectOutputStream(output);
            } catch(Exception e){
                e.printStackTrace();
            }
        }


        public ObjectInputStream getInput() {
            return input;
        }


        public ObjectOutputStream getOutput() {
            return output;
        }

    }

}
