package com.dada.cloudpushwebsocket.service.websocket.handlerText;

import com.dada.api.entity.WebsocketMessage;
import com.dada.cloudpushwebsocket.constants.AttrConstants;
import com.dada.cloudpushwebsocket.service.message.MessageSendService;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 处理心跳websocket消息类型
 */
@Service
public class HeartBeatService implements IMessageService {

    @Autowired
    private MessageSendService messageSendService;

    @Override
    public void handler(Channel channel, WebsocketMessage websocketMessage) {
        websocketMessage.setMessageId(UUID.randomUUID().toString());
        //记录接收到的消息
        //给客户端发送心跳消息回执
        websocketMessage = getFrame(websocketMessage);
        messageSendService.sendMessage(channel.attr(AttrConstants.channelId).get(),websocketMessage);
    }

    /**
     * 构建心跳回执
     * @param websocketMessage
     * @return
     */
    private WebsocketMessage getFrame(WebsocketMessage websocketMessage){
        websocketMessage.setMsgType(WebsocketMessage.MsgType.HEARTBEAT_ACK.code);
        websocketMessage.setTrigger(WebsocketMessage.Trigger.WEBSOCKET.code);
        return websocketMessage;
    }
}
