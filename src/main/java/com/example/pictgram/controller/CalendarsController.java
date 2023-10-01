package com.example.pictgram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class CalendarsController {
    @GetMapping(path = "/calendars")
    public String index(Model model) {
        return "calendars/index";
    }
}
