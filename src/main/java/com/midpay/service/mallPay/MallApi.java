package com.midpay.service.mallPay;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.midpay.constant.PayWayEnum;
import com.midpay.constant.RedisKeyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Optional;

import static com.midpay.service.mallPay.MallProperties.*;


@Component
@Slf4j
public class MallApi {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MallThreadLocal mallThreadLocal;

    /**
     * https://api.it120.cc/yishi/user/m/
     * 登录接口
     *
     * @return token
     */
    public String login() {
        EnvProperties envProperties = mallThreadLocal.getLocalMallEnv();
        EnvUser envUser = mallThreadLocal.getLocalMallUser();

        String reqUrl = envProperties.getBaseUrl() + LOGIN_PATH;
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("mobile", envUser.getMobile());
        formData.add("pwd", envUser.getPwd());
        formData.add("deviceId", "tianshitongzhuang");
        formData.add("deviceName", "h5");

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 创建HttpEntity对象，包含表单数据和请求头
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        // 发送POST请求
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(reqUrl, HttpMethod.POST, requestEntity, JSONObject.class);
        String token = this.extractBody(responseEntity).getString("token");

        String redisKeySuffix = ":" + envProperties.getEnv() + ":" + envUser.getMobile();
        this.setCache(token, RedisKeyEnum.API_MALL_TOKEN, redisKeySuffix);
        return token;
    }

    /**
     * 获取token
     *
     * @return token信息
     */
    public String getToken() {
        EnvProperties envProperties = mallThreadLocal.getLocalMallEnv();
        EnvUser envUser = mallThreadLocal.getLocalMallUser();
        String redisKeySuffix = ":" + envProperties.getEnv() + ":" + envUser.getMobile();

        String token = stringRedisTemplate.opsForValue().get(RedisKeyEnum.API_MALL_TOKEN.getKey() + redisKeySuffix);
        if (StringUtils.hasText(token)) {
            return token;
        }

        return this.login();
    }

    /**
     * https://api.it120.cc/yishi/shop/goods/list/v2
     * 获取商品列表
     *
     * @return 商品列表
     */
    public JSONArray goodsList() {
        EnvProperties envProperties = mallThreadLocal.getLocalMallEnv();
        String redisKeySuffix = ":" + envProperties.getEnv();

        String goodsJSON = stringRedisTemplate.opsForValue().get(RedisKeyEnum.API_MALL_GOODS.getKey() + redisKeySuffix);
        if (StringUtils.hasText(goodsJSON)) {
            return JSONArray.parseArray(goodsJSON);
        }
        String reqUrl = envProperties.getBaseUrl() + GOODS_LIST_PATH;
        JSONObject request = JSONObject.of("recommendStatus", 1);
        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(reqUrl, request, JSONObject.class);
        JSONArray goods = this.extractBody(responseEntity).getJSONArray("result");
        JSONArray rsGoods = new JSONArray();
        for (int i = 0; i < goods.size(); i++) {
            JSONObject origin = goods.getJSONObject(i);
            JSONObject target = new JSONObject();
            target.put("id", origin.getString("id"));
            target.put("name", origin.getString("name"));
            target.put("price", new BigDecimal(origin.getString("minPrice")));
            rsGoods.add(target);
        }
        // 简化goods属性，设置缓存
        this.setCache(rsGoods.toJSONString(), RedisKeyEnum.API_MALL_GOODS, redisKeySuffix);
        return rsGoods;
    }

    /**
     * https://api.it120.cc/yishi/order/create
     * 创佳订单
     *
     * @return 订单id
     */
    public JSONObject createOrder(String goodsId) {
        EnvProperties envProperties = mallThreadLocal.getLocalMallEnv();
        String reqUrl = envProperties.getBaseUrl() + CREATE_ORDER_PATH;
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("token", this.getToken());
        formData.add("calculate", false);
        formData.add("goodsType", 0);
        formData.add("provinceId", "120000000000");
        formData.add("cityId", "120100000000");
        formData.add("districtId", "120102000000");
        formData.add("streetId", "120102001000");
        formData.add("address", "397 Xingang Middle Rd, KeCun");
        formData.add("linkMan", "John Doe");
        formData.add("mobile", "020-81167888");
        formData.add("goodsJsonStr", JSONArray.of(JSONObject.of("goodsId", goodsId, "number", 1)).toJSONString());

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 创建HttpEntity对象，包含表单数据和请求头
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(reqUrl, HttpMethod.POST, requestEntity, JSONObject.class);
        return this.extractBody(responseEntity);
    }

