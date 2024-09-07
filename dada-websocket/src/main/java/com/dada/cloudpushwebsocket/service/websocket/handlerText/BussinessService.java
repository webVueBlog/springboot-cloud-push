package com.dada.cloudpushwebsocket.service.websocket.handlerText;

import com.alibaba.fastjson.JSONObject;
import com.dada.api.entity.WebsocketMessage;
import com.dada.api.util.RedisUtils;
import com.dada.cloudpushwebsocket.constants.AttrConstants;
import com.dada.cloudpushwebsocket.constants.MessageConstants;
import com.dada.cloudpushwebsocket.config.ComConfig;
import com.dada.cloudpushwebsocket.service.message.MessageSendService;
import com.dada.cloudpushwebsocket.util.WebsocketMessageGenerateUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 接收处理websocket消息的业务逻辑类
 */
@Service
@Slf4j
public class BussinessService implements IMessageService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MessageSendService messageSendService;
    @Autowired
    private ComConfig comConfig;

    @Override
    public void handler(Channel channel, WebsocketMessage websocketMessage) {
        if(!checkWsMessage(channel,websocketMessage)){
            return ;
        }
        String arr[] = websocketMessage.getTo();
        List<String> toClients = Arrays.asList(arr);
        Map<String,List<String>> hostClientsMap = new HashMap<>();
        List<Object> pipeResult = redisTemplate.executePipelined(RedisUtils.getClientHostByClientFromRedis(toClients));
        for (int i=0;i<toClients.size();i++) {
            //遍历list 依次推送消息
            //根据channelId找到对应的客户端对象所对应websocket服务的实例名
            String channelId = toClients.get(i);
            Object hostObj = pipeResult.get(i);
            if (hostObj == null) {
                log.info("不存在的客户端[{}]", channelId);
                continue;
            }
            String host = hostObj.toString();
            //如果所在的host正好是节点上，则直接发起推送
            if(comConfig.getInstanceId().equals(host)){
                messageSendService.sendMessage(channelId,websocketMessage);
                continue;
            }
            //如果不是，则请求其客户端所在的节点发起推送
            if (hostClientsMap.containsKey(host)) {
                hostClientsMap.get(host).add(channelId);
            } else {
                List<String> clients = new LinkedList<>();
                clients.add(channelId);
                hostClientsMap.put(host, clients);
            }
        }
    }

    private boolean checkWsMessage(Channel channel,WebsocketMessage websocketMessage) {
        String[] to = websocketMessage.getTo();
        if(to == null || to.length == 0){
            channel.writeAndFlush(
                    WebsocketMessageGenerateUtils.generateErrorWebsocketMessage(
                            channel,
                            MessageConstants.NoSendToError,
                            JSONObject.toJSONString(websocketMessage)));
            log.info("目标对象为空"+websocketMessage);
            return false;
        }
        websocketMessage.setTrigger(WebsocketMessage.Trigger.WEBSOCKET.code);
        websocketMessage.setMsgType(WebsocketMessage.MsgType.BUSSINESS.code);
        websocketMessage.setFrom(channel.attr(AttrConstants.channelId).get());
        return true;
    }
}
