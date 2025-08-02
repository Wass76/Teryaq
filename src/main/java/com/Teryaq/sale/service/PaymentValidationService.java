package com.Teryaq.sale.service;

import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.Enum.PaymentMethod;
import org.springframework.stereotype.Service;

@Service
public class PaymentValidationService {
 
    public boolean validatePayment(PaymentType paymentType, PaymentMethod paymentMethod) {
        if (paymentType == null || paymentMethod == null) {
            return false;
        }
        
        // التحقق من التوافق بين نوع الدفع ووسيلة الدفع
        switch (paymentType) {
            case CASH:
                // الدفع النقدي يمكن أن يكون كاش أو حساب بنك
                return paymentMethod == PaymentMethod.CASH || 
                       paymentMethod == PaymentMethod.BANK_ACCOUNT;
                       
            case CREDIT:
                // الدفع الآجل يمكن أن يكون حساب بنك
                return paymentMethod == PaymentMethod.BANK_ACCOUNT;
                       
            default:
                return false;
        }
    }
    

    public boolean validatePaidAmount(float totalAmount, float paidAmount, PaymentType paymentType) {
        switch (paymentType) {
            case CASH:
                // الدفع النقدي يجب أن يكون كاملاً (لا يمكن أن يكون هناك مبلغ متبقي)
                return paidAmount >= 0 && paidAmount >= totalAmount;
                
            case CREDIT:
                // الدفع الآجل يمكن أن يكون جزئياً
                return paidAmount >= 0;
                
            default:
                return false;
        }
    }
    
 
    public float calculateRemainingAmount(float totalAmount, float paidAmount) {
        return Math.max(0, totalAmount - paidAmount);
    }
    
    
    public boolean isPaymentComplete(float totalAmount, float paidAmount) {
        return paidAmount >= totalAmount;
    }
} 