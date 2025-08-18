package com.Teryaq.product.mapper;

import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.dto.StockReportDTOResponse;
import com.Teryaq.product.dto.StockItemWithProductInfoDTOResponse;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.entity.Category;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.purchase.repository.PurchaseOrderItemRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StockItemMapper {
    
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;
    private final PurchaseOrderItemRepo purchaseOrderItemRepo;
    
    public StockItemDTOResponse toResponse(StockItem stockItem) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = stockItem.getExpiryDate();
        
        Boolean isExpired = expiryDate != null && expiryDate.isBefore(today);
        Boolean isExpiringSoon = expiryDate != null && 
            expiryDate.isAfter(today) && 
            expiryDate.isBefore(today.plusDays(30));
        
        Integer daysUntilExpiry = expiryDate != null ? 
            (int) ChronoUnit.DAYS.between(today, expiryDate) : null;
        
        String productName = getProductName(
            stockItem.getProductId(), stockItem.getProductType());
        
        Integer total = getTotalFromPurchaseOrder(stockItem);
        
        String supplier = getSupplierName(stockItem);
        List<String> categories = getCategories(stockItem.getProductId(), stockItem.getProductType());
        Integer minQuantity = getMinQuantity(stockItem.getProductId(), stockItem.getProductType());
        
        Float sellingPrice = getProductSellingPrice(stockItem.getProductId(), stockItem.getProductType());
        
        return StockItemDTOResponse.builder()
                .id(stockItem.getId())
                .productId(stockItem.getProductId())
                .productName(productName)
                .productType(stockItem.getProductType())
                .quantity(stockItem.getQuantity())
                .bonusQty(stockItem.getBonusQty())
                .total(total)
                .supplier(supplier)
                .categories(categories)
                .minQuantity(minQuantity)
                .expiryDate(stockItem.getExpiryDate())
                .batchNo(stockItem.getBatchNo())
                .actualPurchasePrice(stockItem.getActualPurchasePrice())
                .sellingPrice(sellingPrice)
                .dateAdded(stockItem.getDateAdded())
                .addedBy(stockItem.getCreatedBy() != null ? stockItem.getCreatedBy() : stockItem.getAddedBy())
                .purchaseInvoiceId(stockItem.getPurchaseInvoice() != null ? 
                    stockItem.getPurchaseInvoice().getId() : null)
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .daysUntilExpiry(daysUntilExpiry)
                .pharmacyId(stockItem.getPharmacy().getId())
                .purchaseInvoiceNumber(stockItem.getPurchaseInvoice() != null ? 
                    stockItem.getPurchaseInvoice().getInvoiceNumber() : null)
                .build();
    }
    
    public List<StockItemDTOResponse> toResponseList(List<StockItem> stockItems) {
        return stockItems.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public StockReportDTOResponse toReportResponse(List<StockItem> stockItems, 
                                                   ProductType productType) {
        List<StockItemDTOResponse> stockItemResponses = toResponseList(stockItems);
        
        Integer totalQuantity = stockItems.stream()
                .mapToInt(StockItem::getQuantity)
                .sum();
        
        Double totalValue = stockItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice())
                .sum();
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        Long expiredItems = stockItems.stream()
                .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(today))
                .count();
        
        Long expiringSoonItems = stockItems.stream()
                .filter(item -> item.getExpiryDate() != null && 
                    item.getExpiryDate().isAfter(today) && 
                    item.getExpiryDate().isBefore(thirtyDaysFromNow))
                .count();
        
        return StockReportDTOResponse.builder()
                .productType(productType)
                .totalItems(stockItems.size())
                .totalQuantity(totalQuantity)
                .totalValue(totalValue)
                .expiredItems(expiredItems)
                .expiringSoonItems(expiringSoonItems)
                .stockItems(stockItemResponses)
                .build();
    }
    
    public StockItemWithProductInfoDTOResponse toStockItemWithProductInfoDTO(StockItem stockItem) {
        return StockItemWithProductInfoDTOResponse.builder()
                .id(stockItem.getId())
                .productId(stockItem.getProductId())
                .productType(stockItem.getProductType())
                .quantity(stockItem.getQuantity())
                .actualPurchasePrice(stockItem.getActualPurchasePrice())
                .expiryDate(stockItem.getExpiryDate())
                .dateAdded(stockItem.getDateAdded())
                .productName(getProductName(stockItem.getProductId(), stockItem.getProductType()))
                .batchNo(stockItem.getBatchNo())
                .bonusQty(stockItem.getBonusQty())
                .addedBy(stockItem.getAddedBy())
                .purchaseInvoiceId(stockItem.getPurchaseInvoice() != null ? stockItem.getPurchaseInvoice().getId() : null)
                .requiresPrescription(isProductRequiresPrescription(stockItem.getProductId(), stockItem.getProductType()))
                .build();
    }
    
    public List<StockItemWithProductInfoDTOResponse> toStockItemWithProductInfoDTOList(List<StockItem> stockItems) {
        return stockItems.stream()
                .map(this::toStockItemWithProductInfoDTO)
                .collect(Collectors.toList());
    }

    private String getSupplierName(StockItem stockItem) {
        try {
            if (stockItem.getPurchaseInvoice() != null && 
                stockItem.getPurchaseInvoice().getSupplier() != null) {
                return stockItem.getPurchaseInvoice().getSupplier().getName();
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private List<String> getCategories(Long productId, ProductType productType) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent() && pharmacyProduct.get().getCategories() != null) {
                    return pharmacyProduct.get().getCategories().stream()
                            .map(Category::getName)
                            .collect(Collectors.toList());
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent() && masterProduct.get().getCategories() != null) {
                    return masterProduct.get().getCategories().stream()
                            .map(Category::getName)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
        }
        return List.of();
    }
    
    private Integer getMinQuantity(Long productId, ProductType productType) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    return pharmacyProduct.get().getMinStockLevel();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String getProductName(Long productId, ProductType productType) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    return pharmacyProduct.get().getTradeName();
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    return masterProduct.get().getTradeName();
                }
            }
        } catch (Exception e) {
        }
        return "Unknown Product";
    }
    
    public boolean isProductRequiresPrescription(Long productId, ProductType productType) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    return pharmacyProduct.get().getRequiresPrescription();
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    return masterProduct.get().getRequiresPrescription();
                }
            }
        } catch (Exception e) {
        }
        return false;   
    }
    
    public Float getProductSellingPrice(Long productId, ProductType productType) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    return pharmacyProduct.get().getRefSellingPrice();
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    return masterProduct.get().getRefSellingPrice();
                }
            }
        } catch (Exception e) {
        }
        return 0f;
    }

    // private Integer getTotalFromPurchaseInvoice(StockItem stockItem) {
    //     try {
    //         if (stockItem.getPurchaseInvoice() != null) {
    //             Optional<com.Teryaq.purchase.entity.PurchaseInvoiceItem> invoiceItem = 
    //                 stockItem.getPurchaseInvoice().getItems().stream()
    //                     .filter(item -> item.getProductId().equals(stockItem.getProductId()) &&
    //                                   item.getProductType() == stockItem.getProductType() &&
    //                                   (item.getBatchNo() != null && item.getBatchNo().equals(stockItem.getBatchNo())))
    //                     .findFirst();
                
    //             if (invoiceItem.isPresent()) {
    //                 double price = invoiceItem.get().getInvoicePrice() != null ? invoiceItem.get().getInvoicePrice() : 0.0;
    //                 int quantity = invoiceItem.get().getReceivedQty() != null ? invoiceItem.get().getReceivedQty() : 0;
        //             return (int) (price * quantity);
        //         }
        //     }
        // } catch (Exception e) {
        // }
    //    return 0;
    //}

    private Integer getTotalFromPurchaseOrder(StockItem stockItem) {
        try {
            List<com.Teryaq.purchase.entity.PurchaseOrderItem> orderItems = 
                purchaseOrderItemRepo.findByProductIdAndProductType(stockItem.getProductId(), stockItem.getProductType());
            
            if (!orderItems.isEmpty()) {
                com.Teryaq.purchase.entity.PurchaseOrderItem orderItem = orderItems.get(0);
                
                // استخدام total من PurchaseOrder
                if (orderItem.getPurchaseOrder() != null) {
                    return orderItem.getPurchaseOrder().getTotal() != null ? 
                           orderItem.getPurchaseOrder().getTotal().intValue() : 0;
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }
} 