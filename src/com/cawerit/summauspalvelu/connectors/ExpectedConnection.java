package com.cawerit.summauspalvelu.connectors;

/**
 * Yhteydenottostrategia jota voidaan käyttää kun tiedetään palvelimen jo olevan ottamassa yhteyttä
 */
public class ExpectedConnection extends UnpreparedConnection {

    public ExpectedConnection(int toPort, int timeout, int maxFailures){
        super(toPort,timeout, maxFailures);
    }

    public ExpectedConnection(int toPort, int timeout){
        this(toPort, timeout, 1);
    }

    public void prepare(){}

}
