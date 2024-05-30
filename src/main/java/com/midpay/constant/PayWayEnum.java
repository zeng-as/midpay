package com.midpay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
@AllArgsConstructor
public enum PayWayEnum {
    WX_QR("wx_qr", "微信二维码支付"),
    ALI_QR("ali_qr", "支付宝二维码支付"),
    ;
    final String key;
    final String desc;

    public static PayWayEnum keyOf(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        for (PayWayEnum e : PayWayEnum.values()) {
            if (key.equals(e.key)) {
                return e;
            }
        }

        return null;
    }
}
