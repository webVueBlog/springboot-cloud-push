package com.dada.cloudpushportal.service.messagedispatch;

import com.dada.api.entity.request.SendRequest;

/**
 * 消息分发接口定义
 */
public interface MessageDispatchService {
    void send(String instants,SendRequest request);
}
