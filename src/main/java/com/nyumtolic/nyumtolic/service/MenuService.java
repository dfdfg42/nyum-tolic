package com.nyumtolic.nyumtolic.service;


import com.nyumtolic.nyumtolic.domain.Menu;
import com.nyumtolic.nyumtolic.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public void save(Menu menu){
        menuRepository.save(menu);
    }

}
