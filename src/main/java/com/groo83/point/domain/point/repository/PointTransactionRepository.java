package com.groo83.point.domain.point.repository;

import com.groo83.point.domain.point.PointTransaction;
import com.groo83.point.domain.point.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    // 만료되지 않고, 취소, 사용되지 않은 포인트 + 부분 사용 취소된 포인트 합계 정렬하여 조회
    @Query(value = "SELECT pt.id, " +
            "pt.expired_date, " +
            "(pt.amount - " +
            "COALESCE((SELECT SUM(u.amount) FROM point_transaction u WHERE u.type = :useType AND u.used_point_id = pt.id), 0) + " +
            "COALESCE((SELECT SUM(uc.amount) FROM point_transaction uc WHERE uc.type = :useCancelType AND uc.cancel_point_id = pt.id), 0)" +
            ") AS availableAmount " +
            "FROM point_transaction pt " +
            "WHERE pt.member_id = :memberId " +
            "AND pt.type = :saveType " +
            "AND pt.expired_date > :currentDate " +
            "AND NOT EXISTS (SELECT 1 FROM point_transaction pc WHERE pc.type = :saveCancelType AND pc.original_point_id = pt.id) " +
            "HAVING (pt.amount - COALESCE((SELECT SUM(u.amount) FROM point_transaction u WHERE u.type = :useType AND u.used_point_id = pt.id), 0) + " +
            "COALESCE((SELECT SUM(uc.amount) FROM point_transaction uc WHERE uc.type = :useCancelType AND uc.used_point_id = pt.id), 0)) > 0 " +
            "ORDER BY pt.expired_date ASC",
            countQuery = "SELECT COUNT(*) FROM point_transaction",
            nativeQuery = true)
    Page<Object[]> findAvailablePoints(@Param("memberId") Long memberId,
                                               @Param("currentDate") LocalDateTime currentDate,
                                               @Param("saveType") String saveType,
                                               @Param("useType") String useType,
                                               @Param("useCancelType") String useCancelType,
                                               @Param("saveCancelType") String saveCancelType,
                                               Pageable pageable);

    // 적립 취소되지 않고, 사용되지 않은 적립건 추출
    @Query("SELECT pt FROM PointTransaction pt " +
            "WHERE pt.type = :saveType " +
            "AND pt.memberId = :memberId " +
            "AND pt.orderId = :orderId " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM PointTransaction u " +
            "  WHERE u.usedPointId = pt.id " +
            "  AND u.orderId = :orderId " +
            "  AND u.type = :useType" +
            ") " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM PointTransaction c " +
            "  WHERE c.originalPointId = pt.id " +
            "  AND c.orderId = :orderId " +
            "  AND c.type = :saveCancelType" +
            ")")
    PointTransaction findSavedTransactionsByOrderId(@Param("memberId") Long memberId,
                                                          @Param("orderId") Long orderId,
                                                          @Param("saveType") TransactionType saveType,
                                                          @Param("useType") TransactionType useType,
                                                          @Param("saveCancelType") TransactionType saveCancelType);

    // 특정 멤버의 특정 트랜잭션 타입에 해당하는 포인트 트랜잭션을 조회
    Optional<PointTransaction> findByMemberIdAndOrderIdAndType(Long memberId, Long orderId, TransactionType type);

    // 사용된 포인트에 대해 취소된 트랜잭션의 합계를 조회
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PointTransaction pt WHERE pt.cancelPointId = :usedTransactionId AND pt.type = :cancelType")
    Long findSumByUsedPointIdAndType(@Param("usedTransactionId") Long usedTransactionId, @Param("cancelType") TransactionType cancelType);
}
