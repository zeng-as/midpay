package com.midpay.controller;


import com.alibaba.fastjson2.JSONObject;
import com.midpay.constant.RedisKeyEnum;
import com.midpay.service.mallPay.MallProperties;
import com.midpay.utils.LocalUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/tool")
@Api(tags = "工具接口")
public class ToolController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MallProperties mallProperties;

    @PostMapping("/refreshGoods")
    @ApiOperation(value = "刷新商品缓存")
    public String refreshGoods() {
        stringRedisTemplate.delete(RedisKeyEnum.API_MALL_GOODS.getKey());
        return "刷新成功";
    }

    @PostMapping("/decodeOrderId/{orderId}")
    @ApiOperation(value = "订单号解码")
    public String decodeOrderId(@PathVariable("orderId") String orderId) {
        return LocalUtil.base64Decode(orderId);
    }

    @PostMapping("/getMallProperties")
    @ApiOperation(value = "获取商城配置")
    public String getMallProperties() {
        return JSONObject.toJSONString(mallProperties.getEnvs());
    }
}
