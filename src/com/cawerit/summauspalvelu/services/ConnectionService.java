package com.cawerit.summauspalvelu.services;


import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;

/**
 * Luokka joka vastaa yhteydenpidosta palvelimen kanssa sen jälkeen kun yhteys on onnistuneesti muodostettu
 */
public class ConnectionService extends Thread {

    private Socket socket;
    private ServerSocket server;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private boolean completed;

    public ConnectionService(Socket socket){
        setSocket(socket);
    }

    /**
     * Vaihtoehtoinen tapa luoda yhteys palvelimeen: mikäli valmista yhteyttä ei vielä ole luotu, voidaan
     * antaa ConnectionServicelle ServerSocket johon palvelin voi ottaa yhteyttä.
     * @param socket Soketti johon palvelin aikoo ottaa yhteyttä.
     */
    public ConnectionService(ServerSocket socket){
        this.server = socket;
    }

    public ConnectionService(ObjectInputStream input, ObjectOutputStream output){
        this();
        inputStream = input;
        outputStream = output;
    }

    public ConnectionService(){
        this.socket = null;
    }

    public Socket getSocket(){
        return this.socket;
    }

    public ObjectInputStream getInputStream(){
        return inputStream;
    }

    public ObjectOutputStream getOutputStream(){
        return outputStream;
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
        System.out.println("client: Answering with message " + message);
        outputStream.writeInt(message);
        outputStream.flush();
    }


    /**
     * Pyrkii ottamaan vastaan palvelimen hyväksynnän yhteydenotosta.
     * @param timeout Yläraja vastaanotolle, eli kuinka monta millisekuntia palvelimen vastausta maksimissaan odotetaan.
     * @return Soketti, jolla on hyväksytty yhteys palvelimelle tai null, jos yhteyttä ei voitu muodostaa aikarajan sisällä
     */
    public Socket getConnection(ServerSocket from, int timeout){
        System.out.println("client: Waiting for the server to respond...");
        Socket response = null;
        try {
            from.setSoTimeout(timeout);//Merkataan odotettava aika
            response = from.accept();//Otetaan vastaus
            System.out.println("client: ...response received.");
        } catch (Exception e) {
        } finally {
            return response;
        }
    }


    /**
     * Odottaa kunnes palvelimelta on saatavissa viesti ja sen jälkeen palauttaa saadun viestin.
     * @return Palvelimen viesti.
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException Mikäli säie on yritetty sulkea, heitetään virhe sen merkiksi.
     */
    private int readInt() throws java.io.IOException, InterruptedException{
        InputStreamReader reader = new InputStreamReader(inputStream);
        while (!completed && !reader.ready()){//Loop joka loppuu vasta kun palvelimella on vastaus valmiina
            if(this.isInterrupted()){//Tarkistetaan onko säie yritetty lopettaa
                throw new InterruptedException("Thread interrupted.");
            }
        };
        return inputStream.readInt();
    }



    @Override
    public void run(){

        if(socket == null){
            if(server != null){//Jos client sokettia ei vielä ole luotu, mutta server socket on, yritetään vastaanottaa yhteys sieltä

                setSocket(getConnection(server, 5000));
                server = null;
                System.out.println("getConnection " + socket);
                run();
                return;

            } else if(outputStream == null && inputStream == null)
                throw new NoEstablishedConnection();
        }

        try {
            completed = false;
            while(!completed) {
                answer(readInt());
            }
        } catch(InterruptedException e){

            System.out.println("ServerService interrupted");
            return;

        } catch(Exception e){
            e.printStackTrace();
        }


    }

    /**
     * Metodi jota kutsutaan mikäli säikeen suoritus päättyy tehtävän valmistumisen takia.
     * Ylikirjoitettuna tätä voi käyttää "event callbackina" tehtävän valmistumiselle.
     */
    public void onComplete(){
        this.completed = true;
    }


    static class NoEstablishedConnection extends RuntimeException {
        public NoEstablishedConnection(){
            super("Attempted to contact server before establishing any connection.\nMethod setSocket must be called first.");
        }
    }


}
