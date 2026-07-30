package com.example.paytrans.payment.service;

import com.example.paytrans.payment.dto.LedgerEntryRequest;
import com.example.paytrans.payment.dto.LedgerCreationResponse;
import com.example.paytrans.payment.dto.LedgerResponse;
import com.example.paytrans.payment.entity.LedgerEntity;
import com.example.paytrans.payment.enums.CurrencyEnum;
import com.example.paytrans.payment.enums.TransactionDirection;
import com.example.paytrans.payment.error.ErrorCodes;
import com.example.paytrans.payment.error.GlobalException;
import com.example.paytrans.payment.repository.LedgerRepository;
import com.example.paytrans.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final WalletRepository walletRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public LedgerCreationResponse createLedgerEntry(LedgerEntryRequest request) {

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

        return LedgerCreationResponse.builder()
                .debitEntryId(debitEntry.getId())
                .creditEntryId(creditEntry.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<LedgerResponse> getWalletLedger(Long walletId, Long userId, Pageable pageable) {
        walletRepository.findByUserIdAndId(userId, walletId)
                .orElseThrow(() -> new GlobalException(ErrorCodes.WALLET_NOT_FOUND));

        Pageable newestFirst = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        return ledgerRepository.findByWalletId(walletId, newestFirst)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<LedgerResponse> getTransactionLedger(Long transactionId, Long userId) {
        List<LedgerEntity> entries = ledgerRepository.findByTransactionIdOrderByIdAsc(transactionId);
        if (entries.isEmpty()) {
            throw new GlobalException(ErrorCodes.LEDGER_NOT_FOUND);
        }

        List<Long> involvedWalletIds = entries.stream()
                .map(LedgerEntity::getWalletId)
                .distinct()
                .toList();
        if (!walletRepository.existsByIdInAndUserId(involvedWalletIds, userId)) {
            throw new GlobalException(ErrorCodes.WALLET_NOT_FOUND);
        }

        return entries.stream()
                .map(this::mapToResponse)
                .toList();
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

    private LedgerResponse mapToResponse(LedgerEntity entry) {
        return LedgerResponse.builder()
                .id(entry.getId())
                .transactionId(entry.getTransactionId())
                .walletId(entry.getWalletId())
                .transactionDirection(entry.getTransactionDirection())
                .amount(entry.getAmount())
                .currency(entry.getCurrencyEnum())
                .balanceBefore(entry.getBalanceBefore())
                .balanceAfter(entry.getBalanceAfter())
                .build();
    }
}
