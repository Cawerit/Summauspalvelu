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

    protected Connection activeConnection;
    private int connected = 0;
    private boolean closed = false;


    /**
     * Metodi jota kutsutaan säettä suorittaessa palvelinyhteyden saavuttamiseksi.
     * Huom! whenConnected-metodia kutsutaan vasta kun isOccupied() palauttaa false.
     */
    public Connection connect(){
        System.out.println("Attempt to connect " + isClosed());
        if(!isClosed()) {
            connected++;
            if(activeConnection == null){
                try {
                    this.setConnection();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            return activeConnection;
        } else return null;
    }

    /**
     * Lopullisesti sulkee yhteyden ja siivoaa jäljet.
     * Huom! Tämä on eri asia kuin release, joka luovuttaa yhteyden käyttöoikeuden muualle.
     */
    public void close(){

        if(connected <= 0 && !isClosed()) {
            closed = true;
            try {
                activeConnection.output.close();
                activeConnection.input.close();
                cleanup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Kutsutaan sulkemisen yhteydessä
     */
    protected void cleanup(){};


    public void release(){
        connected--;
    }

    public boolean isClosed(){
        return closed;
    }


    protected abstract void setConnection() throws Exception;

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


        public synchronized ObjectInputStream getInput() {
            return input;
        }

        public synchronized ObjectOutputStream getOutput() {
            return output;
        }

    }

}
