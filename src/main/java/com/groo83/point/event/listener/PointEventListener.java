package com.groo83.point.event.listener;

import com.groo83.point.common.code.ErrorCode;
import com.groo83.point.domain.history.PointHistory;
import com.groo83.point.domain.history.service.PointHistoryService;
import com.groo83.point.domain.member.MemberPoint;
import com.groo83.point.domain.member.repository.MemberPointRepository;
import com.groo83.point.domain.point.PointTransaction;
import com.groo83.point.domain.point.repository.PointTransactionRepository;
import com.groo83.point.event.PointEvent;
import com.groo83.point.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointHistoryService historyService;
    private final PointTransactionRepository transactionRepository;
    private final MemberPointRepository memberRepository;

    @EventListener
    @Transactional
    public void handlePointEvent(PointEvent.CreatePointTransaction event) {

        log.info("Handling PointEvent for memberId: {}, amount: {}, type: {}", event.getTransactionId(), event.getAmount(), event.getType());
        try {
            // 1. PointHistory에 포인트 적립,적립/사용취소 내역 기록
            saveHistory(event);

            // 2. 사용자 포인트 보유액 적립/사용취소인 경우 증가, 적립취소인 경우 차감(amount 음수)
            calculateBalance(event);
        } catch (Exception e) {
            log.error("[ERROR] handling PointEvent for memberId: {}", event.getMemberId(), e);
            throw new BusinessException("Error handling PointEvent", e);
        }
    }

    @EventListener
    @Transactional
    public void handlePointUseEvent(PointEvent.CreatePointUseTransaction event) {

        log.info("Handling PointUseEvent for memberId: {}, pointAmount: {}", event.getMemberId(), event.getAmount());
        try {
            // 1. PointHistory에 포인트 적립 내역 기록
            saveHistory(event);

            // 2. 사용자 포인트 보유액 사용인 경우 차감(amount 음수)
            calculateBalance(event);
        } catch (Exception e) {
            log.error("[ERROR] handling PointUseEvent for memberId: {}", event.getMemberId(), e);
            throw new BusinessException("Error handling PointUseEvent", e);
        }
    }

    private void calculateBalance(PointEvent.CreatePointTransaction event) {
        // MemberPoint의 pointBalance 증가
        MemberPoint memberPoint = memberRepository.findByMemberId(event.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        memberPoint.calculateBalance(event.getAmount());
    }
    private void calculateBalance(PointEvent.CreatePointUseTransaction event) {
        // MemberPoint의 pointBalance 감소
        MemberPoint memberPoint = memberRepository.findByMemberId(event.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        memberPoint.calculateBalance(event.getAmount());
    }

    private void saveHistory(PointEvent.CreatePointTransaction event) {
        PointTransaction pointTransaction = findPointTransactionEntity(event.getTransactionId());
        PointHistory history = PointHistory.createHistory(pointTransaction);

        historyService.registerHistory(history);
    }

    private void saveHistory(PointEvent.CreatePointUseTransaction event) {
        PointHistory history = PointHistory.createUseHistory(event);

        historyService.registerHistory(history);
    }

    private PointTransaction findPointTransactionEntity(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
    }
}
