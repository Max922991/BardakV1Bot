package com.example.bardakv1bot.repository;

import com.example.bardakv1bot.entity.Client;
import com.example.bardakv1bot.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

//    Order findOrderByTimeOfRecord(LocalTime time);
    Order findByClientAndRecord(Client client, Boolean record);
    void deleteByClientAndRecord(Client client, Boolean record);
}
