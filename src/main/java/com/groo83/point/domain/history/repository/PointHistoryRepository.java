package com.groo83.point.domain.history.repository;

import com.groo83.point.domain.history.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