    /**
     * https://api.it120.cc/yishi/pay/wx/h5
     * 支付订单
     *
     * @return 支付订单
     */
    public JSONObject payOrder(JSONObject jsonObject, PayWayEnum payWayEnum) {
        EnvProperties envProperties = mallThreadLocal.getLocalMallEnv();
        BigDecimal money = new BigDecimal(jsonObject.getString("amountReal"));
        String orderId = jsonObject.getString("id");

        String reqUrl;
        switch (payWayEnum) {
            case ALI_QR:
                reqUrl = envProperties.getBaseUrl() + PAY_ORDER_PATH_ALI_QR;
                break;
            case WX_QR:
                reqUrl = envProperties.getBaseUrl() + PAY_ORDER_PATH_WX_QR;
                break;
            default:
                reqUrl = envProperties.getBaseUrl() + PAY_ORDER_PATH_ALI_QR;
                break;
        }
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("token", this.getToken());
        formData.add("money", money);
        formData.add("payName", "支付订单 ：" + orderId);
        formData.add("remark", "支付订单 ：" + orderId);
        formData.add("nextAction", JSONObject.of("type", 0, "id", orderId).toJSONString());

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 创建HttpEntity对象，包含表单数据和请求头
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(reqUrl, HttpMethod.POST, requestEntity, JSONObject.class);
        JSONObject resp = this.extractBody(responseEntity);

        switch (payWayEnum) {
            case ALI_QR:
                String payUrl = JSONObject.parseObject(resp.getString("qrcode")).getJSONObject("alipay_trade_precreate_response").getString("qr_code");
                resp.put("payUrl", payUrl);
                break;
            case WX_QR:
                resp.put("payUrl", resp.getString("codeUrl"));
                break;
            default:
                break;
        }

        return this.extractBody(responseEntity);
    }

    /**
     * https://api.it120.cc/yishi/order/detail?id=4333817&token=da1a6c90-dc95-490c-91be-37135812bb30&hxNumber=&peisongOrderId=
     * 获取订单
     *
     * @return 订单信息
     */
    public JSONObject getOrder(String orderId) {
        String reqUrl = mallThreadLocal.getLocalMallEnv().getBaseUrl() + String.format(GET_ORDER_PATH, orderId, getToken());
        ResponseEntity<JSONObject> responseEntity = restTemplate.getForEntity(reqUrl, JSONObject.class);
        JSONObject body = this.extractBody(responseEntity);
        return body.getJSONObject("orderInfo");
    }

    public void orderMapping(String mallOrderId, String midPayOrderId) {
        this.setCache(midPayOrderId, RedisKeyEnum.API_MALL_ORDER_MAPPING, ":" + mallOrderId);
    }

    private JSONObject extractBody(ResponseEntity<JSONObject> responseEntity) {
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            int code = Optional.ofNullable(responseEntity.getBody()).map(jsonObject -> jsonObject.getIntValue("code")).orElse(-1);
            if (0 != code) {
                String msg = "商城接口调用失败：json返回 " + responseEntity.getBody();
                log.error(msg);
                throw new RuntimeException(msg);
            }
            return responseEntity.getBody().getJSONObject("data");
        } else {
            String msg = "商城接口调用失败：http返回码 " + responseEntity.getStatusCode();
            log.error(msg);
            throw new RuntimeException("商城接口调用失败：http返回码 " + responseEntity.getStatusCode());
        }
    }

    private void setCache(String v, RedisKeyEnum redisKeyEnum) {
        this.setCache(v, redisKeyEnum, "");
    }

    private void setCache(String v, RedisKeyEnum redisKeyEnum, String keySuffix) {
        if (null != redisKeyEnum.getExpired()) {
            stringRedisTemplate.opsForValue().set(redisKeyEnum.getKey() + keySuffix, v, redisKeyEnum.getExpired(), redisKeyEnum.getTimeUnit());
        } else {
            stringRedisTemplate.opsForValue().set(redisKeyEnum.getKey() + keySuffix, v);
        }
    }
}
