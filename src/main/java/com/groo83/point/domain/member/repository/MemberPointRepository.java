package com.groo83.point.domain.member.repository;

import com.groo83.point.domain.member.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberPointRepository extends JpaRepository<MemberPoint, Long> {
    Optional<MemberPoint> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId); // 사용자 중복 방지
}
