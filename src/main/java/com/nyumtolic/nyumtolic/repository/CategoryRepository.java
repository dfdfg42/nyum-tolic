package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
}
