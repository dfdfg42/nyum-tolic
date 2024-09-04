package com.nyumtolic.nyumtolic.catholic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/catholic")
public class CatholicCafeController {

    private final CatholicCafeTableRepository catholicCafeTableRepository;

    // todo 메뉴 페이지 수정
    @GetMapping("/menu")
    public String showCatholicMenu(Model model) {
        List<CatholicCafeTable> catholicCafeTable = catholicCafeTableRepository.findAll();
        model.addAttribute("catholicCafeTable", catholicCafeTable);
        return "catholic/menu";
    }
}
