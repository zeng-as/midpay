package com.midpay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("创建支付订单出参")
public class CreatePayOrderRespDTO {

    @ApiModelProperty("支付订单流水号")
    private String orderId;

    @ApiModelProperty("支付链接")
    private String payUrl;
}
