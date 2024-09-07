package com.dada.cloudpushwebsocket.service.websocket.handlerText;

import com.dada.api.entity.WebsocketMessage;
import io.netty.channel.Channel;

/**
 * 各种消息类型对应的处理类
 */
public interface IMessageService {
    public void handler(Channel channel, WebsocketMessage websocketMessage);
}
