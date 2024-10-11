package com.groo83.point.domain.point;

import com.groo83.point.domain.member.MemberPoint;
import com.groo83.point.domain.member.dto.MemberPointReqDto;
import com.groo83.point.domain.member.repository.MemberPointRepository;
import com.groo83.point.domain.point.dto.PointSaveReqDto;
import com.groo83.point.domain.point.dto.PointUseReqDto;
import com.groo83.point.domain.point.enums.TransactionType;
import com.groo83.point.domain.point.repository.PointTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PointTransactionControllerTest {

    @Autowired
    private PointTransactionRepository pointRepository;

    @Autowired
    private MemberPointRepository memberRepository;

    @LocalServerPort
    int randomServerPort;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private Long memberId;

    private final String domain = "http://localhost:";
    @BeforeEach
    void setUp() {
        memberId = 999L;

        MemberPointReqDto memberPointRequestDto = MemberPointReqDto.builder()
                .memberId(memberId)
                .maximumLimit(100000L)
                .build();

        String url = domain + randomServerPort + "/api/member";

        // MemberPoint 초기 등록
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(url, memberPointRequestDto, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void testSavePoint() {
        // case
        Long saveAmount = 1000L;
        PointSaveReqDto requestDto = PointSaveReqDto.builder()
                .amount(saveAmount)
                .orderId(4L)
                .rewardType("ORDER")
                .build();

        // when
        String url = domain + randomServerPort + "/api/member/" + memberId + "/point/save";
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(url, requestDto, Object.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        List<PointTransaction> points = pointRepository.findAll();
        Optional<MemberPoint> member = memberRepository.findByMemberId(memberId);


        assertThat(points.get(0).getMemberId()).isEqualTo(memberId);
        assertThat(points.get(0).getAmount()).isEqualTo(requestDto.getAmount());
        assertThat(member.get().getPointBalance()).isEqualTo(saveAmount);

    }

    @Test
    void testUsePoint() {
        // case
        testSavePoint();
        Long useAmount = 700L;
        PointUseReqDto requestDto = PointUseReqDto.builder()
                .amount(useAmount)
                .orderId(5L)
                .build();
        Optional<MemberPoint> member = memberRepository.findByMemberId(memberId);
        Long useBeforeBalance = member.get().getPointBalance();

        // when
        String url = domain + randomServerPort + "/api/member/" + memberId + "/point/use";

        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(url, requestDto, Object.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Optional<PointTransaction> point = pointRepository.findByMemberIdAndOrderIdAndType(memberId, 5L, TransactionType.USE);
        Optional<MemberPoint> usedMember = memberRepository.findByMemberId(memberId);

        assertThat(point).isNotEmpty(); // 포인트적립건이 존재하는지 확인
        assertThat(member).isNotEmpty(); // 멤버가 존재하는지 확인
        assertThat(point.get().getMemberId()).isEqualTo(memberId);
        assertThat(usedMember.get().getPointBalance()).isEqualTo(useBeforeBalance - useAmount);

    }
}
