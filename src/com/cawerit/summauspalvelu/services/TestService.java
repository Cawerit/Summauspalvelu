package com.cawerit.summauspalvelu.services;

import com.cawerit.summauspalvelu.connectors.ConnectionStrategy;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Vastaa palvelimen testeihin
 */
public class TestService extends ConnectionService {

    private final int RESPONSE_CLOSE = 0;
    private final int RESPONSE_SUM = 1;
    private final int RESPONSE_MAX = 2;
    private final int RESPONSE_TOTAL = 3;

    ArrayList<SumService> services;

    public TestService(ConnectionStrategy connector, ArrayList<SumService> sumServices){
        super(connector);
        this.services = sumServices;
    }


    @Override
    public void answer(int message) throws java.io.IOException{

        Stream<SumService> stream = services.stream();


        int result;

        switch(message) {

            case RESPONSE_SUM:
                result = stream
                        .mapToInt(SumService::getSum)
                        .sum();
                System.out.println("Summattu nyt " + result);
                break;

            case RESPONSE_MAX:
                result = stream
                        .max(Comparator.comparing(SumService::getCalls))
                        .map(s -> s.IDENTIFIER)
                        .orElse(0);

                break;

            case RESPONSE_TOTAL:
                result = stream
                        .mapToInt(SumService::getCalls)
                        .sum();
                break;

            case RESPONSE_CLOSE:
                onComplete();
                interrupt();
                return;//Kutsutaan tässä return jotta vältytään lähettämästä palvelimelle enempää viestejä

            default:
                result = -1;
                break;

        }
        super.answer(result);

    }

    @Override
    public void interrupt(){
        services.stream()
                .filter(s -> !isInterrupted())
                .forEach(s -> s.interrupt());
        super.interrupt();
    }



}