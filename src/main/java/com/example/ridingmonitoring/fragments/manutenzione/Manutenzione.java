package com.example.ridingmonitoring.fragments.manutenzione;

public class Manutenzione {
    private final String title;
    private float kmManutenzionePrec;
    private final int km;
    public Manutenzione(String title,float kmManutenzionePrec,int km){
        this.title = title;
        this.kmManutenzionePrec = kmManutenzionePrec;
        this.km = km;
    }

    public int getKm() { return km; }
    public String getTitle() { return title; }
    public float getKmManutenzionePrec() { return kmManutenzionePrec; }

    public void setKmManutenzionePrec(float kmManutenzionePrec) { this.kmManutenzionePrec = kmManutenzionePrec; }
}
