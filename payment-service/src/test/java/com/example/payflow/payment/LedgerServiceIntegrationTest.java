package com.example.payflow.payment;

import com.example.payflow.payment.dto.AddMoneyRequest;
import com.example.payflow.payment.dto.CreateWalletRequest;
import com.example.payflow.payment.dto.LedgerResponse;
import com.example.payflow.payment.dto.SendMoneyRequest;
import com.example.payflow.payment.dto.TransactionResponse;
import com.example.payflow.payment.dto.WalletResponse;
import com.example.payflow.payment.enums.CurrencyEnum;
import com.example.payflow.payment.enums.TransactionDirection;
import com.example.payflow.payment.error.ErrorCodes;
import com.example.payflow.payment.error.GlobalException;
import com.example.payflow.payment.service.LedgerService;
import com.example.payflow.payment.service.TransactionService;
import com.example.payflow.payment.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PaymentServiceApplication.class)
@Transactional
class LedgerServiceIntegrationTest {

    private static final Long SENDER_USER_ID = 10L;
    private static final Long RECEIVER_USER_ID = 20L;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerService ledgerService;

    @Test
    void userCanViewOwnWalletLedger() {
        TransferFixture fixture = createTransferFixture();

        Page<LedgerResponse> page = ledgerService.getWalletLedger(
                fixture.senderWallet().getId(),
                SENDER_USER_ID,
                PageRequest.of(0, 20)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTransactionDirection())
                .isEqualTo(TransactionDirection.DEBIT);
        assertThat(page.getContent().get(0).getTransactionId())
                .isEqualTo(fixture.transaction().getId());
    }

    @Test
    void userCannotViewAnotherUsersWalletLedger() {
        TransferFixture fixture = createTransferFixture();

        assertThatThrownBy(() -> ledgerService.getWalletLedger(
                fixture.senderWallet().getId(),
                RECEIVER_USER_ID,
                PageRequest.of(0, 20)
        ))
                .isInstanceOf(GlobalException.class)
                .extracting(exception -> ((GlobalException) exception).getErrorCode())
                .isEqualTo(ErrorCodes.WALLET_NOT_FOUND);
    }

    @Test
    void transactionLedgerReturnsDebitAndCreditEntries() {
        TransferFixture fixture = createTransferFixture();

        List<LedgerResponse> entries = ledgerService.getTransactionLedger(
                fixture.transaction().getId(),
                RECEIVER_USER_ID
        );

        assertThat(entries).hasSize(2);
        assertThat(entries)
                .extracting(LedgerResponse::getTransactionDirection)
                .containsExactlyInAnyOrder(TransactionDirection.DEBIT, TransactionDirection.CREDIT);
        assertThat(entries)
                .extracting(LedgerResponse::getWalletId)
                .containsExactlyInAnyOrder(
                        fixture.senderWallet().getId(),
                        fixture.receiverWallet().getId()
                );
    }

    @Test
    void walletLedgerIsPaginatedAndSortedNewestFirst() {
        WalletResponse sender = createWallet(SENDER_USER_ID);
        WalletResponse receiver = createWallet(RECEIVER_USER_ID);
        addMoney(sender, new BigDecimal("100.00"));

        TransactionResponse first = transfer(sender, receiver, "1.00");
        TransactionResponse second = transfer(sender, receiver, "2.00");
        TransactionResponse third = transfer(sender, receiver, "3.00");

        Page<LedgerResponse> firstPage = ledgerService.getWalletLedger(
                sender.getId(),
                SENDER_USER_ID,
                PageRequest.of(0, 2)
        );
        Page<LedgerResponse> secondPage = ledgerService.getWalletLedger(
                sender.getId(),
                SENDER_USER_ID,
                PageRequest.of(1, 2)
        );

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent())
                .extracting(LedgerResponse::getTransactionId)
                .containsExactly(third.getId(), second.getId());
        assertThat(secondPage.getContent())
                .extracting(LedgerResponse::getTransactionId)
                .containsExactly(first.getId());
    }

    private TransferFixture createTransferFixture() {
        WalletResponse sender = createWallet(SENDER_USER_ID);
        WalletResponse receiver = createWallet(RECEIVER_USER_ID);
        addMoney(sender, new BigDecimal("100.00"));
        return new TransferFixture(sender, receiver, transfer(sender, receiver, "25.00"));
    }

    private WalletResponse createWallet(Long userId) {
        return walletService.createWallet(
                CreateWalletRequest.builder().currency(CurrencyEnum.TL).build(),
                userId
        );
    }

    private void addMoney(WalletResponse wallet, BigDecimal amount) {
        transactionService.addMoney(
                AddMoneyRequest.builder()
                        .amount(amount)
                        .idempotencyKey(UUID.randomUUID().toString())
                        .build(),
                wallet.getId(),
                wallet.getUserId()
        );
    }

    private TransactionResponse transfer(
            WalletResponse sender,
            WalletResponse receiver,
            String amount
    ) {
        return transactionService.sendMoney(
                SendMoneyRequest.builder()
                        .receiverWalletId(receiver.getId())
                        .amount(new BigDecimal(amount))
                        .idempotencyKey(UUID.randomUUID().toString())
                        .build(),
                sender.getId(),
                sender.getUserId()
        );
    }

    private record TransferFixture(
            WalletResponse senderWallet,
            WalletResponse receiverWallet,
            TransactionResponse transaction
    ) {
    }
}
