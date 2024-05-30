package com.midpay.service;


import com.alibaba.fastjson2.JSONObject;
import com.midpay.constant.PayWayEnum;

import java.math.BigDecimal;

public interface IMidPayService {

    /**
     * 获取默认支付方式
     *
     * @return 支付方式
     */
    PayWayEnum getDefaultPayWay();

    /**
     * 创建支付订单
     *
     * @param amount 支付金额
     * @return 支付订单信息
     */
    JSONObject createPayOrder(BigDecimal amount);

    /**
     * 创建支付订单
     *
     * @param amount 支付金额
     * @param payWay 支付方式
     * @return 支付订单信息
     */
    JSONObject createPayOrder(BigDecimal amount, String payWay);

    /**
     * 获取支付状态
     *
     * @param orderId 支付订单id
     * @return 0 未支付成功 1 支付成功
     */
    String getPayStatus(String orderId);
}
