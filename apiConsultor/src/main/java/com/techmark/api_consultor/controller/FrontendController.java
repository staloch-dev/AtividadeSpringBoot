package com.techmark.api_consultor.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {
    @GetMapping("/api/frontend")
    public String frontend() {
        return "index";
    }
}
