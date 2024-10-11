package com.groo83.point.domain.member.api;

import com.groo83.point.common.dto.DataResponse;
import com.groo83.point.domain.member.dto.MemberPointReqDto;
import com.groo83.point.domain.member.dto.MemberPointResDto;
import com.groo83.point.domain.member.service.MemberPointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberPointController {

    private final MemberPointService memberService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public DataResponse<MemberPointResDto> register(@Valid @RequestBody MemberPointReqDto dto) {
        return DataResponse.create(memberService.registerMemberPoint(dto));
    }

    @GetMapping("/{memberId}")
    @ResponseStatus(value = HttpStatus.OK)
    public DataResponse<MemberPointResDto> getMemberPoint(@PathVariable("memberId")  Long memberId) {
        return DataResponse.create(memberService.getMemberPoint(memberId));
    }

    @PatchMapping("/{memberId}")
    @ResponseStatus(value = HttpStatus.OK)
    public DataResponse<ResponseEntity<String>> patchMaximumLimit(@PathVariable("memberId") Long memberId,
                                                    @Valid @RequestBody MemberPointReqDto dto) {
        memberService.patchMaximumLimit(memberId, dto);
        return DataResponse.create(ResponseEntity.ok("Maximum limit updated"));
    }

}
