package com.okeeper.performance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author zhangyue1
 */
@Controller
public class WebController {

    @RequestMapping("/")
    public String index(Model m){
        return "redirect:performance";
    }

    @RequestMapping("/performance")
    public String hello(Model m){
        m.addAttribute("now", DateFormat.getDateTimeInstance().format(new Date()));
        return "performance";
    }

    @RequestMapping("/performanceHttp")
    public String performanceHttp(Model m){
        m.addAttribute("now", DateFormat.getDateTimeInstance().format(new Date()));
        return "performanceHttp";
    }

    @RequestMapping("/uploadJar")
    public String uploadJar(Model m){
        m.addAttribute("now", DateFormat.getDateTimeInstance().format(new Date()));
        return "uploadJar";
    }
}
