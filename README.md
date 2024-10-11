# 포인트 API

## 🗂️ Summary
- 적립, 적립취소, 사용, 사용취소 기능을 제공하는 추적 용이한 포인트 API
- 대량 데이터 고려 : 사용 가능 포인트 조회 시 Pagenation 처리
- 요구사항에 유연하게 대처하기 위한 추적 필드 구분
- 동시성 제어를 위한 낙관적 락(Optimistic Lock)
- 사용자 최대 보유 포인트 금액 DB 설정
- 1회 최대 적립 한도 yml 프로퍼티 관리


## 🛠️ 주요 기술
- Java 21
- SpringBoot 3.3.4
- Spring Data JPA
- JUnit5
- Swagger
- H2 DB


## 실행 방법
```shell
./gradlew bootRun
```


## Swagger 

URL : http://localhost:8080/swagger-ui/index.html

<img width="829" alt="swagger_index_241011" src="https://github.com/user-attachments/assets/24a7390b-28ad-47b8-a221-034c25a54a7a">

### 사용자 및 최대 보유 한도 등록

<img width="829" alt="swagger_member_register" src="https://github.com/user-attachments/assets/c5a57913-d337-4687-8556-e15477b96027">

### 사용자 보유 포인트 조회

<img width="829" alt="swagger_member_balanceAndMaximum_get" src="https://github.com/user-attachments/assets/4c46f67a-384b-4642-8926-5241a01a9deb">

### 포인트 적립

<img width="829" alt="swagger_point_save" src="https://github.com/user-attachments/assets/91215203-ed88-4386-a7f3-6bcd974191cc">

### 포인트 적립 취소

<img width="829" alt="swagger_point_saveCancel" src="https://github.com/user-attachments/assets/edd6f6b8-4d4f-40b4-a6ea-fb401b28bbd6">

### 포인트 사용

<img width="829" alt="swagger_point_use" src="https://github.com/user-attachments/assets/8e98cd21-119f-40ed-be58-e2837acad11d">

### 포인트 사용 취소

<img width="829" alt="swagger_point_useCancel" src="https://github.com/user-attachments/assets/95a49c3c-1825-4226-ae72-290a3ab10803">

## 💡 고려사항 및 해결방안
### 1. Update와 Delete가 없는, Insert 만 있는 도메인으로 구성
- 포인트 도메인 특성상 일종의 기업 부채 혹은 재화와 비슷한 의미로 사용되기 때문에, 이력을 정확하고 상세하게 관리
- 사용자의 포인트가 왜 이 금액이 되었는지 정확히 추적하기 위함

### 2. 포인트 사용처리 성능 최적화 
- Pagenation 통해 대량의 데이터를 한번에 로딩하지 않고 나눠서 조회함으로써 성능 최적화
- 사용 가능 포인트 추출 시 소스가 아닌 DB 레벨에서 추출
  - java 처리 시 대용량의 데이터를 메모리로 가져와서 계산해야하기 때문에 OutOfMemory 발생 방지를 위해 NativeQuery 사용

    ```java
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
     ```

### 3. 포인트 사용, 취소, 적립 취소 시 출처 트랜잭션 구분
- 각 필드는 특정 동작에 대한 추적만을 담당하게 하여, 의미적 충돌을 방지하고 코드를 보다 명확하게 유지
- 확장성 고려 : 추후 비즈니스 로직이 추가될 경우 필드가 명확하게 분리되어, 새로운 요구사항에 유연하게 대처 가능함으로써 유지보수성 향상

### 4. 서비스 레이어 결합도 낮추고 응집도 향상 
- 옵저버 패턴 사용 : 다른 도메인과 독립적으로 동작하도록 이벤트를 처리하여 결합도를 낮추고 응집도를 향상시킴으로써 유지 보수성 향상
### 5. 동시성 제어를 위한 낙관적 락(Optimistic Lock)
- @Version 을 사용하여 ObjectOptimisticLockingFailureException 발생 : Retry 정책 수립하여 처리 가능
