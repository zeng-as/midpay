package com.midpay.controller;


import com.alibaba.fastjson2.JSONObject;
import com.midpay.constant.RedisKeyEnum;
import com.midpay.dto.CreatePayOrderReqDTO;
import com.midpay.dto.CreatePayOrderRespDTO;
import com.midpay.dto.OrderInfoDTO;
import com.midpay.service.MallMidPayService;
import com.midpay.service.mallPay.MallProperties;
import com.midpay.service.mallPay.MallThreadLocal;
import com.midpay.utils.LocalUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
@RequestMapping("/pay")
@Api(tags = "支付接口")
@Slf4j
public class MidPayController {

    @Resource
    private MallMidPayService mallMidPayService;

    @Resource
    private MallThreadLocal mallThreadLocal;

    @Resource
    private MallProperties mallProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RestTemplate restTemplate;

    @PostMapping("/createPayOrder")
    @ApiOperation(value = "创建支付订单")
    public CreatePayOrderRespDTO createPayOrder(@RequestBody CreatePayOrderReqDTO request) {
        // 设置商城环境
        mallThreadLocal.setRandomMallEnv();
        JSONObject rsObj;
        if (StringUtils.hasText(request.getPayWay())) {
            rsObj = mallMidPayService.createPayOrder(request.getAmount(), request.getPayWay());
        } else {
            rsObj = mallMidPayService.createPayOrder(request.getAmount());
        }
        return JSONObject.parseObject(rsObj.toJSONString(), CreatePayOrderRespDTO.class);
    }

    @GetMapping("/getPayStatus/{orderId}")
    @ApiOperation(value = "获取支付状态")
    public OrderInfoDTO getPayStatus(@PathVariable("orderId") @ApiParam("支付订单流水号") String orderId) {
        String[] split = LocalUtil.base64Decode(orderId).split("-");
        // 设置商城环境
        mallThreadLocal.setLocalMallEnv(split[0], split[1]);
        String payStatus = mallMidPayService.getPayStatus(split[2]);
        OrderInfoDTO orderInfoDTO = new OrderInfoDTO();
        orderInfoDTO.setOrderId(orderId);
        orderInfoDTO.setPayStatus(payStatus);
        return orderInfoDTO;
    }


    @PostMapping("/mallReceive")
    public String mallReceive(@RequestBody JSONObject jsonObject) {
        log.info("回调数据" + jsonObject.toJSONString());
        String type = jsonObject.getString("type");
        if ("paySuccess".equals(type)) {
            String callbackUrl = mallProperties.getCallbackUrl();
            // 回调处理，获取商城订单号
            if (StringUtils.hasText(callbackUrl)) {
                String orderId = JSONObject.parseObject(jsonObject.getJSONObject("payInfo").getString("nextAction")).getString("id");
                // 从redis获取商城订单号和midpay订单号的映射
                String midPayOrderId = stringRedisTemplate.opsForValue().get(RedisKeyEnum.API_MALL_ORDER_MAPPING + ":" + orderId);
                restTemplate.postForEntity(callbackUrl, JSONObject.of("orderId", midPayOrderId), Object.class);
            }
        }

        return "success";
    }
}
