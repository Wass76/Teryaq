package com.Teryaq.sale.controller;

import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.Enum.PaymentMethod;
import com.Teryaq.sale.service.PaymentValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment Management", description = "APIs for payment validation and methods")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentValidationService paymentValidationService;

    /**
     * الحصول على أنواع الدفع المتاحة
     */
    @GetMapping("/types")
    @Operation(summary = "Get available payment types", description = "Returns all available payment types (CASH/CREDIT)")
    public ResponseEntity<List<PaymentTypeResponse>> getPaymentTypes() {
        List<PaymentTypeResponse> types = Arrays.stream(PaymentType.values())
            .map(type -> new PaymentTypeResponse(
                type.name(),
                type.getTranslatedName("ar"),
                type.getTranslatedName("en")
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(types);
    }

    /**
     * الحصول على وسائل الدفع المتاحة
     */
    @GetMapping("/methods")
    @Operation(summary = "Get available payment methods", description = "Returns all available payment methods")
    public ResponseEntity<List<PaymentMethodResponse>> getPaymentMethods() {
        List<PaymentMethodResponse> methods = Arrays.stream(PaymentMethod.values())
            .map(method -> new PaymentMethodResponse(
                method.name(),
                method.getTranslatedName("ar"),
                method.getTranslatedName("en")
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(methods);
    }

    /**
     * الحصول على وسائل الدفع المتوافقة مع نوع الدفع
     */
    @GetMapping("/methods/{paymentType}")
    @Operation(summary = "Get compatible payment methods", description = "Returns payment methods compatible with the given payment type")
    public ResponseEntity<List<PaymentMethodResponse>> getCompatiblePaymentMethods(@PathVariable String paymentType) {
        try {
            PaymentType type = PaymentType.valueOf(paymentType.toUpperCase());
            List<PaymentMethod> compatibleMethods = Arrays.stream(PaymentMethod.values())
                .filter(method -> paymentValidationService.validatePayment(type, method))
                .collect(Collectors.toList());
            
            List<PaymentMethodResponse> methods = compatibleMethods.stream()
                .map(method -> new PaymentMethodResponse(
                    method.name(),
                    method.getTranslatedName("ar"),
                    method.getTranslatedName("en")
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(methods);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * التحقق من صحة الدفع
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate payment", description = "Validates payment type and method compatibility")
    public ResponseEntity<PaymentValidationResponse> validatePayment(@RequestBody PaymentValidationRequest request) {
        boolean isValid = paymentValidationService.validatePayment(request.getPaymentType(), request.getPaymentMethod());
        
        PaymentValidationResponse response = new PaymentValidationResponse();
        response.setValid(isValid);
        response.setMessage(isValid ? "the payment is valid" : "the payment is not valid");
        
        return ResponseEntity.ok(response);
    }

    // DTOs for responses
    public static class PaymentTypeResponse {
        private String code;
        private String arabicName;
        private String englishName;

        public PaymentTypeResponse(String code, String arabicName, String englishName) {
            this.code = code;
            this.arabicName = arabicName;
            this.englishName = englishName;
        }

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getArabicName() { return arabicName; }
        public void setArabicName(String arabicName) { this.arabicName = arabicName; }
        public String getEnglishName() { return englishName; }
        public void setEnglishName(String englishName) { this.englishName = englishName; }
    }

    public static class PaymentMethodResponse {
        private String code;
        private String arabicName;
        private String englishName;

        public PaymentMethodResponse(String code, String arabicName, String englishName) {
            this.code = code;
            this.arabicName = arabicName;
            this.englishName = englishName;
        }

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getArabicName() { return arabicName; }
        public void setArabicName(String arabicName) { this.arabicName = arabicName; }
        public String getEnglishName() { return englishName; }
        public void setEnglishName(String englishName) { this.englishName = englishName; }
    }

    public static class PaymentValidationRequest {
        private PaymentType paymentType;
        private PaymentMethod paymentMethod;

        // Getters and setters
        public PaymentType getPaymentType() { return paymentType; }
        public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class PaymentValidationResponse {
        private boolean valid;
        private String message;

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
} 