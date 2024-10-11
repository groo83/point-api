package com.groo83.point.domain.point;

import com.groo83.point.domain.member.MemberPoint;
import com.groo83.point.domain.member.dto.MemberPointReqDto;
import com.groo83.point.domain.member.repository.MemberPointRepository;
import com.groo83.point.domain.point.dto.PointSaveReqDto;
import com.groo83.point.domain.point.dto.PointUseReqDto;
import com.groo83.point.domain.point.enums.TransactionType;
import com.groo83.point.domain.point.repository.PointTransactionRepository;
import com.groo83.point.domain.point.service.PointTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PointTransactionControllerTest {

    @Autowired
    private PointTransactionRepository pointRepository;

    @Autowired
    private MemberPointRepository memberRepository;

    @Autowired
    private PointTransactionService pointService;


    @LocalServerPort
    int randomServerPort;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private final Long memberId = 999L;

    private final String domain = "http://localhost:";

    @BeforeEach
    void setUp() {
        MemberPointReqDto memberPointRequestDto = MemberPointReqDto.builder()
                .memberId(memberId)
                .maximumLimit(100000L)
                .build();

        String url = domain + randomServerPort + "/api/member";

        // Member 등록
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(url, memberPointRequestDto, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("포인트 적립 시 사용자 포인트 잔액 확인")
    void testSavePoint() {
        // given
        Long saveAmount = 9000L;
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
    @DisplayName("포인트 사용 시 사용자 포인트 잔액 확인")
    void testUsePoint() {
        // given
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

    @Test
    @DisplayName("사용자가 동시에 포인트 사용을 시도한다.")
    void testUsePoint100Request() throws InterruptedException {
        // 스레드 1: 첫 번째 트랜잭션에서 동일한 엔티티를 조회하여 업데이트
        MemberPoint pointFromThread1 = memberRepository.findByMemberId(memberId).orElseThrow();
        pointFromThread1.calculateBalance(-50L); // 포인트 50 차감

        // 스레드 2: 두 번째 트랜잭션에서 동일한 엔티티를 조회하여 업데이트
        MemberPoint pointFromThread2 = memberRepository.findByMemberId(memberId).orElseThrow();
        pointFromThread2.calculateBalance(-30L); // 포인트 30 차감

        // 스레드 2가 먼저 커밋되었다고 가정 (트랜잭션에서 자동으로 적용됨)
        memberRepository.saveAndFlush(pointFromThread2);

        // 스레드 1이 나중에 커밋 시도 -> OptimisticLockException 발생
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            memberRepository.saveAndFlush(pointFromThread1);
        });
    }

}
