package com.xlaoy.starter.controller;

import com.xlaoy.starter.WebApplaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author yijun.zhang
 * @version 1.0
 * @date 2019/7/18 20:22
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplaction.class)
public class TestDoc {

    @Autowired
    private DocService docService;

    private Map<String, String> mapApis = new HashMap<>();

    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void test() {
        docService.setApi("/api/v2/psa/pc/room/saveRoomDetailsByRoomId");
        docService.setTitle("saveRoomDetailsByRoomId");
        docService.done();
    }
}
















