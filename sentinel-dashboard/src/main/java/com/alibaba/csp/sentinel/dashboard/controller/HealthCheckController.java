package com.alibaba.csp.sentinel.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController{

    @GetMapping(value = "/slb/health")
    public void health() {
        //do nothing
    }
}
