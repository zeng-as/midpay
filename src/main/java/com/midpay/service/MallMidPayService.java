package com.midpay.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.midpay.constant.PayWayEnum;
import com.midpay.service.mallPay.MallApi;
import com.midpay.service.mallPay.MallThreadLocal;
import com.midpay.utils.LocalUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MallMidPayService implements IMidPayService {

    @Resource
    private MallApi mallApi;

    @Resource
    private MallThreadLocal mallThreadLocal;

    @Override
    public PayWayEnum getDefaultPayWay() {
        return PayWayEnum.ALI_QR;
    }

    @Override
    public JSONObject createPayOrder(BigDecimal amount) {
        return this.createPayOrder(amount, getDefaultPayWay().getKey());
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public JSONObject createPayOrder(BigDecimal amount, String payWay) {
        PayWayEnum payWayEnum = PayWayEnum.keyOf(payWay);
        if (null == payWayEnum) {
            throw new RuntimeException("暂不支持该支付方式：" + payWay);
        }

        // 获取订单列表
        JSONArray goodsList = mallApi.goodsList();
        Map<String, List<Object>> priceMap = goodsList.stream().collect(Collectors.groupingBy(o -> {
            JSONObject g = (JSONObject) o;
            return g.getBigDecimal("price").stripTrailingZeros().toPlainString();
        }));
        List<Object> amtGoodsList = priceMap.get(amount.stripTrailingZeros().toPlainString());
        if (CollectionUtils.isEmpty(amtGoodsList)) {
            throw new RuntimeException("创建订单失败：无对应金额【" + amount + "】可用商品信息");
        }

        // 随机获取商品
        JSONObject goods = (JSONObject) LocalUtil.randomExtract(amtGoodsList);

        // 创建订单
        JSONObject order = mallApi.createOrder(goods.getString("id"));

        // 创建支付订单
        JSONObject payOrder = mallApi.payOrder(order, payWayEnum);

        // 返回信息
        JSONObject rs = new JSONObject();
        String orderId = order.getString("id");
        String midPayOrderId = LocalUtil.base64Encode(mallThreadLocal.getLocalMallEnv().getEnv() + "-" + mallThreadLocal.getLocalMallUser().getMobile() +
                "-" + orderId);

        // 商城订单与midpay订单存redis映射
        mallApi.orderMapping(orderId, midPayOrderId);

        rs.put("orderId", midPayOrderId);
        rs.put("payUrl", payOrder.getString("payUrl"));
        return rs;
    }

    @Override
    public String getPayStatus(String orderId) {
        JSONObject order = mallApi.getOrder(orderId);
        return order.getString("isPay");
    }
}
