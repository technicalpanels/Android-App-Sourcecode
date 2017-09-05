package com.example.ntd.tpapplication;

/**
 * Created by ntd on 27/08/2015.
 */
import java.lang.String;
public class Person {
    private String idP1;
    public boolean isP1In;
    private String idP2;
    public boolean isP2In;
    private String idP3;
    public boolean isP3In;
    public void setIDP1(String value) {
        this.idP1 = value;
    }
    public String getIDP1()
    {
        return this.idP1;
    }
    public void setIDP2(String value) {
        this.idP2 = value;
    }
    public String getIDP2()
    {
        return this.idP2;
    }
    public void setIDP3(String value) {
        this.idP3 = value;
    }
    public String getIDP3()
    {
        return this.idP3;
    }
}

