package com.dada.api.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.UUID;

@Data
public abstract class AbstractRequest {
    @ApiModelProperty(value="请求的唯一id" ,required=false)
    private String requestId = UUID.randomUUID().toString();
}
