package com.dada.cloudpushwebsocket.service.websocket.handlerPing;

import com.dada.cloudpushwebsocket.constants.AttrConstants;
import com.dada.cloudpushwebsocket.service.channel.ChannelService;
import com.dada.cloudpushwebsocket.service.websocket.IWebSocketService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PingWebSocketService implements IWebSocketService {

    @Autowired
    private ChannelService channelService;

    @Override
    public void handler(ChannelHandlerContext ctx,WebSocketFrame frame) {
        Channel channel = ctx.channel();
        log.info("[{}]Ping来了。。。。",channel.attr(AttrConstants.channelId).get());
        //写回pong响应
        ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
    }
}
