package com.example.bardakv1bot.repository;

import com.example.bardakv1bot.entity.Client;
import com.example.bardakv1bot.entity.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author nerzon
 */
@Repository
public interface PhoneRepo extends JpaRepository<PhoneNumber, UUID> {
    PhoneNumber findByClient(Client client);
}
