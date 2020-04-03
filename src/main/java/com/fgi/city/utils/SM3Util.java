package com.fgi.city.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.Security;

public class SM3Util {

    private volatile static SM3Util sm3Util;

    private SM3Util() {
    }

    public static SM3Util getInstance() {
        if (sm3Util == null) {
            synchronized (SM3Util.class) {
                if (sm3Util == null) {
                    sm3Util = new SM3Util();
                }
            }
        }
        return sm3Util;
    }

    /**
     * 指定key杂凑
     *
     * @param key key 十六进制编码
     * @param msg 待加密的数据
     * @return 十六进制编码
     */
    public static String SM3Hash(String key, String msg) throws DecoderException {
        Security.addProvider(new BouncyCastleProvider());
        byte[] dataByte = msg.getBytes();
        byte[] keyByte = Hex.decodeHex(key);
        KeyParameter keyParameter = new KeyParameter(keyByte);
        SM3Digest sm3Digest = new SM3Digest();
        HMac hMac = new HMac(sm3Digest);
        hMac.init(keyParameter);
        hMac.update(dataByte, 0, dataByte.length);
        byte[] result = new byte[hMac.getMacSize()];
        hMac.doFinal(result, 0);
        return Hex.encodeHexString(result);
    }

    /**
     * 不指定key杂凑
     *
     * @param msg 待加密的数据
     * @return 十六进制编码
     */
    public static String SM3Hash(String msg) {
        Security.addProvider(new BouncyCastleProvider());
        byte[] dataByte = msg.getBytes();
        SM3Digest sm3Digest = new SM3Digest();
        sm3Digest.update(dataByte, 0, dataByte.length);
        byte[] result = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(result, 0);
        return Hex.encodeHexString(result);
    }

    /**
     * 文件摘要
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    public static String SM3HashFile(String filepath) {
        FileUtil fileUtil = FileUtil.getInstance();
        byte[] dataByte = fileUtil.FileToByte(new File(filepath));
        if (dataByte == null) {
            // 文件读取失败
            return null;
        }
        Security.addProvider(new BouncyCastleProvider());
        SM3Digest sm3Digest = new SM3Digest();
        sm3Digest.update(dataByte, 0, dataByte.length);
        byte[] result = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(result, 0);
        return Hex.encodeHexString(result);
    }


    public static void main(String[] args) throws DecoderException {
        System.out.println(Hex.encodeHexString("123456".getBytes()));
        System.out.println(SM3Hash("313233343536", "中国").length());
        System.out.println(SM3Hash("中国"));
    }


}