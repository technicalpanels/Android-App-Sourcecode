package com.example.ntd.tpapplication;

import android.util.Base64;

import java.math.BigInteger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ntd on 01/03/2016.
 */
public class EncryptionClass {
    public static String HexToString(String hex) {
        try {
            byte[] bytes = new BigInteger(hex, 16).toByteArray();
            String decryptString = new String(bytes, "UTF-8");
            return decryptString;
        } catch (Exception e) {
            return "";
        }
    }

    public static String StringToHex(String original) {
        String hexString = "";
        for (char i : original.toCharArray()) {
            hexString += String.format("%02x", (int) i);
        }
        return hexString.toUpperCase();
    }

    private static String AESKey(String key) {
        String result = "0000000000000000" + key;
        result = result.substring(key.length());
        return result;
    }
    private static String BlowFish(String key) {
        if(key.length()<4) {
            String result = "0000" + key;
            result = result.substring(key.length());
            return result;
        }
        else
            return key;
    }
public static String byteToStr(byte byteValue)
{
     return String.format("%02x", byteValue);
}
    public static byte[] strToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
          data[i / 2] =  (byte)Integer.parseInt(s.substring(i,i+2), 16);

        }
        return data;

    }
    public static String encryptBlowFish(String key,  String value) { //String initVector,
        String result="";
        try {
           // key = BlowFish(key);

          //  IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));

            SecretKeySpec skeySpec = new SecretKeySpec(value.getBytes(), "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            byte[] encrypted = cipher.doFinal(key.getBytes());



            for (byte byteValue:encrypted) {
                result+=byteToStr(byteValue);
            }
            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    public static String decryptBlowFish(String key, String encrypted) {
        try {
           // key = BlowFish(key);
          //  IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));


            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] original = cipher.doFinal(encrypted.getBytes("UTF-8"));


            /*
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "BlowFish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] original = cipher.doFinal(encrypted.getBytes());
           */


            String result="";
            for (byte byteValue:original) {
                result+=byteToStr(byteValue);
            }
            return result;
         //   return new String(original);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public static String encryptAES(String key,  String value) {
        try {
            key = AESKey(key);
          //  IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(value.getBytes());

            int flags = Base64.NO_WRAP | Base64.URL_SAFE;
            System.out.println("encrypted string: "
                    + Base64.encodeToString(encrypted, flags));

            return Base64.encodeToString(encrypted, flags);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decryptAES(String key, String encrypted) {
        try {
            key = AESKey(key);
           // IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            int flags = Base64.NO_WRAP | Base64.URL_SAFE;
            byte[] original = cipher.doFinal(Base64.decode(encrypted, flags));
            return new String(original);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
//    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
//        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
//        byte[] encrypted = cipher.doFinal(clear);
//        return encrypted;
//    }
//    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
//        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
//        byte[] decrypted = cipher.doFinal(encrypted);
//        return decrypted;
//    }
}
