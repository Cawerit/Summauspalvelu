package com.cawerit.summauspalvelu.services;

import com.cawerit.summauspalvelu.connectors.ConnectionStrategy;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Vastaa palvelimen lähettämiin testeihin
 */
public class TestService extends ConnectionService {

    private final int RESPONSE_CLOSE = 0;//Sulkee yhteyden koko ohjelmaan
    private final int RESPONSE_SUM = 1;//Lokeroiden yhteenlaskettu summa
    private final int RESPONSE_MAX = 2;//Lokero jonka summa on suurin
    private final int RESPONSE_TOTAL = 3;//Yhteenlaskettu kyselyjen määrä

    private int result;

    SumService[] services;
    Deposit[] deposits;

    public TestService(ConnectionStrategy connector, SumService[] sumServices){
        super(connector);
        services = sumServices;
        deposits = Arrays
                    .stream(services)
                    .map(s -> s.deposit)
                    .toArray(size -> new Deposit[size]);
    }
    public TestService(SumService[] services){
        this(null, services);
    }


    @Override
    public void answer(int message) throws java.io.IOException{

        if(message != RESPONSE_CLOSE) waitUntilStable(deposits);

        Stream<Deposit> stream = Arrays.stream(deposits);
        int result;


        switch(message) {

            case RESPONSE_SUM:
                result = stream
                        .mapToInt(Deposit::getSum)  //otetaan jokaisen yksittäisen lokeron summa
                        .sum();                     //ja lasketaan ne yhteen
                break;

            case RESPONSE_MAX:
                result = stream
                        .max(Comparator.comparing(Deposit::getSum)) //etsitään lokero jossa on suurin getSum-arvo
                        .map(s -> s.IDENTIFIER)                     //ja sitten valitaan löydetystä IDENTIFIER (annettu lokerolle PortServicessä)
                        .orElse(0);

                break;

            case RESPONSE_TOTAL:
                result = stream
                        .mapToInt(Deposit::getCount) //otetaan jokaisen lokeron kyselyjen määrä
                        .sum();                     //ja lasketaan ne yhteen
                break;

            case RESPONSE_CLOSE://Suljetaan hallitusti
                interrupt();
                onComplete();
                return;//Kutsutaan tässä return jotta vältytään lähettämästä palvelimelle enempää viestejä

            default:
                result = -1;
                break;

        }

        //System.out.print("client: Answering " + result + " for deposits " + Arrays.toString(deposits));

        super.answer(result);

    }

    /**
     * Pysäyttää säikeen suorittamisen kunnes deposits-listaan ei ole enää tulossa muutoksia (kaikki summaukset on kirjattu).
     * Ilman tämän metodin kutsumista joissain tapauksissa TestService tekee laskut ennenaikaisesti ja siksi väärin.
     *
     * TODO: Tämä on hack, jolla varmistetaan että summaus on varmasti valmis. Parempikin keino tähän varmasti olisi, tai kyseessä voi olla bugi.
     * @param deposits Lista lokeroista, joihin ei haluta enää tulevan muutoksia
     */
    private void waitUntilStable(Deposit[] deposits){
        try {
            int size = deposits.length;
            int[] current = new int[size];//Vertailulista edellisen kierroksen tuloksista
            boolean isStable = false;
            while (!isStable) {
                isStable = true;
                for (int i = 0; i < size; i++) {
                    if (current[i] != deposits[i].getCount()) {//Mikäli uusi ja vanha arvo eivät täsmää
                        current[i] = deposits[i].getCount();//asetetaan listaan uusi arvo
                        isStable = false;//ja otetaan uusi kierros
                    }
                }
                if(!isStable) Thread.sleep(100);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onComplete(){
        Arrays.stream(services)
                .filter(s -> !isInterrupted())
                .forEach(s -> s.interrupt());
    }

    @Override
    public void interrupt(){
        super.interrupt();
        this.onComplete();
    }



}
