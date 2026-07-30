package com.example.paytrans.payment.service;


import com.example.paytrans.payment.dto.*;
import com.example.paytrans.payment.error.ErrorCodes;
import com.example.paytrans.payment.error.GlobalException;
import com.example.paytrans.payment.entity.TransactionEntity;
import com.example.paytrans.payment.entity.WalletEntity;
import com.example.paytrans.payment.enums.TransactionStatus;
import com.example.paytrans.payment.enums.TransactionType;
import com.example.paytrans.payment.enums.WalletStatus;
import com.example.paytrans.payment.event.TransferCompletedEvent;
import com.example.paytrans.payment.repository.TransactionRepository;
import com.example.paytrans.payment.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final OutboxService outboxService;
    @Transactional
    public TransactionResponse addMoney(AddMoneyRequest req, Long walletId, Long userId){
        // todo : SYSTEM ACCOUNT FOR EVERY CURRENY AND USE IT AS THE DEBIT FOR THE LEGDER
        //  FOR NOW WE CREATE MONEY OUT OF THIN AIR WITH THIS ENDPOINT
        //check if idempotency key exists in db
        Optional<TransactionEntity> existing = transactionRepository.findByIdempotencyKey(req.getIdempotencyKey());
        if(existing.isPresent()){
            log.info("Idempotent request detected for key={}, returning existing transaction id={}",req.getIdempotencyKey(), existing.get().getId());
            return mapToResponse(existing.get());
        }

        // check if wallet exists
        WalletEntity walletEntity = walletRepository.findByIdWithLock(walletId).orElseThrow(() ->  new GlobalException(ErrorCodes.WALLET_NOT_FOUND));
        // check if sender wallet is owned by user
        if(!Objects.equals(walletEntity.getUserId(), userId)) throw new GlobalException(ErrorCodes.WALLET_NOT_FOUND);

        BigDecimal balanceBefore = walletEntity.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(req.getAmount());

        TransactionEntity transaction = TransactionEntity.builder()
                .transactionRef(UUID.randomUUID().toString())
                .type(TransactionType.CREDIT)
                .amount(req.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(TransactionStatus.COMPLETED)
                .description(req.getDescription() != null ? req.getDescription() : "Wallet top-up")
                .receiverWalletId(walletId)
                .idempotencyKey(req.getIdempotencyKey())
                .completedAt(LocalDateTime.now())
                .build();
        // Update wallet balance
        walletEntity.setBalance(balanceAfter);
        walletRepository.save(walletEntity);
        transaction = transactionRepository.save(transaction);

        log.info("Added {} to walletId={}, newBalance={}", req.getAmount(), walletId, balanceAfter);
        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse sendMoney(SendMoneyRequest req, Long senderId, Long userId){


        Optional<TransactionEntity> existing = transactionRepository.findByIdempotencyKey(req.getIdempotencyKey());
        if (existing.isPresent()) {
            log.info("Cant send money. Idempotent request detected for key={}, returning existing transaction id={}", req.getIdempotencyKey(), existing.get().getId());
            return mapToResponse(existing.get());
        }

        if (Objects.equals(senderId, req.getReceiverWalletId())) {
            throw new GlobalException(ErrorCodes.WALLET_SAME);
        }

        // todo: check daily limit here


        Long receiverId = req.getReceiverWalletId();

        Long firstLockId = Math.min(senderId, receiverId);
        Long secondLockId = Math.max(senderId, receiverId);

        WalletEntity firstLockedWallet = walletRepository.findByIdWithLock(firstLockId)
                .orElseThrow(() -> new GlobalException(ErrorCodes.WALLET_NOT_FOUND));

        WalletEntity secondLockedWallet = walletRepository.findByIdWithLock(secondLockId)
                .orElseThrow(() -> new GlobalException(ErrorCodes.WALLET_NOT_FOUND));

        WalletEntity senderWallet;
        WalletEntity receiverWallet;

        if (firstLockedWallet.getId().equals(senderId)) {
            senderWallet = firstLockedWallet;
            receiverWallet = secondLockedWallet;
        } else {
            senderWallet = secondLockedWallet;
            receiverWallet = firstLockedWallet;
        }

        if (!Objects.equals(senderWallet.getUserId(), userId)) {
            throw new GlobalException(ErrorCodes.WALLET_NOT_FOUND);
        }

        // check if sender is active
        if(!(senderWallet.getStatus() == WalletStatus.ACTIVE)) throw new GlobalException(ErrorCodes.WALLET_NOT_ACTIVE_SENDER);
        if(!(receiverWallet.getStatus() == WalletStatus.ACTIVE)) throw new GlobalException(ErrorCodes.WALLET_NOT_ACTIVE_RECEIVER);

        // currency check
        if (senderWallet.getCurrency() != receiverWallet.getCurrency()) {
            throw new GlobalException(ErrorCodes.TRANSACTION_CURRENCY_MISMATCH);
        }
        if (senderWallet.getBalance().compareTo(req.getAmount()) < 0) {
            BigDecimal missingAmount = req.getAmount().subtract(senderWallet.getBalance());
            throw new GlobalException(ErrorCodes.WALLET_BALANCE_NOT_ENOUGH ,"Balance not enough. Missing amount: " + missingAmount);
        }

        BigDecimal senderBalanceBefore = senderWallet.getBalance();
        BigDecimal senderBalanceAfter = senderBalanceBefore.subtract(req.getAmount());
        BigDecimal receiverBalanceBefore = receiverWallet.getBalance();
        BigDecimal receiverBalanceAfter = receiverBalanceBefore.add(req.getAmount());

        TransactionEntity transaction = TransactionEntity.builder()
                .transactionRef(UUID.randomUUID().toString())
                .type(TransactionType.TRANSFER)
                .amount(req.getAmount())
                .balanceBefore(senderBalanceBefore)
                .balanceAfter(senderBalanceAfter)
                .status(TransactionStatus.COMPLETED)
                .description(req.getDescription() != null ? req.getDescription() : "Wallet transfer")
                .senderWalletId(senderId)
                .receiverWalletId(req.getReceiverWalletId())
                .idempotencyKey(req.getIdempotencyKey())
                .completedAt(LocalDateTime.now())
                .build();

        senderWallet.setBalance(senderBalanceAfter);
        receiverWallet.setBalance(receiverBalanceAfter);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        transactionRepository.save(transaction);

        transaction = transactionRepository.save(transaction);

        LedgerEntryRequest ledgerRequest = LedgerEntryRequest.builder()
                .transactionId(transaction.getId())
                .senderWalletId(senderWallet.getId())
                .receiverWalletId(receiverWallet.getId())
                .amount(req.getAmount())
                .currency(senderWallet.getCurrency())
                .senderBalanceBefore(senderBalanceBefore)
                .senderBalanceAfter(senderBalanceAfter)
                .receiverBalanceBefore(receiverBalanceBefore)
                .receiverBalanceAfter(receiverBalanceAfter)
                .build();

        ledgerService.createLedgerEntry(ledgerRequest);

        TransferCompletedEvent transferCompletedEvent = new TransferCompletedEvent(
                UUID.randomUUID(),
                transaction.getId(),
                transaction.getTransactionRef(),
                senderWallet.getId(),
                receiverWallet.getId(),
                userId,
                req.getAmount(),
                senderWallet.getCurrency(),
                transaction.getCompletedAt()
        );
        outboxService.createTransferCompletedEvent(transferCompletedEvent);

        log.info("Transfer of {} from walletId={} to walletId={} completed. Sender balance: {}, Receiver balance: {}",
                req.getAmount(), senderId, req.getReceiverWalletId(),
                senderBalanceAfter, receiverBalanceAfter);

        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long transactionId) {
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new GlobalException(ErrorCodes.TRANSACTION_NOT_FOUND));
        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactionHistory(Long walletId, Pageable pageable) {
        Page<TransactionEntity> page = transactionRepository
                .findBySenderWalletIdOrReceiverWalletId(walletId, walletId, pageable);

        List<TransactionResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<TransactionResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private TransactionResponse mapToResponse(TransactionEntity transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionRef(transaction.getTransactionRef())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .senderWalletId(transaction.getSenderWalletId())
                .receiverWalletId(transaction.getReceiverWalletId())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }


}
