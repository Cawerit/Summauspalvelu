package com.cawerit.summauspalvelu.connectors;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class UnpreparedConnection extends ConnectionStrategy {

    private int toPort;
    private int timeout;
    private int maxFailures;
    private Socket socket;

    public UnpreparedConnection(int toPort, int timeout, int maxFailures){
        this.toPort = toPort;
        this.timeout = timeout;
        this.maxFailures = maxFailures;
    }

    public UnpreparedConnection(int toPort, int timeout){
        this(toPort, timeout, 1);
    }

    /**
     * Tätä metodia kutsutaan yhteydenoton alkuvaiheessa. Se voi sisältää esimerkiksi yhteydenoton palvelimelle.
     */
    public abstract void prepare();


    /**
     * Pyrkii ottamaan vastaan palvelimen hyväksynnän yhteydenotosta.
     * @return Soketti, jolla on hyväksytty yhteys palvelimelle tai null, jos yhteyttä ei voitu muodostaa aikarajan sisällä
     */
    protected void setConnection() throws java.io.IOException {
        Socket response = null;
        ServerSocket server = new ServerSocket(this.toPort);
        int attemptsLeft = maxFailures;
        do {
            prepare();//Tehdään tarvittavat alkuvalmistelut
            server.setSoTimeout(timeout);//Merkataan odotettava aika
            response = server.accept();//Otetaan vastaus
            if(response != null) break;
        } while(attemptsLeft > 0);

        this.activeConnection = new ConnectionStrategy.Connection(response.getInputStream(), response.getOutputStream());
        socket = response;
    }

    @Override
    protected void cleanup(){
        System.out.println("client: Closing socket....");
        if(socket != null && !socket.isClosed()) try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
