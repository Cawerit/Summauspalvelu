package com.cawerit.summauspalvelu;


import org.utu.assignment.WorkDistributor;

public class Summauspalvelu {

    public static void main(String[] args) {

        System.out.println("Summauspalvelu käynnissä");

        try { //Kutsutaan tehtävässä määritettyä palvelinta Y
            WorkDistributor.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
