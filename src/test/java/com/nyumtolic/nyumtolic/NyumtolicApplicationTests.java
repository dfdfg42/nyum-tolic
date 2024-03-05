package com.nyumtolic.nyumtolic;

import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NyumtolicApplicationTests {

	@Autowired
	RestaurantService restaurantService;
	@Autowired
	RestaurantRepository restaurantRepository;
	@Autowired
	CategoryRepository categoryRepository;

	@AfterEach
	public void cleanup() {
		restaurantRepository.deleteAll();
		categoryRepository.deleteAll();
	}
	@Test
	void start(){
		cleanup();
	}


}
