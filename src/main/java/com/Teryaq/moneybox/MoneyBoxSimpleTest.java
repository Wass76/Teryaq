package com.Teryaq.moneybox;

import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.Enum.MoneyBoxStatus;
import com.Teryaq.moneybox.Enum.PeriodType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Simple test class to verify the Money Box system compiles correctly
 * This will be removed in production
 */
@Component
public class MoneyBoxSimpleTest {
    
    public void testEntityCreation() {
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
    }
}
