package com.studentmanager.client;

import java.util.ArrayList;

public class TempStudent {

    private String imie;
    private String nazwisko;
    private long indeks;

    private ArrayList<TempPrzedmiot> przedmioty;

    public TempStudent(String imie, String nazwisko, long indeks, ArrayList<TempPrzedmiot> przedmioty) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.indeks = indeks;
        this.przedmioty = przedmioty;
    }

    public TempStudent() {}


    public String getImie() {
        return imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public void setNazwisko(String nazwisko) {
        this.nazwisko = nazwisko;
    }

    public long getIndeks() {
        return indeks;
    }

    public void setIndeks(long indeks) {
        this.indeks = indeks;
    }

    public ArrayList<TempPrzedmiot> getPrzedmioty() {
        return przedmioty;
    }

    public void setPrzedmioty(ArrayList<TempPrzedmiot> przedmioty) {
        this.przedmioty = przedmioty;
    }


}
