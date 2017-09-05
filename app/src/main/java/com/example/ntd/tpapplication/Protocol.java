package com.example.ntd.tpapplication;

/**
 * Created by ntd on 29/01/2016.
 */
public class Protocol {
    public String secure = "SECURE";
    public String depo = "DEPO";
    public String park = "PARKING";
    public String s2s = "S2S";
    public String crew_change = "CREWCHANGE";
    public String maintenance = "MAINTEN";

    public String rtb1 = "RTB1";
    public String rtb2 = "RTB2";
    public String rtb3 = "RTB3";
    public String rtb4 = "RTB4";
    public String rtb5 = "RTB5";
    public String rtb6 = "RTB6";


    public String sos = "SOS";
    public String door1 = "Door1";
    public String door2 = "Door2";
    public String door3 = "Door3";
    public Protocol()
    {

    }
    public String RequestString(int mode) {
        String a= "Request:";
        if(mode==1)
            return a+secure;
        else if(mode==2)
            return a+depo;
        else  if(mode==3)
            return a+park;
        else  if(mode==4)
            return a+s2s;
        else  if(mode==5)
            return a+crew_change;
        else  if(mode==6)
            return a+maintenance;
        else  if(mode==7)
            return a+sos;

        else  if(mode==8)
            return a+rtb1;
        else  if(mode==9)
            return a+rtb2;
        else  if(mode==10)
            return a+rtb3;
        else  if(mode==11)
            return a+rtb4;
        else  if(mode==12)
            return a+rtb5;
        else  if(mode==13)
            return a+rtb6;


        else
            return "";
    }
    public String GetMode(int mode) {
        if(mode==1)
            return secure;
        else if(mode==2)
            return depo;
        else  if(mode==3)
            return park;
        else  if(mode==4)
            return s2s;
        else  if(mode==5)
            return crew_change;
        else  if(mode==6)
            return maintenance;
        else  if(mode==7)
            return sos;

        else  if(mode==8)
            return rtb1;
        else  if(mode==9)
            return rtb2;
        else  if(mode==10)
            return rtb3;
        else  if(mode==11)
            return rtb4;
        else  if(mode==12)
            return rtb5;
        else  if(mode==13)
            return rtb6;


        else
            return "";
    }
    public String RequestMode(String mode) {
        String request = "MODE:" + mode.toUpperCase() + ":?";

        return request;
    }
    public String CodePass(String mode)
    {
        String request = "MODE:" + mode.toUpperCase() + ":CHEP";
        return request;
    }
}