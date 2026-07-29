package com.example.payflow.payment.service;

import com.example.payflow.payment.dto.LedgerEntryRequest;
import com.example.payflow.payment.dto.LedgerResponse;
import com.example.payflow.payment.entity.LedgerEntity;
import com.example.payflow.payment.enums.CurrencyEnum;
import com.example.payflow.payment.enums.TransactionDirection;
import com.example.payflow.payment.error.ErrorCodes;
import com.example.payflow.payment.error.GlobalException;
import com.example.payflow.payment.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public LedgerResponse createLedgerEntry(LedgerEntryRequest request) {

        LedgerEntity debitEntry = LedgerEntity.builder()
                .transactionId(request.getTransactionId())
                .walletId(request.getSenderWalletId())
                .transactionDirection(TransactionDirection.DEBIT)
                .amount(request.getAmount())
                .currencyEnum(request.getCurrency())
                .balanceBefore(request.getSenderBalanceBefore())
                .balanceAfter(request.getSenderBalanceAfter())
                .build();

        LedgerEntity creditEntry = LedgerEntity.builder()
                .transactionId(request.getTransactionId())
                .walletId(request.getReceiverWalletId())
                .transactionDirection(TransactionDirection.CREDIT)
                .amount(request.getAmount())
                .currencyEnum(request.getCurrency())
                .balanceBefore(request.getReceiverBalanceBefore())
                .balanceAfter(request.getReceiverBalanceAfter())
                .build();

        List<LedgerEntity> entries = List.of(debitEntry, creditEntry);

        validateBalancedEntries(entries);

        ledgerRepository.saveAll(entries);

        return LedgerResponse.builder()
                .debitEntryId(debitEntry.getId())
                .creditEntryId(creditEntry.getId())
                .build();
    }
    private void validateBalancedEntries(List<LedgerEntity> entries) {
        BigDecimal totalDebit = entries.stream()
                .filter(entry -> entry.getTransactionDirection() == TransactionDirection.DEBIT)
                .map(LedgerEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = entries.stream()
                .filter(entry -> entry.getTransactionDirection() == TransactionDirection.CREDIT)
                .map(LedgerEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new GlobalException(ErrorCodes.LEDGER_NOT_BALANCED);
        }
    }
}
