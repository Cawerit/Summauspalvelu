package com.cawerit.summauspalvelu.services;

/**
 * Lokero johon summaa voidaan tallettaa.
 */
public class Deposit {

    /**
     * Numero jonka avulla lokeron voi tunnistaa vaikka se olisi esimerkiksi sekoitetussa listassa
     */
    public final int IDENTIFIER;

    public Deposit(int identifier) {
        IDENTIFIER = identifier;
        count = sum = 0;
    }

    public int getSum() {
        return sum;
    }

    private int sum;

    /**
     * Lokeroon tallennettujen kokonaislukujen määrä
     * @return
     */
    public int getCount() {
        return count;
    }

    private int count;


    /**
     * Lisää lokeron summaan annetun arvon num
     * @param num
     */
    public void add(int num){
        sum += num;
        count++;
    }

    @Override
    public String toString(){
        return "{ #" + IDENTIFIER + ": sum " + this.sum + ", count " + this.count + " }";
    }



}
