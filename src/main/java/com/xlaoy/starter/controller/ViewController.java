package com.xlaoy.starter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by Administrator on 2018/12/19 0019.
 */
@Controller
public class ViewController {

    @GetMapping("/index")
    public String index() {
        return "index";
    }

}
