package com.Teryaq.sale.controller;

import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.Enum.PaymentMethod;
import com.Teryaq.sale.service.PaymentValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "BearerAuth")
public class PaymentController {

    @Autowired
    private PaymentValidationService paymentValidationService;

    /**
     * الحصول على أنواع الدفع المتاحة
     */
    @GetMapping("/types")
    @Operation(
        summary = "Get available payment types", 
        description = "Returns all available payment types (CASH/CREDIT)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved payment types",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaymentTypeResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(
        summary = "Get available payment methods", 
        description = "Returns all available payment methods"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved payment methods",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaymentMethodResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(
        summary = "Get compatible payment methods", 
        description = "Returns payment methods compatible with the given payment type"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved compatible payment methods",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaymentMethodResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payment type"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PaymentMethodResponse>> getCompatiblePaymentMethods(
            @Parameter(description = "Payment type", example = "CASH", 
                      schema = @Schema(allowableValues = {"CASH", "CREDIT"})) 
            @PathVariable String paymentType) {
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
    @Operation(
        summary = "Validate payment", 
        description = "Validates payment type and method compatibility"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully validated payment",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaymentValidationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentValidationResponse> validatePayment(
            @Parameter(description = "Payment validation data", required = true)
            @RequestBody PaymentValidationRequest request) {
        boolean isValid = paymentValidationService.validatePayment(request.getPaymentType(), request.getPaymentMethod());
        
        PaymentValidationResponse response = new PaymentValidationResponse();
        response.setValid(isValid);
        response.setMessage(isValid ? "the payment is valid" : "the payment is not valid");
        
        return ResponseEntity.ok(response);
    }

    // DTOs for responses
    @Schema(description = "Payment type response")
    public static class PaymentTypeResponse {
        @Schema(description = "Payment type code", example = "CASH")
        private String code;
        @Schema(description = "Arabic name", example = "نقداً")
        private String arabicName;
        @Schema(description = "English name", example = "Cash")
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

    @Schema(description = "Payment method response")
    public static class PaymentMethodResponse {
        @Schema(description = "Payment method code", example = "CASH")
        private String code;
        @Schema(description = "Arabic name", example = "نقداً")
        private String arabicName;
        @Schema(description = "English name", example = "Cash")
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

    @Schema(description = "Payment validation request")
    public static class PaymentValidationRequest {
        @Schema(description = "Payment type", example = "CASH")
        private PaymentType paymentType;
        @Schema(description = "Payment method", example = "CASH")
        private PaymentMethod paymentMethod;

        // Getters and setters
        public PaymentType getPaymentType() { return paymentType; }
        public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    @Schema(description = "Payment validation response")
    public static class PaymentValidationResponse {
        @Schema(description = "Validation result", example = "true")
        private boolean valid;
        @Schema(description = "Validation message", example = "the payment is valid")
        private String message;

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
} 