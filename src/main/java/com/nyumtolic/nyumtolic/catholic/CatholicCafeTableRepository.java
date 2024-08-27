package com.nyumtolic.nyumtolic.catholic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatholicCafeTableRepository extends JpaRepository<CatholicCafeTable, Long> {
}
