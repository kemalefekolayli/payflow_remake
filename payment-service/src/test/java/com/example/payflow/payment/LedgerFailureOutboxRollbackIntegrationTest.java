package com.example.payflow.payment;

import com.example.payflow.payment.dto.AddMoneyRequest;
import com.example.payflow.payment.dto.CreateWalletRequest;
import com.example.payflow.payment.dto.LedgerEntryRequest;
import com.example.payflow.payment.dto.SendMoneyRequest;
import com.example.payflow.payment.dto.WalletResponse;
import com.example.payflow.payment.enums.CurrencyEnum;
import com.example.payflow.payment.repository.LedgerRepository;
import com.example.payflow.payment.repository.OutboxEventRepository;
import com.example.payflow.payment.repository.TransactionRepository;
import com.example.payflow.payment.service.LedgerService;
import com.example.payflow.payment.service.TransactionService;
import com.example.payflow.payment.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = PaymentServiceApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:ledger_failure_outbox_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
)
class LedgerFailureOutboxRollbackIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private LedgerService ledgerService;

    @Test
    void ledgerFailureLeavesNoOutboxAndRollsBackTransfer() {
        WalletResponse sender = createWallet(10L);
        WalletResponse receiver = createWallet(20L);
        addMoney(sender, "100.00");

        when(ledgerService.createLedgerEntry(any(LedgerEntryRequest.class)))
                .thenThrow(new IllegalStateException("ledger failed"));

        assertThatThrownBy(() -> transactionService.sendMoney(
                SendMoneyRequest.builder()
                        .receiverWalletId(receiver.getId())
                        .amount(new BigDecimal("30.00"))
                        .idempotencyKey("ledger-failure-transfer")
                        .build(),
                sender.getId(),
                sender.getUserId()
        )).isInstanceOf(IllegalStateException.class);

        assertThat(outboxEventRepository.count()).isZero();
        assertThat(ledgerRepository.count()).isZero();
        assertThat(transactionRepository.count()).isEqualTo(1);
        assertThat(walletService.getWalletDetail(10L, sender.getId()).getBalance())
                .isEqualByComparingTo("100.00");
        assertThat(walletService.getWalletDetail(20L, receiver.getId()).getBalance())
                .isEqualByComparingTo("0.00");
    }

    private WalletResponse createWallet(Long userId) {
        return walletService.createWallet(
                CreateWalletRequest.builder().currency(CurrencyEnum.TL).build(),
                userId
        );
    }

    private void addMoney(WalletResponse wallet, String amount) {
        transactionService.addMoney(
                AddMoneyRequest.builder()
                        .amount(new BigDecimal(amount))
                        .idempotencyKey("ledger-failure-test-top-up")
                        .build(),
                wallet.getId(),
                wallet.getUserId()
        );
    }
}
