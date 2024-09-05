package com.nyumtolic.nyumtolic.catholic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatholicCafeTableRepository extends JpaRepository<CatholicCafeTable, Long> {

    Optional<CatholicCafeTable> findByName(String name);

}
