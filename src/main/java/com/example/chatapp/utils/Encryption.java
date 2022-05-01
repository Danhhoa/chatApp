package com.example.chatapp.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
public class Encryption {

    public static String encryptDataByAES (String data, Key secKey) {
        String result = "";
        try {
            //Mã hóa dữ liệu bằng AES
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secKey);
            byte[] byteEncryptedNew = cipher.doFinal(data.getBytes());
            String encryptedDataNew = Base64.getEncoder().encodeToString(byteEncryptedNew);
            result = encryptedDataNew;
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException i) {
            i.printStackTrace();
        }
        catch (IllegalBlockSizeException i) {
            i.printStackTrace();
        }
        catch (BadPaddingException i) {
            i.printStackTrace();
        }
        catch (InvalidKeyException i) {
            i.printStackTrace();
        }
        return result;
    }


    public static String encryptDataByRSA (String data, Key pubKey) {
        String result = "";
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, pubKey);
            byte encryptOutData[] = c.doFinal(data.getBytes());
            String encryptedData = Base64.getEncoder().encodeToString(encryptOutData);
            result = encryptedData;
        }
        catch (NoSuchPaddingException | IllegalBlockSizeException |
                NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }
}
