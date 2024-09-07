package com.dada.cloudpushwebsocket.controller;

import com.dada.api.constants.CommonConsts;
import com.dada.api.entity.request.SendRequest;
import com.dada.api.entity.response.Response;
import com.dada.cloudpushwebsocket.service.MessageHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class MessageController {

    @Autowired
    private MessageHttpService messageHttpService;

    @RequestMapping("/message/send")
    public Response sendToAllClient(@RequestBody SendRequest request){
        messageHttpService.send(request);
        return new Response(CommonConsts.SUCCESS,CommonConsts.REQUST_SUC);
    }
}
