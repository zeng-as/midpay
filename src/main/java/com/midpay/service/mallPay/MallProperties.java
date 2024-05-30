package com.midpay.service.mallPay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(value = "mall.pay")
@Data
public class MallProperties {
    private List<EnvProperties> envs;

    private String callbackUrl;
    public static String LOGIN_PATH = "/user/m/login";
    public static String GOODS_LIST_PATH = "/shop/goods/list/v2";
    public static String CREATE_ORDER_PATH = "/order/create";
    public static String PAY_ORDER_PATH_WX_QR = "/pay/wx/wxapp";
    public static String PAY_ORDER_PATH_ALI_QR = "/pay/alipay/gate/qrcode";
    public static String GET_ORDER_PATH = "/order/detail?id=%s&token=%s";

    @Data
    public static class EnvProperties {
        private String env;
        private String baseUrl;

        private List<EnvUser> users;
    }

    @Data
    public static class EnvUser {
        private String mobile;
        private String pwd;
    }
}
