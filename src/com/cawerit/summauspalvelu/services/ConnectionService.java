package com.cawerit.summauspalvelu.services;


import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Luokka joka vastaa yhteydenpidosta palvelimen kanssa sen jälkeen kun yhteys on onnistuneesti muodostettu
 */
public class ConnectionService extends Thread {

    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ConnectionService(Socket socket){
        setSocket(socket);
    }

    public ConnectionService(){
        this(null);
    }


    public void setSocket(Socket socket) {
        this.socket = socket;
        if(socket != null) {
            try {
                this.inputStream = new ObjectInputStream(socket.getInputStream());
                this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Lähettää vastauksen palvelimelle. Oletusmuodossaan vastaus on täsmälleen sama kuin
     * saatu viesti, mutta ylikirjoitettuna tätä metodia voidaan käyttää minkä tahansa vastauksen antamiseen.
     * @param message Lähetettävä viesti
     */
    public void answer(int message) throws java.io.IOException{
        System.out.println("Answering with message " + message);
        outputStream.writeInt(message);
        outputStream.flush();
    }

    /**
     * Odottaa kunnes palvelimelta on saatavissa viesti ja sen jälkeen palauttaa saadun viestin.
     * @return Palvelimen viesti.
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException Mikäli säie on yritetty sulkea, heitetään virhe sen merkiksi.
     */
    private int readInt() throws java.io.IOException, InterruptedException{
        InputStreamReader reader = new InputStreamReader(inputStream);
        while (!reader.ready()){//Loop joka loppuu vasta kun palvelimella on vastaus valmiina
            if(this.isInterrupted()){//Tarkistetaan onko säie yritetty lopettaa
                throw new InterruptedException("Thread interrupted.");
            }
        };
        return inputStream.readInt();
    }



    @Override
    public void run(){

        if(this.socket == null) throw new NoEstablishedConnection();

        try {
            while(true) {
                answer(readInt());
            }
        } catch(InterruptedException e){

            System.out.println("ServerService interrupted");
            return;

        } catch(Exception e){
            e.printStackTrace();
        }


    }


    public void close(){
        try {
            if (socket != null) socket.close();
        } catch(Exception e){
            System.out.println("Exception occurred when closing the socket.");
            e.printStackTrace();
        }
    }


    static class NoEstablishedConnection extends RuntimeException {
        public NoEstablishedConnection(){
            super("Attempted to contact server before establishing any connection.\nMethod setSocket must be called first.");
        }
    }


}
