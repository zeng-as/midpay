package com.midpay.utils;

import org.springframework.util.CollectionUtils;

import java.util.Base64;
import java.util.List;
import java.util.Random;

public class LocalUtil {

    public static <E> E randomExtract(List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    public static String base64Encode(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes());
    }

    public static String base64Decode(String s) {
        return new String(Base64.getDecoder().decode(s.getBytes()));
    }
}
