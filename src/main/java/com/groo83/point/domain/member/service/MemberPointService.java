package com.groo83.point.domain.member.service;

import com.groo83.point.common.code.ErrorCode;
import com.groo83.point.domain.member.MemberPoint;
import com.groo83.point.domain.member.dto.MemberPointReqDto;
import com.groo83.point.domain.member.dto.MemberPointResDto;
import com.groo83.point.domain.member.repository.MemberPointRepository;
import com.groo83.point.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPointService {

    private final MemberPointRepository memberRepository;

    @Transactional
    public MemberPointResDto registerMemberPoint(MemberPointReqDto dto) {
        if (memberRepository.existsByMemberId(dto.getMemberId())) {
            throw new BusinessException(ErrorCode.EXIST_MEMBER_ID);
        }

        MemberPoint savePoint = memberRepository.save(MemberPoint.toEntity(dto));
        return MemberPointResDto.fromEntity(savePoint);

    }

    public MemberPointResDto getMemberPoint(Long memberId) {
        return MemberPointResDto.fromEntity(findMember(memberId));
    }

    private MemberPoint findMember(Long memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void patchMaximumLimit(Long memberId, MemberPointReqDto dto) {
        MemberPoint member = findMember(memberId);
        member.updateMaximumLimit(dto.getMaximumLimit());
    }
}
