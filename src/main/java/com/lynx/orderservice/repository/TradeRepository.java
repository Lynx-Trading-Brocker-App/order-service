package com.lynx.orderservice.repository;

import com.lynx.orderservice.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID>, JpaSpecificationExecutor<Trade> {

}