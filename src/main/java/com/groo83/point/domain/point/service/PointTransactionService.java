package com.groo83.point.domain.point.service;

import com.groo83.point.common.code.ErrorCode;
import com.groo83.point.config.PointPolicyProperties;
import com.groo83.point.domain.member.MemberPoint;
import com.groo83.point.domain.member.repository.MemberPointRepository;
import com.groo83.point.domain.point.PointTransaction;
import com.groo83.point.domain.point.dto.PointCancelReqDto;
import com.groo83.point.domain.point.dto.PointSaveReqDto;
import com.groo83.point.domain.point.dto.PointTransactionResDto;
import com.groo83.point.domain.point.dto.PointUseReqDto;
import com.groo83.point.domain.point.enums.TransactionType;
import com.groo83.point.domain.point.repository.PointTransactionRepository;
import com.groo83.point.event.PointEvent;
import com.groo83.point.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointTransactionService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PointPolicyProperties policyProperties;
    private final MemberPointRepository memberRepository;
    private final PointTransactionRepository pointRepository;

    @Transactional
    public PointTransactionResDto savePoint(Long memberId, PointSaveReqDto reqDto) {

        MemberPoint member = findMember(memberId);

        // 포인트 적립 전 검증
        validateSavePoint(member, reqDto.getAmount(), reqDto.getExpiredDate());

        // 포인트 적립
        PointTransaction savePoint = PointTransaction.createSavePoint(memberId, reqDto);
        pointRepository.save(savePoint);

        // 포인트 잔액 반영 및 히스토리 저장 이벤트 발행
        applicationEventPublisher.publishEvent(PointEvent.CreatePointTransaction.of(savePoint.getId(), savePoint.getMemberId(), savePoint.getAmount(), TransactionType.SAVE));

        return PointTransactionResDto.fromEntity(savePoint, member.getPointBalance());
    }

    @Transactional
    public PointTransactionResDto usePoint(Long memberId, PointUseReqDto reqDto) {
        MemberPoint member = findMember(memberId);

        // 포인트 잔액 검증
        if (member.getPointBalance() < reqDto.getAmount()) {
            throw new BusinessException(ErrorCode.POINT_NOT_ENOUGH);
        }

        Long remainingAmount = reqDto.getAmount();
        int page = 0;
        int size = 10;

        Pageable pageable = PageRequest.of(page, size);

        // 만료일 빠른 순으로 포인트 사용
        while (remainingAmount > 0) {
            Page<Object[]> availablePoints = pointRepository.findAvailablePoints(memberId,
                    LocalDateTime.now(),
                    TransactionType.SAVE.getCode(),
                    TransactionType.USE.getCode(),
                    TransactionType.USE_CANCEL.getCode(),
                    TransactionType.SAVE_CANCEL.getCode(), pageable);

            if (!availablePoints.hasContent()) {
                throw new BusinessException(ErrorCode.POINT_NOT_ENOUGH);
            }

            for (Object[] result : availablePoints.getContent()) {
                Long transactionId = (Long) result[0];
                Timestamp expiredTimestamp = (Timestamp) result[1];
                LocalDateTime expiredDate = expiredTimestamp.toLocalDateTime();
                Long availableAmount = Long.parseLong(String.valueOf(result[2])); // BigDecimal to Long

                Long amountToUse = Math.min(remainingAmount, availableAmount);

                // 포인트 사용 트랜잭션 생성
                PointTransaction useTransaction = PointTransaction.createUsePoint(memberId, reqDto, amountToUse, transactionId, expiredDate);
                pointRepository.save(useTransaction);

                remainingAmount -= amountToUse;

                if(remainingAmount <= 0) {
                    break;
                }

            }

            // 포인트가 남아있으면 다음 페이지 조회
            pageable = PageRequest.of(++page, size);
        }

        // 포인트 잔액 반영 및 히스토리 저장 이벤트 발행
        Long deductAmount = reqDto.getAmount() * -1L; // 음수 처리
        applicationEventPublisher.publishEvent(PointEvent.CreatePointUseTransaction.of(memberId, deductAmount, reqDto.getOrderId()));

        return PointTransactionResDto.builder()
                .memberId(memberId)
                .pointBalance(member.getPointBalance())
                .orderId(reqDto.getOrderId())
                .amount(deductAmount)
                .build();
    }

    @Transactional
    public PointTransactionResDto savedPointCancel(Long memberId, PointCancelReqDto reqDto) {
        MemberPoint member = findMember(memberId);

        // 주문 번호로 적립된 건 조회, 이미 적립 취소, 사용 처리된 건 제외
        PointTransaction savedTransaction = pointRepository.findSavedTransactionsByOrderId(
                        memberId,
                        reqDto.getOrderId(),
                        TransactionType.SAVE,
                        TransactionType.USE,
                        TransactionType.SAVE_CANCEL);

        if(savedTransaction == null) {
            throw new BusinessException(ErrorCode.INVALID_SAVED_POINT);
        }

        // 포인트 사용 취소
        Long deductAmount = savedTransaction.getAmount() * -1L; // 적립 취소 음수 처리
        PointTransaction savedPointCancel = PointTransaction.createSavedPointCancel(savedTransaction);
        pointRepository.save(savedPointCancel);

        // 포인트 잔액 반영 및 히스토리 저장 이벤트 발행
        applicationEventPublisher.publishEvent(PointEvent.CreatePointTransaction.of(savedPointCancel.getId(), memberId, deductAmount, TransactionType.SAVE_CANCEL));
        return PointTransactionResDto.fromEntity(savedPointCancel, member.getPointBalance());
    }

    @Transactional
    public PointTransactionResDto usedPointCancel(Long memberId, PointCancelReqDto reqDto) {
        MemberPoint member = findMember(memberId);

        // 사용한 포인트 트랜잭션 조회
        PointTransaction usedTransaction = pointRepository.findByMemberIdAndOrderIdAndType(
                        memberId, reqDto.getOrderId(), TransactionType.USE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USED_POINT));

        // 이미 취소된 포인트 계산
        long alreadyCanceledAmount = calculateCanceledPoints(usedTransaction.getUsedPointId());

        // 취소 가능한 포인트 계산 (사용된 포인트 - 이미 취소된 포인트)
        long availableCancelAmount = usedTransaction.getAmount() - alreadyCanceledAmount;

        if (availableCancelAmount < reqDto.getAmount()) {
            throw new BusinessException(ErrorCode.USED_POINT_OVER);
        }

        TransactionType type = TransactionType.USE_CANCEL;
        LocalDateTime expiredDate = usedTransaction.getExpiredDate();

        // 만료일 지난 경우 적립으로 처리
        if (expiredDate.isBefore(LocalDateTime.now())) {
            type = TransactionType.SAVE;
            expiredDate = LocalDateTime.now();
        }

        PointTransaction usedPointCancel = PointTransaction.createUsedPointCancel(usedTransaction, reqDto.getAmount(), type, expiredDate);
        pointRepository.save(usedPointCancel);

        // 포인트 잔액 반영 및 히스토리 저장 이벤트 발행
        applicationEventPublisher.publishEvent(PointEvent.CreatePointTransaction.of(usedPointCancel.getId(), memberId, usedPointCancel.getAmount(), type));
        return PointTransactionResDto.fromEntity(usedPointCancel, member.getPointBalance());
    }

    /**
     * 포인트 적립 검증
     * @param member
     * @param amount
     * @param expiredDate
     */
    private void validateSavePoint(MemberPoint member, final Long amount, final LocalDateTime expiredDate) {
        // 1. 1회 적립 가능 포인트 이하 검증
        Long oneTimeMaximumLimit = policyProperties.getOneTimeMaximumLimit();

        if(oneTimeMaximumLimit < amount) {
            throw new BusinessException(ErrorCode.POINT_SAVE_ONE_TIME_MAX_LIMIT);
        }

        // 2. 개인별 최대한도 초과 여부 검증
        if (member.getMaximumLimit() < member.getPointBalance() + amount) {
            throw new BusinessException(ErrorCode.USER_POINT_SAVE_MAX_LIMIT);
        }

        // 3. 만료일 1일 이상, 5년 미만 검증
        if (expiredDate != null) {
            validateExpiredDate(expiredDate);
        }
    }

    private MemberPoint findMember(Long memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateExpiredDate(LocalDateTime expiredDate) {
        LocalDateTime now = LocalDateTime.now();
        if (expiredDate.isBefore(now.plusDays(1)) || expiredDate.isAfter(now.plusYears(5))) {
            throw new BusinessException(ErrorCode.POINT_EXPIRED_DATE_INVALID);
        }
    }

    private long calculateCanceledPoints(Long usedTransactionId) {
        // 해당 사용 트랜잭션에 대해 이미 취소된 포인트 합산
        return pointRepository.findSumByUsedPointIdAndType(usedTransactionId, TransactionType.USE_CANCEL);
    }
}
