package com.dada.cloudpushwebsocket.service.websocket.handlerText;

import com.alibaba.fastjson.JSONObject;
import com.dada.api.entity.WebsocketMessage;
import com.dada.cloudpushwebsocket.constants.AttrConstants;
import com.dada.cloudpushwebsocket.constants.MessageConstants;
import com.dada.cloudpushwebsocket.service.websocket.IWebSocketService;
import com.dada.cloudpushwebsocket.util.WebsocketMessageGenerateUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 处理客户端发过来的推送消息
 */
@Service
@Slf4j
public class TextWebSocketService implements IWebSocketService {

    @Autowired
    private MessageServiceStrategy messageServiceStrategy;

    @Override
    public void handler(ChannelHandlerContext ctx, WebSocketFrame frame) {
        String str = ((TextWebSocketFrame) frame).text();
        if (StringUtils.isEmpty(str)) {
            return ;
        }
        Channel channel = ctx.channel();
        String channelId = channel.attr(AttrConstants.channelId).get();
        String sessionId = channel.attr(AttrConstants.sessionId).get();
        log.info("receive[{}]:" + str,channelId);
        //按规定规则解析消息
        WebsocketMessage msg = init(str,channelId,sessionId);
        if(msg == null){
            ctx.channel().writeAndFlush(errorResponse(channel,str));
            return ;
        }
        //处理消息
        messageServiceStrategy.handler(channel,msg);
    }

    /**
     * 初始化消息类型
     * @param str
     * @param channelId
     * @return
     */
    private WebsocketMessage init(String str,String channelId,String sessionId) {
        try {
            WebsocketMessage msg = JSONObject.parseObject(str, WebsocketMessage.class);
            msg.setFrom(channelId);//设置消息来源
            msg.setSessionId(sessionId);
            msg.setTrigger(WebsocketMessage.Trigger.WEBSOCKET.code);//设置触发类型为websocket形式
            return msg;
        } catch (Exception e) {
            log.error("json解析失败,无法识别的消息:[{}]",str);
            return null;
        }
    }

    private TextWebSocketFrame errorResponse(Channel channel,String str){
        return WebsocketMessageGenerateUtils.generateResponse(
                WebsocketMessageGenerateUtils.generateErrorWebsocketMessage(
                        channel,
                        MessageConstants.ParseError,
                        str));
    }
}
