package com.midpay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum RedisKeyEnum {
    API_MALL_TOKEN("api:mall:token", "电商平台登录token缓存", 15L, TimeUnit.MINUTES),
    API_MALL_GOODS("api:mall:goods", "电商平台商品缓存", 1L, TimeUnit.DAYS),
    API_MALL_ORDER_MAPPING("api:mall:orders", "电商平台订单映射缓存", 2L, TimeUnit.HOURS);
    final String key;
    final String desc;
    final Long expired;
    final TimeUnit timeUnit;
    }
