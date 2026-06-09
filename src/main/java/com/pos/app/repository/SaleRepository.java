package com.pos.app.repository;

import com.pos.app.model.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository
        extends JpaRepository<Sale, Long> {

    java.util.Optional<Sale> findByTxRef(String txRef);
}