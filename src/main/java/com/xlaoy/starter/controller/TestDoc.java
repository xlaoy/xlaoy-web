package com.xlaoy.starter.controller;

import com.xlaoy.starter.WebApplaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
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


    @Test
    public void test() {
        mapApis.put("/api/v2/psa/room/roomsInfoForPC/{id}", "获取客房详情(房型列表-房间整改列表-客房详情列表)");
        mapApis.put("/api/v2/psa/room/roomsRectifyForPC", "客房整改列表分页");

        mapApis.forEach((k, v) -> {

            docService.setTitle(v);
            docService.setApi(k);
            docService.done();
        });

        //System.out.println("run  done");
    }
}
















