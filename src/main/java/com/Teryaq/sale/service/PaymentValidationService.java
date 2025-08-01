package com.Teryaq.sale.service;

import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.Enum.PaymentMethod;
import org.springframework.stereotype.Service;

@Service
public class PaymentValidationService {
    
    /**
     * التحقق من صحة نوع الدفع ووسيلة الدفع
     * @param paymentType نوع الدفع (كاش/دين)
     * @param paymentMethod وسيلة الدفع
     * @return true إذا كان صحيحاً
     */
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
    
    /**
     * التحقق من المبلغ المدفوع
     * @param totalAmount إجمالي الفاتورة
     * @param paidAmount المبلغ المدفوع
     * @param paymentType نوع الدفع
     * @return true إذا كان صحيحاً
     */
    public boolean validatePaidAmount(float totalAmount, float paidAmount, PaymentType paymentType) {
        switch (paymentType) {
            case CASH:
                // الدفع النقدي يجب أن يكون كاملاً أو أقل
                return paidAmount >= 0 && paidAmount <= totalAmount;
                
            case CREDIT:
                // الدفع الآجل يمكن أن يكون جزئياً
                return paidAmount >= 0;
                
            default:
                return false;
        }
    }
    
    /**
     * حساب المبلغ المتبقي
     * @param totalAmount إجمالي الفاتورة
     * @param paidAmount المبلغ المدفوع
     * @return المبلغ المتبقي
     */
    public float calculateRemainingAmount(float totalAmount, float paidAmount) {
        return Math.max(0, totalAmount - paidAmount);
    }
    
    /**
     * التحقق من اكتمال الدفع
     * @param totalAmount إجمالي الفاتورة
     * @param paidAmount المبلغ المدفوع
     * @return true إذا كان الدفع مكتملاً
     */
    public boolean isPaymentComplete(float totalAmount, float paidAmount) {
        return paidAmount >= totalAmount;
    }
} 