package com.cawerit.summauspalvelu.services;

import com.cawerit.summauspalvelu.connectors.ConnectionStrategy;

/**
 * Toisin kuin ConnectionService, tämä luokka varmistaa ennen jatkuvan lukemisen aloittamista että
 * ensimmäinen viesti on validi.
 */
public abstract class UnsureConnectionService extends ConnectionService {

    private boolean connectionEnsuredFlag;

    public UnsureConnectionService(ConnectionStrategy connector){
        super(connector);
        connectionEnsuredFlag = false;
    }

    /**
     * Metodi jota kutsutaan varmistettaessa että ensimmäinen arvo oli hyväksyttävä.
     * @param message Palvelimelta saatu viesti
     * @return True, mikäli kanssakäyntiä palvelimen kanssa voidaan jatkaa, muutoin false
     */
    public abstract boolean predicate(int message);

    /**
     * Metodi jota kutsutaan jos predicate(message) palautti false.
     * @param message Palvelimelta saatu viesti. Huom, tässä pokkeuksellisesti Integer eikä int, jotta virhetilanteessa voidaan antaa null.
     * @return Arvo joka lähetetään takaisin palvelimelle.
     */
    public abstract int onErrorAnswer(Integer message);


    @Override
    protected void keepReading(){
        if(!connectionEnsuredFlag){
            connectionEnsuredFlag = true;//Tarkistus tehdään vain kerran
            try {
                int message = readInt();
                if(!predicate(message)) answer(onErrorAnswer(message));
                else answer(message);
            } catch(Exception e){
                e.printStackTrace();
            }

        } else super.keepReading();
    }


}
