package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
}
