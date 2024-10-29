package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
    // 필요한 커스텀 쿼리를 추가할 수 있습니다.
}
