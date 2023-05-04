package com.example.pictgram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PagesController {

	/**
	 * @return index.html(TOPページ)
	 */
    @RequestMapping("/")
    public String index() {
        return "pages/index";
    }
}