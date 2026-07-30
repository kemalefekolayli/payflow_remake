package com.example.paytrans.payment;

import com.example.paytrans.payment.error.ErrorCodes;
import com.example.paytrans.payment.error.GlobalException;
import com.example.paytrans.payment.entity.OutboxEventEntity;
import com.example.paytrans.payment.dto.AddMoneyRequest;
import com.example.paytrans.payment.dto.CreateWalletRequest;
import com.example.paytrans.payment.dto.SendMoneyRequest;
import com.example.paytrans.payment.dto.TransactionResponse;
import com.example.paytrans.payment.dto.WalletResponse;
import com.example.paytrans.payment.enums.CurrencyEnum;
import com.example.paytrans.payment.enums.OutboxStatus;
import com.example.paytrans.payment.repository.OutboxEventRepository;
import com.example.paytrans.payment.service.TransactionService;
import com.example.paytrans.payment.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PaymentServiceApplication.class)
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void walletTransferPreservesOwnershipLockingAndIdempotencyBehavior() {
        WalletResponse sender = walletService.createWallet(
                CreateWalletRequest.builder().currency(CurrencyEnum.TL).build(),
                10L
        );
        WalletResponse receiver = walletService.createWallet(
                CreateWalletRequest.builder().currency(CurrencyEnum.TL).build(),
                20L
        );

        transactionService.addMoney(
                AddMoneyRequest.builder()
                        .amount(new BigDecimal("100.00"))
                        .idempotencyKey("top-up-1")
                        .build(),
                sender.getId(),
                10L
        );

        SendMoneyRequest transferRequest = SendMoneyRequest.builder()
                .receiverWalletId(receiver.getId())
                .amount(new BigDecimal("30.00"))
                .idempotencyKey("transfer-1")
                .build();

        TransactionResponse first = transactionService.sendMoney(transferRequest, sender.getId(), 10L);
        TransactionResponse replay = transactionService.sendMoney(transferRequest, sender.getId(), 10L);

        assertThat(replay.getId()).isEqualTo(first.getId());
        assertThat(outboxEventRepository.count()).isEqualTo(1);
        OutboxEventEntity outboxEvent = outboxEventRepository.findAll().get(0);
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvent.getRetryCount()).isZero();
        assertThat(outboxEvent.getAggregateId()).isEqualTo(first.getId());
        assertThat(outboxEvent.getPayload()).contains(first.getTransactionRef());
        assertThat(outboxEventRepository.findReadyEvents(10))
                .extracting(OutboxEventEntity::getId)
                .containsExactly(outboxEvent.getId());
        assertThat(walletService.getWalletDetail(10L, sender.getId()).getBalance())
                .isEqualByComparingTo("70.00");
        assertThat(walletService.getWalletDetail(20L, receiver.getId()).getBalance())
                .isEqualByComparingTo("30.00");

        assertThatThrownBy(() -> transactionService.addMoney(
                AddMoneyRequest.builder()
                        .amount(BigDecimal.ONE)
                        .idempotencyKey("unauthorized-top-up")
                        .build(),
                sender.getId(),
                999L
        ))
                .isInstanceOf(GlobalException.class)
                .extracting(exception -> ((GlobalException) exception).getErrorCode())
                .isEqualTo(ErrorCodes.WALLET_NOT_FOUND);
    }
}
