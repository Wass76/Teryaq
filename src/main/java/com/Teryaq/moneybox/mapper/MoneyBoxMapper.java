package com.Teryaq.moneybox.mapper;

import com.Teryaq.moneybox.dto.MoneyBoxRequestDTO;
import com.Teryaq.moneybox.dto.MoneyBoxResponseDTO;
import com.Teryaq.moneybox.dto.MoneyBoxTransactionResponseDTO;
import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.enums.MoneyBoxStatus;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoneyBoxMapper {
    
    public static MoneyBox toEntity(MoneyBoxRequestDTO request) {
        MoneyBox moneyBox = new MoneyBox();
        moneyBox.setInitialBalance(request.getInitialBalance());
        moneyBox.setCurrentBalance(request.getInitialBalance());
        moneyBox.setStatus(MoneyBoxStatus.OPEN); // Set default status to OPEN
        moneyBox.setCurrency("SYP"); // Always set to SYP for consistency
        return moneyBox;
    }
    
    public static MoneyBoxResponseDTO toResponseDTO(MoneyBox moneyBox) {
        return MoneyBoxResponseDTO.builder()
                .id(moneyBox.getId())
                .pharmacyId(moneyBox.getPharmacyId())
                .currentBalance(moneyBox.getCurrentBalance())
                .initialBalance(moneyBox.getInitialBalance())
                .lastReconciled(moneyBox.getLastReconciled())
                .reconciledBalance(moneyBox.getReconciledBalance())
                .status(moneyBox.getStatus())
                .currency(moneyBox.getCurrency())
                .baseCurrency(Currency.SYP)
                .totalBalanceInSYP(moneyBox.getCurrentBalance())
                .createdAt(moneyBox.getCreatedAt())
                .updatedAt(moneyBox.getUpdatedAt())
                .build();
    }
    
    public static MoneyBoxResponseDTO toResponseDTOWithTransactions(MoneyBox moneyBox, 
                                                                  List<MoneyBoxTransaction> transactions) {
        MoneyBoxResponseDTO response = toResponseDTO(moneyBox);
        
        if (transactions != null && !transactions.isEmpty()) {
            response.setRecentTransactions(transactions.stream()
                    .map(MoneyBoxMapper::toTransactionResponseDTO)
                    .collect(Collectors.toList()));
            response.setTotalTransactionCount(transactions.size());
        }
        
        return response;
    }
    
    public static MoneyBoxTransactionResponseDTO toTransactionResponseDTO(MoneyBoxTransaction transaction) {
        return toTransactionResponseDTO(transaction, null);
    }
    
    public static MoneyBoxTransactionResponseDTO toTransactionResponseDTO(MoneyBoxTransaction transaction, String userEmail) {
        return MoneyBoxTransactionResponseDTO.builder()
                .id(transaction.getId())
                .moneyBoxId(transaction.getMoneyBox().getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .referenceType(transaction.getReferenceType())
                .originalCurrency(transaction.getOriginalCurrency())
                .originalAmount(transaction.getOriginalAmount())
                .convertedCurrency(transaction.getConvertedCurrency())
                .convertedAmount(transaction.getConvertedAmount())
                .exchangeRate(transaction.getExchangeRate())
                .conversionTimestamp(transaction.getConversionTimestamp())
                .conversionSource(transaction.getConversionSource())
                .createdAt(transaction.getCreatedAt())
                .createdBy(transaction.getCreatedBy())
                .createdByUserEmail(userEmail)
                .build();
    }
    
    public static List<MoneyBoxTransactionResponseDTO> toTransactionResponseDTOList(List<MoneyBoxTransaction> transactions) {
        if (transactions == null) {
            return List.of();
        }
        
        return transactions.stream()
                .map(MoneyBoxMapper::toTransactionResponseDTO)
                .collect(Collectors.toList());
    }
}
