package com.dada.cloudpushwebsocket.service.channel;

import com.dada.api.constants.RedisPrefix;
import com.dada.api.entity.Client;
import com.dada.api.util.DateUtils;
import com.dada.api.util.ObjUtils;
import com.dada.cloudpushwebsocket.constants.AttrConstants;
import com.dada.cloudpushwebsocket.config.ComConfig;
import com.dada.cloudpushwebsocket.task.UpdateRedisChannelActiveTimeTask;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 维护客户端集合的操作类
 */
@Slf4j
@Service
public class ChannelService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ComConfig config;
    @Autowired
    private UpdateRedisChannelActiveTimeTask updateRedisChannelActiveTimeTask;

    //初始化1000容量，减少扩容
    private static Map<String,Channel> channels = new ConcurrentHashMap(1000);

    /**
     * 根据channelId获取单个对象
     * @param channelId
     * @return
     */
    public Channel get(String channelId){
        return channels.get(channelId);
    }

    /**
     * 获取当前websocket节点所维护的所有客户端websocket连接对象
     * @return
     */
    public Map<String,Channel> getAll(){
        return channels;
    }

    /**
     * 客户端websocket连接上服务器
     * @param channelId
     * @param channel
     * @return
     */
    public Channel put(String channelId,Channel channel){
        try {
            //缓存服务端与客户端关联信息
            redisTemplate.opsForSet().add(RedisPrefix.PREFIX_SERVERCLIENTS+config.getInstanceId(),channelId);
            //给channel对象绑定客户端channelId标识
            channel.attr(AttrConstants.channelId).set(channelId);
            //给channel对象绑定客户端sessionId会话标识，一次连接保持一致的值
            channel.attr(AttrConstants.sessionId).set(config.getInstanceId()+"_"+channelId+"_"+DateUtils.getCurrentDateTimeFormat());
            //更新活跃时间
            channel.attr(AttrConstants.activeTime).set(System.currentTimeMillis()+"");

            redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    redisConnection.openPipeline();
                    //缓存客户端信息
//                    redisTemplate.opsForHash().putAll(RedisPrefix.PREFIX_CLIENT+channelId, ObjUtils.ObjToMap(new Client(channelId,config.getInstanceId())));
                    redisConnection.hMSet((RedisPrefix.PREFIX_CLIENT+channelId).getBytes(), ObjUtils.ObjToByteMap(new Client(channelId,config.getInstanceId())));
//                    redisTemplate.expire(RedisPrefix.PREFIX_CLIENT+channelId,config.getExpireTime(),TimeUnit.SECONDS);
                    redisConnection.expire((RedisPrefix.PREFIX_CLIENT+channelId).getBytes(),TimeUnit.SECONDS.toSeconds(config.getExpireTime()));
                    //缓存服务端与客户端关联信息
//                    redisTemplate.opsForSet().add(RedisPrefix.PREFIX_SERVERCLIENTS+config.getInstanceId(),channelId);
                    redisConnection.sAdd((RedisPrefix.PREFIX_SERVERCLIENTS+config.getInstanceId()).getBytes(),channelId.getBytes());
                    return null;
                }
            });
            log.info("加入了客户端：[{}]",channelId);
            channels.put(channelId,channel);
            return channel;
        }catch (Exception e){
            log.error("加入客户端失败:["+channelId+"]",e);
        }
        return null;
    }

    /**
     * 移除客户端
     * @param channelId
     */
    public void remove(String channelId){
        if(!StringUtils.isNotEmpty(channelId)){return;}
        Channel channel = get(channelId);
        if(channel==null){return;}
        try {
            String dateTime = channel.attr(AttrConstants.activeTime).get();
            //断开当前连接
            get(channelId).close();
            //删除自己节点维护的客户端列表
            channels.remove(channelId);
//            channel.closeFuture().addListener()
            //删除redis中维护的客户端信息
            redisTemplate.delete(RedisPrefix.PREFIX_CLIENT+channelId);
            //删除redis中客户端与host的关联关系
            redisTemplate.opsForSet().remove(RedisPrefix.PREFIX_SERVERCLIENTS+config.getInstanceId(),channelId);
            log.info("移除了客户端[{}],上一次的活跃时间为[{}]",
                    channelId,
                    StringUtils.isNotEmpty(dateTime)?DateUtils.dateToDateTime(new Date(Long.parseLong(dateTime))):"");
        }catch (Exception e){
            log.error("移除客户端失败["+channelId+"]",e);
        }
    }

    /**
     * 更新活跃时间
     * @param channel
     */
    public void updateActiveTime(Channel channel){
        //更新自己维护的信息
        channel.attr(AttrConstants.activeTime).set(System.currentTimeMillis()+"");
        //更新redis维护的信息
        //redisTemplate.opsForHash().put(RedisPrefix.PREFIX_CLIENT+channel.attr(AttrConstants.attrChannelId).get(),"lastActiveTime" ,DateUtils.dateToDateTime(now));
        updateRedisChannelActiveTimeTask.addChannel(channel.attr(AttrConstants.channelId).get());
    }

}
