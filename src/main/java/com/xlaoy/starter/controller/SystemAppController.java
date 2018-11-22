package com.xlaoy.starter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2018/6/22 0022.
 */
@RestController
public class SystemAppController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    Environment env;


    @GetMapping("/system_app/get_property")
    public String getProperty(@RequestParam("key") String key) {
        return env.getProperty(key);
    }


}
