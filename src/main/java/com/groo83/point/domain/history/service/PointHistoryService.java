package com.groo83.point.domain.history.service;

import com.groo83.point.domain.history.PointHistory;
import com.groo83.point.domain.history.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

    private final PointHistoryRepository historyRepository;

    @Transactional
    public void registerHistory(PointHistory history) {
        historyRepository.save(history);
    }
}
