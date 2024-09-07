package com.dada.cloudpushwebsocket.task;

import com.dada.api.constants.RedisPrefix;
import com.dada.cloudpushwebsocket.config.ComConfig;
import com.dada.cloudpushwebsocket.constants.AttrConstants;
import com.dada.cloudpushwebsocket.service.channel.ChannelService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 扫描不在线的用户
 */
@Component
@EnableScheduling   // 2.开启定时任务
@Slf4j
public class ScanClientsNotOnline {

    @Autowired
    private ChannelService channelService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ComConfig config;

    private static Long now = null;

    @Scheduled(fixedRate=60000)
    private void execute() {
        Long startTime = System.currentTimeMillis();
        log.info("过期客户端扫描任务开启...");
        now = System.currentTimeMillis();
        Map<String,Channel> channels = channelService.getAll();
        if(CollectionUtils.isEmpty(channels)){
            log.info("没有客户端连接.");
            return ;
        }
        Channel channel = null;
        //存储需要删除的客户端
        List<String> delKeys = new ArrayList<>();
        for(String key : channels.keySet()){
            channel = channelService.get(key);
            //判断上一次的活跃时间
            if(outOfTime(channel)){
                //如果在离线就删除
                delKeys.add(key);
            }else{
                //如果在线就延长有效时间
                redisTemplate.expire(RedisPrefix.PREFIX_CLIENT+key,config.getExpireTime(),TimeUnit.SECONDS);
            }
        }
        //遍历key移除过期客户端连接
        for(String key : delKeys){
            channelService.remove(key);
        }
        redisTemplate.expire(RedisPrefix.PREFIX_SERVERCLIENTS+config.getInstanceId(),120,TimeUnit.SECONDS);
        log.info("过期客户端扫描任务执行完毕,踢出[{}]个过期客户端,剩余[{}]个,耗时[{}]ms",delKeys.size(),channels.size(),System.currentTimeMillis()-startTime);
    }

    private boolean outOfTime(Channel channel) {
        String activeTime = channel.attr(AttrConstants.activeTime).get();
        if(System.currentTimeMillis()-Long.parseLong(activeTime)>config.getIntervalTime()*1000){
            return true;
        }else{
            return false;
        }
    }
}
