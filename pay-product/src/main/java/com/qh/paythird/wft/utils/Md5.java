package com.qh.paythird.wft.utils;


import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;

public class Md5 {
    public static String getMd5ofStr(String inbuf) {
        try {
            return DigestUtils.md5Hex(inbuf.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            return inbuf;
        }
    }
}

