package com.fg.franco.i_agro;

import java.util.Random;

public class RandomAnalyzer implements Analyzer {

    public boolean analize(){
        Random rand = new Random();
        return rand.nextBoolean();
    }
}
