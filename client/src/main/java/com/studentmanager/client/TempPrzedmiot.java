package com.studentmanager.client;

public class TempPrzedmiot {

    private String nazwa;
    private int ocena;

    public TempPrzedmiot(){}
    public TempPrzedmiot(String nazwa, int ocena) {
        this.nazwa = nazwa;
        this.ocena = ocena;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public int getOcena() {
        return ocena;
    }

    public void setOcena(int ocena) {
        this.ocena = ocena;
    }
}
