package com.xlaoy.starter.controller;

import com.xlaoy.starter.WebApplaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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


    @Test
    public void test() {
        docService.setTitle("酒店进程列表");
        docService.setApi("/api/v2/flows/search");
        docService.done();
    }
}
