/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie.test;


import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author cjohnson
 */
public class CryptoTest {
//  public static void main2(String[] args) throws Exception {
//    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());        
//    byte[] input = " www.java2s.com ".getBytes();
//    byte[] keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
//        0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
//
//    SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
//    Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
//    System.out.println("input text : " + new String(input));
//
//    // encryption pass
//
//    byte[] cipherText = new byte[input.length];
//    cipher.init(Cipher.ENCRYPT_MODE, key);
//    int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
//    ctLength += cipher.doFinal(cipherText, ctLength);
//    System.out.println("cipher text: " + new String(cipherText) + " bytes: " + ctLength);
//
//    // decryption pass
//
//    byte[] plainText = new byte[ctLength];
//    cipher.init(Cipher.DECRYPT_MODE, key);
//    int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
//    ptLength += cipher.doFinal(plainText, ptLength);
//    System.out.println("plain text : " + new String(plainText) + " bytes: " + ptLength);
//  }
  
    public static void main(String[] argv) {
        try {
            System.out.println("START!:");

            //SecretKey key = KeyGenerator.getInstance("DES").generateKey();
            //Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            
            
            byte[] keyBytes = "123123asdasd4321".getBytes();
            byte[] iv = "22louie22louie22".getBytes();
            
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
           
            
            //sensitive information
            byte[] text = "No body can see me".getBytes();
            System.out.println("Text [Byte Format] : " + text);
            System.out.println("Text : " + new String(text));
            
            
            // Initialize the cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
            
            // Encrypt the text
            byte[] textEncrypted = cipher.doFinal(text);
            System.out.println("Text Encryted : " + textEncrypted);

            // Initialize the same cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(iv));
            // Decrypt the text
            byte[] textDecrypted = cipher.doFinal(textEncrypted);

            System.out.println("Text Decryted : " + new String(textDecrypted));

            
            
            
//            byte[] bytesOfMessage = yourString.getBytes("UTF-8");
//
//MessageDigest md = MessageDigest.getInstance("MD5");
//byte[] thedigest = md.digest(bytesOfMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

           