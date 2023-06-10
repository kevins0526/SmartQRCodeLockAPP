package com.example.qrlockapp;
import java.util.Random;
public class Randomize {
    public static long IV(){
        //储存現在時間的微秒 作為Ranodm 的種子
        long currentTimestamp = System.currentTimeMillis();
        try {
            Random random=new Random(currentTimestamp);
            long number=1000000000000000L + (long)(random.nextDouble() * 9000000000000000L);
            return number;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
