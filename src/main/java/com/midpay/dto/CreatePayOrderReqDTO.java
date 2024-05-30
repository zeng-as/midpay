package com.midpay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("创建支付订单入参")
public class CreatePayOrderReqDTO {

    @ApiModelProperty("支付金额")
    private BigDecimal amount;

    @ApiModelProperty(name = "支付", allowableValues = "ali_qr, wx_qr")
    private String payWay;
}
