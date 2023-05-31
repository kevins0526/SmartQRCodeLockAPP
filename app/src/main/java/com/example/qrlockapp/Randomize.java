package com.example.qrlockapp;
import java.util.Random;
public class Randomize {
    Random random=new Random();
    long number=1000000000000000L + (long)(random.nextDouble() * 9000000000000000L);
    String randomString = Long.toString(number);
    public static long IV(){
        try {
            Random random=new Random();
            long number=1000000000000000L + (long)(random.nextDouble() * 9000000000000000L);
            return number;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
