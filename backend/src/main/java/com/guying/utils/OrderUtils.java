package com.guying.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class OrderUtils {
    public static String getOrderNo(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        StringBuilder orderNo = new StringBuilder(now.format(formatter));
        Random r = new Random();
        for(int i =0 ;i<4;i++){
            orderNo.append(r.nextInt(10));
        }
        return orderNo.toString();
    }
}
