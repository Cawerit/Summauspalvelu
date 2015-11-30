package com.cawerit.summauspalvelu;


import org.utu.assignment.WorkDistributor;

import java.util.ArrayList;

public class Summauspalvelu {

    public static int SERVER_PORT = 3126;

    public static void main(String[] args) {

        new Thread(){//Käynnistetään tehtävässä määritetty palvelin Y
          public void run(){
              System.out.println("Aloitetaan palvelin Y");
              try {//Kutsutaan WorkDistributorin main-metodia
                  WorkDistributor.main(new String[]{"verbose"});//Argumentilla "verbose" saadaan palvelin Y jakamaan enemmän tietoa toiminnastaan
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
        }.start();

        new Thread(){//Käynnistetään oma sovellus X
            public void run(){
                //Merkataan, että palvelimen Y käyttämää porttia ei voida käyttää tässä ohjelmassa
                ArrayList<Integer> reserved = new ArrayList<>(1);
                reserved.add(SERVER_PORT);

                //Otetaan yhteyttä palvelimeen aloittamalla summauspalvelu
                new WorkManager(SERVER_PORT, new PortGenerator(SERVER_PORT-2, reserved));
            }
        }.start();

    }
}
