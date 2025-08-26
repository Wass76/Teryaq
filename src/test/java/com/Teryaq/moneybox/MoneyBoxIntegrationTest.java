package com.Teryaq.moneybox;

import com.Teryaq.moneybox.service.MoneyBoxService;
import com.Teryaq.moneybox.service.ExchangeRateService;
import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.Enum.MoneyBoxStatus;
import com.Teryaq.moneybox.Enum.PeriodType;
import com.Teryaq.moneybox.Enum.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Simple test to verify MoneyBox integration components
 */
@SpringBootTest
@ActiveProfiles("test")
public class MoneyBoxIntegrationTest {

    @Test
    void testMoneyBoxEntityCreation() {
        // Test that entities can be created without compilation errors
        MoneyBox moneyBox = MoneyBox.builder()
            .pharmacyId(1L)
            .businessDate(LocalDate.now())
            .status(MoneyBoxStatus.OPEN)
            .periodType(PeriodType.DAILY)
            .openingBalance(new BigDecimal("50000"))
            .openedAt(LocalDateTime.now())
            .openedBy(1L)
            .openingNotes("Test")
            .totalCashIn(BigDecimal.ZERO)
            .totalCashOut(BigDecimal.ZERO)
            .netCashFlow(BigDecimal.ZERO)
            .build();
        
        System.out.println("✅ Money Box entity created successfully: " + moneyBox.getPharmacyId());
        System.out.println("✅ Status: " + moneyBox.getStatus());
        System.out.println("✅ Opening Balance: " + moneyBox.getOpeningBalance());
    }

    @Test
    void testTransactionTypeEnum() {
        // Test that transaction types are accessible
        System.out.println("✅ Transaction Types:");
        System.out.println("   - SALE: " + TransactionType.SALE);
        System.out.println("   - PURCHASE: " + TransactionType.PURCHASE);
        System.out.println("   - REFUND: " + TransactionType.REFUND);
        System.out.println("   - WITHDRAWAL: " + TransactionType.WITHDRAWAL);
        System.out.println("   - DEPOSIT: " + TransactionType.DEPOSIT);
    }

    @Test
    void testBigDecimalOperations() {
        // Test BigDecimal operations used in calculations
        BigDecimal amount = new BigDecimal("100.50");
        BigDecimal rate = new BigDecimal("2500.00");
        BigDecimal converted = amount.multiply(rate);
        
        System.out.println("✅ Currency conversion test:");
        System.out.println("   - Amount: " + amount + " USD");
        System.out.println("   - Rate: " + rate + " SYP/USD");
        System.out.println("   - Converted: " + converted + " SYP");
    }
}
