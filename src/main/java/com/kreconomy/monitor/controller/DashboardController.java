package com.kreconomy.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "economy") String tab,
            Model model
    ) {
        model.addAttribute("activeTab", tab);
        if ("realestate".equals(tab)) {
            return "realestate/tab";
        }
        return "economy/tab";
    }
}
