package com.lynx.orderservice.repository;

import com.lynx.orderservice.domain.InstrumentType;
import com.lynx.orderservice.domain.Order;
import com.lynx.orderservice.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find all orders for a specific platform.
     */
    @Query("SELECT o FROM Order o WHERE o.platformId = :platformId")
    List<Order> findByPlatformId(@Param("platformId") UUID platformId);

    /**
     * Find all orders placed by a specific user on a specific platform.
     */
    @Query("SELECT o FROM Order o WHERE o.platformId = :platformId AND o.platformUserId = :platformUserId")
    List<Order> findByPlatformIdAndPlatformUserId(@Param("platformId") UUID platformId, @Param("platformUserId") UUID platformUserId);

    /**
     * Find all orders for a specific platform user with a given status.
     */
    @Query("SELECT o FROM Order o WHERE o.platformUserId = :platformUserId AND o.status = :status")
    List<Order> findByPlatformUserIdAndStatus(@Param("platformUserId") UUID platformUserId, @Param("status") Status status);

    /**
     * Find all orders of a specific instrument type.
     */
    @Query("SELECT o FROM Order o WHERE o.instrumentType = :instrumentType")
    List<Order> findByInstrumentType(@Param("instrumentType") InstrumentType instrumentType);

    /**
     * Find an order by instrument ID and instrument type.
     */
    @Query("SELECT o FROM Order o WHERE o.instrumentId = :instrumentId AND o.instrumentType = :instrumentType")
    List<Order> findByInstrumentIdAndInstrumentType(@Param("instrumentId") String instrumentId, @Param("instrumentType") InstrumentType instrumentType);

    /**
     * Find all orders for a specific platform with a given status.
     */
    @Query("SELECT o FROM Order o WHERE o.platformId = :platformId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByPlatformIdAndStatus(@Param("platformId") UUID platformId, @Param("status") Status status);

    /**
     * Find an order by its ID and platform ID (for validation).
     */
    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId AND o.platformId = :platformId")
    Optional<Order> findByOrderIdAndPlatformId(@Param("orderId") UUID orderId, @Param("platformId") UUID platformId);
}