package com.nyumtolic.nyumtolic.catholic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/catholic")
public class CatholicCafeController {

    private final CatholicCafeTableRepository catholicCafeTableRepository;

    @GetMapping("/menu")
    public String showCatholicMenu(Model model) {
        List<CatholicCafeTable> catholicCafeTable = catholicCafeTableRepository.findAll();
        for (CatholicCafeTable cafe : catholicCafeTable) {
            model.addAttribute(cafe.getName(), cafe.getLink());
            // "cafeMensa"만 별도로 리스트로 변환하여 전달
            if ("cafeMensa".equals(cafe.getName())) {
                List<String> cafeMensaList = Arrays.asList(cafe.getS3Link().split(","));
                model.addAttribute("cafeMensaList", cafeMensaList);
            } else {
                model.addAttribute(cafe.getName() + "JPG", cafe.getS3Link());
            }
        }
        return "catholic/menu";
    }
}
