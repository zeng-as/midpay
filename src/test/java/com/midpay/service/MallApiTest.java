package com.midpay.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.midpay.constant.PayWayEnum;
import com.midpay.service.mallPay.MallApi;
import com.midpay.service.mallPay.MallProperties;
import com.midpay.service.mallPay.MallThreadLocal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MallApiTest {
    @Resource
    private MallApi mallApi;

    @Resource
    private MallProperties mallProperties;

    @Resource
    private MallThreadLocal mallThreadLocal;

    @Test
    public void loginTest() {
        String token = mallApi.getToken();
        System.out.println(token);
    }

    @Test
    public void goodsListTest() {
        mallThreadLocal.setRandomMallEnv();
        JSONArray objects = mallApi.goodsList();
        System.out.println(objects);
    }

    @Test
    public void createOrderTest() {
        JSONObject order = mallApi.createOrder("1759586");
        System.out.println(order);
    }

    @Test
    public void payOrderTest() {
        JSONObject order = mallApi.createOrder("1759586");
        JSONObject jsonObject = mallApi.payOrder(order, PayWayEnum.ALI_QR);
        System.out.println(jsonObject);
    }

    @Test
    public void getOrderTest() {
        JSONObject order = mallApi.getOrder("4337748");
        System.out.println(order);
    }

    @Test
    public void getProperties() {
        System.out.println(JSONObject.toJSONString(mallProperties));
    }
}
