package com.nyumtolic.nyumtolic;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;




@SpringBootTest
public class RestaurantRepositoryTest {

    private RestaurantRepository restaurantRepository;

    @Test
    public void testCreateAndFindRestaurant() {
        // 더미 데이터 생성
        Restaurant restaurant = new Restaurant();
        restaurant.setName("테스트 맛집");
        restaurant.setAddress("테스트 주소");
        restaurant.setPhoneNumber("010-0000-0000");
        restaurant.setRating(4.0);
        restaurant.setDescription("테스트 설명");

        restaurantRepository.save(restaurant);

        // 데이터 조회 테스트
        Restaurant found = restaurantRepository.findById(restaurant.getId()).get();
        assertThat(found.getName()).isEqualTo(restaurant.getName());
    }
}
