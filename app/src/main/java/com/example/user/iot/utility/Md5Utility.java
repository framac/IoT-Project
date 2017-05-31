package com.example.user.iot.utility;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by user on 29/05/2017.
 */

public class Md5Utility {
    public static String encrypt(String password) {
        try{
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(password.getBytes());
            return String.format("%032x",new BigInteger(1,m.digest()));
        }
        catch(Exception e){
            return null;
        }
    }
}
