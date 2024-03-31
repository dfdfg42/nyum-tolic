package com.nyumtolic.nyumtolic.crawler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatholicCafeTableRepository extends JpaRepository<CatholicCafeTable, Long> {
}
