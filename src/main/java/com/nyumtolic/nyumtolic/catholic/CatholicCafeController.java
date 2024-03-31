package com.nyumtolic.nyumtolic.catholic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/catholic")
public class CatholicCafeController {

    private final CatholicCrawlerService catholicCrawlerService;

    @GetMapping("/menu")
    public String showCatholicMenu(Model model) {
        CatholicCafeTable catholicCafeTable = catholicCrawlerService.getCatholicCafeInfo();
        model.addAttribute("catholicCafeTable", catholicCafeTable);
        return "catholic/menu";
    }
}
