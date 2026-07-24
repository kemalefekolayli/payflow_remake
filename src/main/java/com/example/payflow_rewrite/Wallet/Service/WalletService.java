package com.example.payflow_rewrite.Wallet.Service;


import com.example.payflow_rewrite.Wallet.Dto.CreateWalletRequest;
import com.example.payflow_rewrite.Wallet.Dto.WalletResponse;
import com.example.payflow_rewrite.Wallet.WalletRepository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest req){

    }
}
