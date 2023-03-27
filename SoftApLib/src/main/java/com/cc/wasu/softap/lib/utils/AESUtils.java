package com.cc.wasu.softap.lib.utils;

import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    private static final String TAG = "chenchen";

    /**
     * 编码
     */
    private static final String ENCODING = "UTF-8";
    /**
     * 算法定义
     */
    private static final String AES_ALGORITHM = "AES";
    /**
     * 指定填充方式
     */
    private static final String CIPHER_PADDING = "AES/EBC/PKCS7Padding";
    private static final String CIPHER_CBC_PADDING = "AES/CBC/PKCS7Padding";
    /**
     * 偏移量(CBC中使用，增强加密算法强度)
     */
    private static final String IV_SEED = "1111111111111111";


    /**
     * AES加密
     *
     * @param content 待加密内容
     * @param aesKey  密码
     * @return
     */
    public static String encrypt(String content, String aesKey) {
        if (TextUtils.isEmpty(content)) {
            ApLog.d(TAG, "AES encrypt: the content is null!");
            return null;
        }
        try {
            //对密码进行编码
            byte[] bytes = aesKey.getBytes(ENCODING);
            //设置加密算法，生成秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(bytes, AES_ALGORITHM);
            // "算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance(CIPHER_PADDING);
            //选择加密
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            //根据待加密内容生成字节数组
            byte[] encrypted = cipher.doFinal(content.getBytes(ENCODING));
            //返回base64字符串
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     * @param aesKey  密码
     * @return
     */
    public static String decrypt(String content, String aesKey) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        try {
            //对密码进行编码
            byte[] bytes = aesKey.getBytes(ENCODING);
            //设置解密算法，生成秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(bytes, AES_ALGORITHM);
            // "算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance(CIPHER_PADDING);
            //选择解密
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            //先进行Base64解码

//                byte[] decodeBase64 = Base64Utils.decodeFromString(content);
            byte[] decodeBase64 = Base64.decode(content, Base64.DEFAULT);

            //根据待解密内容进行解密
            byte[] decrypted = cipher.doFinal(decodeBase64);
            //将字节数组转成字符串
            return new String(decrypted, ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * AES_CBC加密
     *
     * @param content 待加密内容
     * @param aesKey  密码
     * @return
     */
    public static String encryptCBC(String content, String aesKey) {
        if (TextUtils.isEmpty(content)) {
            ApLog.d(TAG, "AES_CBC encrypt: the content is null!");
            return null;
        }

        try {
            //对密码进行编码
            byte[] bytes = aesKey.getBytes(ENCODING);
            //设置加密算法，生成秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(bytes, AES_ALGORITHM);
            // "算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance(CIPHER_CBC_PADDING);
            //偏移
            IvParameterSpec iv = new IvParameterSpec(IV_SEED.getBytes(ENCODING));
            //选择加密
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            //根据待加密内容生成字节数组
            byte[] encrypted = cipher.doFinal(content.getBytes(ENCODING));
            //返回base64字符串
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * AES_CBC解密
     *
     * @param content 待解密内容
     * @param aesKey  密码
     * @return
     */
    public static String decryptCBC(String content, String aesKey) {
        if (TextUtils.isEmpty(content)) {
            ApLog.d(TAG, "AES_CBC decrypt: the content is null!");
            return "";
        }
        try {
            //对密码进行编码
            byte[] bytes = aesKey.getBytes(StandardCharsets.ISO_8859_1);
            //设置解密算法，生成秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(bytes, AES_ALGORITHM);
            //偏移
            IvParameterSpec iv = new IvParameterSpec(IV_SEED.getBytes(StandardCharsets.ISO_8859_1));
            // "算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance(CIPHER_CBC_PADDING);
            //选择解密
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            //先进行Base64解码
            byte[] decodeBase64 = Base64.decode(content, Base64.DEFAULT);

            //根据待解密内容进行解密
            byte[] decrypted = cipher.doFinal(decodeBase64);
            //将字节数组转成字符串
            return new String(decrypted, ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}