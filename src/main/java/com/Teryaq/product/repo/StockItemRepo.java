package com.Teryaq.product.repo;

import com.Teryaq.product.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepo extends JpaRepository<StockItem, Long> {
} 