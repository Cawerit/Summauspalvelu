package com.cawerit.summauspalvelu.services;


import com.cawerit.summauspalvelu.connectors.ConnectionStrategy;

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

    private ConnectionStrategy.Connection connection;
    private ConnectionStrategy connector;

    private boolean completed;


    public ConnectionService(ConnectionStrategy connector){
        this.connector = connector;
    }


    /**
     * Lähettää vastauksen palvelimelle. Oletusmuodossaan vastaus on täsmälleen sama kuin
     * saatu viesti, mutta ylikirjoitettuna tätä metodia voidaan käyttää minkä tahansa vastauksen antamiseen.
     * @param message Lähetettävä viesti
     */
    protected void answer(int message) throws java.io.IOException{
        System.out.println("client: Answering with message " + message);
        ObjectOutputStream out = connection.getOutput();
        out.writeInt(message);
        out.flush();
    }


    /**
     * Odottaa kunnes palvelimelta on saatavissa viesti ja sen jälkeen palauttaa saadun viestin.
     * @return Palvelimen viesti.
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException Mikäli säie on yritetty sulkea, heitetään virhe sen merkiksi.
     */
    protected int readInt() throws java.io.IOException, InterruptedException{
        ObjectInputStream input = connection.getInput();
        InputStreamReader reader = new InputStreamReader(input);
        while (!completed && !reader.ready()){//Loop joka loppuu vasta kun palvelimella on vastaus valmiina
            if(this.isInterrupted()){//Tarkistetaan onko säie yritetty lopettaa
                throw new InterruptedException("Thread interrupted.");
            }
        };
        return input.readInt();
    }

    @Override
    public void run(){
        this.connector.connect(c -> {
            connection = c;
            keepReading();
        });
    }

    /**
     * Jatkaa palvelinsyötteen odottamista ja siihen vastausta kunnes tälle servicelle kutsutaan joko
     * onCompleted() tai interrupt()
     */
    protected void keepReading(){
        try {
            completed = false;
            while(!completed) {
                answer(readInt());
            }
        } catch(InterruptedException e){
            System.out.println("ConnectionService interrupted");
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
        this.connector.release();
    }

    @Override
    public void interrupt(){
        this.connector.close();
        super.interrupt();
    }

}
