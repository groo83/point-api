package com.groo83.point.event;

import com.groo83.point.domain.point.enums.TransactionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointEvent {

    @Getter
    public static class CreatePointTransaction {

        private final Long transactionId;
        private final Long memberId;
        private final Long amount;
        private final TransactionType type;

        public static CreatePointTransaction of(Long transactionId, Long memberId, Long amount, TransactionType type) {
            return new CreatePointTransaction(transactionId, memberId, amount, type);
        }

        public CreatePointTransaction(final Long transactionId, Long memberId, final Long amount, TransactionType type) {
            this.transactionId = transactionId;
            this.memberId = memberId;
            this.amount = amount;
            this.type = type;
        }
    }


    @Getter
    public static class CreatePointUseTransaction {

        private final Long memberId;
        private final Long amount;
        private final Long orderId;


        public static CreatePointUseTransaction of(Long memberId, Long amount, Long orderId) {
            return new CreatePointUseTransaction(memberId, amount, orderId);
        }

        public CreatePointUseTransaction(Long memberId, Long amount, Long orderId) {
            this.memberId = memberId;
            this.amount = amount;
            this.orderId = orderId;
        }
    }

}
