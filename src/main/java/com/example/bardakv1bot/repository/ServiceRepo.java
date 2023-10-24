package com.example.bardakv1bot.repository;

import com.example.bardakv1bot.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author nerzon
 */
@Repository
public interface ServiceRepo extends JpaRepository<Service, UUID> {
    Service findByTitle(String title);
}
