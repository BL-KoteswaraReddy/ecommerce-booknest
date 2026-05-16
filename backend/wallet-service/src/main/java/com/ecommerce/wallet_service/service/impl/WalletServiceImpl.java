package com.ecommerce.wallet_service.service.impl;
import com.ecommerce.wallet_service.dto.*;
import com.ecommerce.wallet_service.entity.Transaction;
import com.ecommerce.wallet_service.entity.Wallet;
import com.ecommerce.wallet_service.dto.event.NotificationEvent;
import com.ecommerce.wallet_service.enums.TransactionStatus;
import com.ecommerce.wallet_service.enums.TransactionType;
import com.ecommerce.wallet_service.exception.*;
import com.ecommerce.wallet_service.messaging.NotificationEventPublisher;
import com.ecommerce.wallet_service.repository.TransactionRepository;
import com.ecommerce.wallet_service.repository.WalletRepository;
import com.ecommerce.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Value("${wallet.max-balance:50000}")
    private double maxBalance;

    @Value("${wallet.min-add-amount:10}")
    private double minAddAmount;

    @Value("${wallet.max-add-amount:10000}")
    private double maxAddAmount;

    @Override
    public WalletDTO getWalletByUserId(Long userId) {
        log.info("Fetching wallet for user: {}", userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));
        return convertToDTO(wallet);
    }

    @Override
    public WalletDTO addMoney(AddMoneyRequest request) {
        log.info("Adding money to wallet for user: {}", request.getUserId());

        // Validate amount
        if (request.getAmount() < minAddAmount) {
            throw new InvalidAmountException("Minimum add amount is ₹" + minAddAmount);
        }
        if (request.getAmount() > maxAddAmount) {
            throw new InvalidAmountException("Maximum add amount per transaction is ₹" + maxAddAmount);
        }

        // Get or create wallet
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseGet(() -> createWalletEntity(request.getUserId()));

        // Check if wallet is active
        if (!wallet.getIsActive()) {
            throw new WalletTransactionException("Wallet is deactivated. Please contact support.");
        }

        // Check max balance limit
        double newBalance = wallet.getCurrentBalance() + request.getAmount();
        if (newBalance > maxBalance) {
            throw new WalletLimitExceededException("Wallet balance cannot exceed ₹" + maxBalance);
        }

        double balanceBefore = wallet.getCurrentBalance();

        // Update wallet balance
        wallet.setCurrentBalance(newBalance);
        wallet.setTotalCredited(wallet.getTotalCredited() + request.getAmount());
        wallet = walletRepository.save(wallet);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.CREDIT);
        transaction.setAmount(request.getAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Money added to wallet");
        transaction.setRemarks(request.getRemarks() != null ? request.getRemarks() : "Wallet top-up");
        transaction.setWallet(wallet);
        transaction = transactionRepository.save(transaction);

        log.info("Successfully added ₹{} to wallet for user {}. New balance: ₹{}",
                request.getAmount(), request.getUserId(), newBalance);

        notificationEventPublisher.publish(new NotificationEvent(
                request.getUserId(),
                request.getUserEmail(),
                "WALLET_CREDITED",
                "Wallet Credited",
                "₹" + request.getAmount() + " has been added to your wallet. Current balance: ₹" + newBalance,
                transaction.getTransactionId(),
                request.getUserEmail() != null
        ));

        return convertToDTO(wallet);
    }

    @Override
    public WalletPaymentResponse processPayment(WalletPaymentRequest request) {
        log.info("Processing wallet payment for user: {} for amount: ₹{}", request.getUserId(), request.getAmount());

        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + request.getUserId()));

        // Check if wallet is active
        if (!wallet.getIsActive()) {
            throw new WalletTransactionException("Wallet is deactivated. Please contact support.");
        }

        // Check sufficient balance
        if (wallet.getCurrentBalance() < request.getAmount()) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: ₹%.2f, Required: ₹%.2f",
                            wallet.getCurrentBalance(), request.getAmount())
            );
        }

        double balanceBefore = wallet.getCurrentBalance();
        double newBalance = balanceBefore - request.getAmount();

        // Update wallet balance
        wallet.setCurrentBalance(newBalance);
        wallet.setTotalDebited(wallet.getTotalDebited() + request.getAmount());
        wallet = walletRepository.save(wallet);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(request.getAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Payment for order " + request.getOrderNumber());
        transaction.setOrderNumber(request.getOrderNumber());
        transaction.setRemarks("Order payment");
        transaction.setWallet(wallet);
        transaction = transactionRepository.save(transaction);

        log.info("Successfully processed payment of ₹{} from wallet for user {}. New balance: ₹{}",
                request.getAmount(), request.getUserId(), newBalance);

        notificationEventPublisher.publish(new NotificationEvent(
                request.getUserId(),
                request.getUserEmail(),
                "WALLET_DEBITED",
                "Wallet Debited",
                "₹" + request.getAmount() + " has been debited from your wallet. Current balance: ₹" + newBalance,
                transaction.getTransactionId(),
                request.getUserEmail() != null
        ));

        return new WalletPaymentResponse(true, "Payment successful", newBalance);
    }

    @Override
    public TransactionDTO getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new WalletTransactionException("Transaction not found with id: " + transactionId));
        return convertToTransactionDTO(transaction);
    }

    @Override
    public TransactionDTO getTransactionByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new WalletTransactionException("Transaction not found with ID: " + transactionId));
        return convertToTransactionDTO(transaction);
    }

    @Override
    public TransactionResponseDTO getUserTransactions(Long userId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByWalletUserIdOrderByTransactionDateDesc(userId, pageable);
        return convertToTransactionResponseDTO(transactions);
    }

    @Override
    public TransactionResponseDTO getUserTransactionsByType(Long userId, String type, Pageable pageable) {
        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidAmountException("Invalid transaction type: " + type);
        }

        Page<Transaction> transactions = transactionRepository.findByWalletUserIdAndType(userId, transactionType, pageable);
        return convertToTransactionResponseDTO(transactions);
    }

    @Override
    public Double getWalletBalance(Long userId) {
        Double balance = walletRepository.getBalance(userId);
        return balance != null ? balance : 0.0;
    }

    @Override
    public WalletDTO createWallet(Long userId) {
        log.info("Creating wallet for user: {}", userId);

        if (walletRepository.existsByUserId(userId)) {
            throw new WalletTransactionException("Wallet already exists for user: " + userId);
        }

        Wallet wallet = createWalletEntity(userId);
        return convertToDTO(wallet);
    }

    @Override
    public void deactivateWallet(Long userId) {
        log.info("Deactivating wallet for user: {}", userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        wallet.setIsActive(false);
        walletRepository.save(wallet);
    }

    @Override
    public void activateWallet(Long userId) {
        log.info("Activating wallet for user: {}", userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        wallet.setIsActive(true);
        walletRepository.save(wallet);
    }

    private Wallet createWalletEntity(Long userId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrentBalance(0.0);
        wallet.setTotalCredited(0.0);
        wallet.setTotalDebited(0.0);
        wallet.setIsActive(true);
        return walletRepository.save(wallet);
    }

    private WalletDTO convertToDTO(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        dto.setId(wallet.getId());
        dto.setUserId(wallet.getUserId());
        dto.setCurrentBalance(wallet.getCurrentBalance());
        dto.setTotalCredited(wallet.getTotalCredited());
        dto.setTotalDebited(wallet.getTotalDebited());
        dto.setIsActive(wallet.getIsActive());
        dto.setCreatedAt(wallet.getCreatedAt() != null ? wallet.getCreatedAt().toString() : null);
        dto.setUpdatedAt(wallet.getUpdatedAt() != null ? wallet.getUpdatedAt().toString() : null);
        return dto;
    }

    private TransactionDTO convertToTransactionDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setType(transaction.getType().name());
        dto.setAmount(transaction.getAmount());
        dto.setBalanceBefore(transaction.getBalanceBefore());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setStatus(transaction.getStatus().name());
        dto.setDescription(transaction.getDescription());
        dto.setReferenceId(transaction.getReferenceId());
        dto.setOrderNumber(transaction.getOrderNumber());
        dto.setTransactionDate(transaction.getTransactionDate() != null ? transaction.getTransactionDate().toString() : null);
        dto.setRemarks(transaction.getRemarks());
        return dto;
    }

    private TransactionResponseDTO convertToTransactionResponseDTO(Page<Transaction> page) {
        java.util.List<TransactionDTO> content = page.getContent().stream()
                .map(this::convertToTransactionDTO)
                .collect(Collectors.toList());

        return TransactionResponseDTO.builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
