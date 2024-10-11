package com.groo83.point.domain.point.api;

import com.groo83.point.common.dto.DataResponse;
import com.groo83.point.domain.point.dto.PointCancelReqDto;
import com.groo83.point.domain.point.dto.PointSaveReqDto;
import com.groo83.point.domain.point.dto.PointTransactionResDto;
import com.groo83.point.domain.point.dto.PointUseReqDto;
import com.groo83.point.domain.point.service.PointTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/member/{memberId}/point")
@RequiredArgsConstructor
public class PointTransactionController {

    private final PointTransactionService transactionService;

    @PostMapping("/save")
    @ResponseStatus(value = HttpStatus.CREATED)
    public DataResponse<PointTransactionResDto> savePoint(
            @PathVariable("memberId")  Long memberId,
            @Valid @RequestBody PointSaveReqDto reqDto) {
        PointTransactionResDto pointTransaction = transactionService.savePoint(memberId, reqDto);
        return DataResponse.create(pointTransaction);
    }

    @PostMapping("/use")
    @ResponseStatus(value = HttpStatus.CREATED)
    public DataResponse<PointTransactionResDto> usePoint(
            @PathVariable("memberId")  Long memberId,
            @Valid @RequestBody PointUseReqDto reqDto) {
        PointTransactionResDto pointTransaction = transactionService.usePoint(memberId, reqDto);
        return DataResponse.create(pointTransaction);
    }

    @PostMapping("/save-cancel")
    @ResponseStatus(value = HttpStatus.CREATED)
    public DataResponse<PointTransactionResDto> savedPointCancel(
            @PathVariable("memberId")  Long memberId,
            @Valid @RequestBody PointCancelReqDto reqDto) {
        PointTransactionResDto pointTransaction = transactionService.savedPointCancel(memberId, reqDto);
        return DataResponse.create(pointTransaction);
    }

    @PostMapping("/use-cancel")
    @ResponseStatus(value = HttpStatus.CREATED)
    public DataResponse<PointTransactionResDto> usedPointCancel(
            @PathVariable("memberId")  Long memberId,
            @Valid @RequestBody PointCancelReqDto reqDto) {
        PointTransactionResDto pointTransaction = transactionService.usedPointCancel(memberId, reqDto);
        return DataResponse.create(pointTransaction);
    }


}
