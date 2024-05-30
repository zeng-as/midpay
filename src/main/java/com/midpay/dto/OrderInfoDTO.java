package com.midpay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("支付订单信息")
public class OrderInfoDTO {

    @ApiModelProperty("订单支付流水号")
    private String orderId;

    @ApiModelProperty(value = "支付状态", allowableValues = "true, false")
    private String payStatus;
}
