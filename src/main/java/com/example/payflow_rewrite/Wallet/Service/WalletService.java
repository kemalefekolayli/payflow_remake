package com.example.payflow_rewrite.Wallet.Service;


import com.example.payflow_rewrite.Auth.Exception.ErrorCodes;
import com.example.payflow_rewrite.Auth.Exception.GlobalException;
import com.example.payflow_rewrite.Wallet.Dto.CreateWalletRequest;
import com.example.payflow_rewrite.Wallet.Dto.WalletResponse;
import com.example.payflow_rewrite.Wallet.Entity.WalletEntity;
import com.example.payflow_rewrite.Wallet.Enums.WalletStatus;
import com.example.payflow_rewrite.Wallet.WalletRepository.WalletRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest req, Long userId){
        // check if wallet exists for that user for that currency
        if(walletRepository.existsByUserIdAndCurrency(userId, req.getCurrency())){
            throw new GlobalException(ErrorCodes.WALLET_ALREADY_EXIST);
        }

        WalletEntity wallet = WalletEntity.builder()
                .userId(userId)
                .currency(req.getCurrency())
                .build();

        walletRepository.save(wallet);

        return mapToResponse(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletDetail(Long userId, Long walletId) {
        WalletEntity wallet = walletRepository
                .findByUserIdAndId(userId, walletId)
                .orElseThrow(() ->
                        new GlobalException(ErrorCodes.WALLET_NOT_FOUND)
                );

        return mapToResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getWalletsByUserId(Long userId) {
        return walletRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    private WalletResponse mapToResponse(WalletEntity wallet){
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    @Transactional
    public Boolean freezeWallet(Long walletId, Long userId){
        return getaBoolean(walletId, userId, WalletStatus.ACTIVE, WalletStatus.FROZEN);
    }

    @Transactional
    public Boolean unfreezeWallet(Long walletId, Long userId){
        return getaBoolean(walletId, userId, WalletStatus.FROZEN, WalletStatus.ACTIVE);
    }

    private Boolean getaBoolean(Long walletId, Long userId, WalletStatus walletStatus, WalletStatus walletStatus2) {
        WalletEntity wallet = walletRepository
                .findByUserIdAndId(userId, walletId)
                .orElseThrow(() ->
                        new GlobalException(ErrorCodes.WALLET_NOT_FOUND)
                );
        if(wallet.getStatus() == walletStatus){
            wallet.setStatus(walletStatus2);
            return true;
        }
        else {
            throw new GlobalException(ErrorCodes.WALLET_ALREADY_FROZEN);
        }
    }
}
