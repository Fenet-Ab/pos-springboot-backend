package com.pos.app.repository;

import com.pos.app.model.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository
        extends JpaRepository<SaleItem, Long> {
}