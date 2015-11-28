package com.cawerit.summauspalvelu;


import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * PortGenerator-oliolla voi luoda x-määrän portteja, jotka ovat joka kerta uniikkeja
 * kyseisen instanssin sisällä.
 */
public class PortGenerator {

    private ArrayList<Integer> reserved;
    private Integer startFrom;

    /**
     * @param reserved Lista varatuista porttinumeroista. Huom! Tätä listaa muokataan sitä mukaa kun portteja kutsutaan,
     *                 mikä mahdllistaa saman varatut-listan jakamisen useiden PortGenerator-olioiden kesken.
     */
    public PortGenerator(int startFrom, ArrayList<Integer> reserved){
        this.startFrom = startFrom;
        this.reserved = reserved;
    }

    /**
     * @return Uniikki numero, jota ei ole palautettu tämän instanssin sisällä aiemmin eikä sisälly varattuihin numeroihin
     */
    public Integer next(){
        Integer candidate = this.startFrom - 1;
        do {
            candidate = this.nextCandidate(candidate);
        } while(reserved.contains(candidate));
        reserved.add(candidate);
        return candidate;
    }

    /**
     * @return Uusi ehdotus uniikiksi numeroksi
     */
    private Integer nextCandidate(Integer old){
        return new Integer(old.intValue() + 1);
    }

}
