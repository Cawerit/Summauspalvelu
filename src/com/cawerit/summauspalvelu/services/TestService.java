package com.cawerit.summauspalvelu.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Vastaa palvelimen testeihin
 */
public class TestService extends ConnectionService {

    private final int RESPONSE_SUM = 1;
    private final int RESPONSE_MAX = 2;
    private final int RESPONSE_TOTAL = 3;

    SumService[] services;

    public TestService(ObjectInputStream input, ObjectOutputStream output, SumService[] sumServices){
        super(input, output);
        this.services = sumServices;
    }


    @Override
    public void answer(int message) throws java.io.IOException{

        Stream<SumService> stream = new ArrayList<SumService>(Arrays.asList(services)).stream();

        int result;

        switch(message) {

            case RESPONSE_SUM:
                result = stream
                        .mapToInt(s -> s.getSum())
                        .sum();
                break;

            case RESPONSE_MAX:
                result = stream
                        .mapToInt(s -> s.getSum())
                        .max()
                        .orElse(0);
                break;

            case RESPONSE_TOTAL:
                result = stream
                        .mapToInt(s -> s.getCalls())
                        .sum();
                break;

            default:
                result = -1;
                break;

        }

        super.answer(result);

    }


}
