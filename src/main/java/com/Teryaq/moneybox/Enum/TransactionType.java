package com.Teryaq.moneybox.Enum;

public enum TransactionType {
    SALE,           // Cash from customer sale
    PURCHASE,       // Cash to supplier
    REFUND,         // Cash back to customer
    WITHDRAWAL,     // Cash taken from box (no entity)
    DEPOSIT,        // Cash added to box (no entity)
    TRANSFER,       // Between money boxes
    ADJUSTMENT      // Manual correction (no entity)
}
