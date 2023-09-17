package com.example.bardakv1bot.repository;

import com.example.bardakv1bot.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

    LocalTime findOrderByTimeOfRecord(LocalTime time);
}
