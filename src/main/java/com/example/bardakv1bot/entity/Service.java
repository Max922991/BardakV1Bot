package com.example.bardakv1bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * @author nerzon
 */
@Table(name = "services")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Service {
    @Id
    UUID id;

    @Column(name = "tittle", unique = true)
    String tittle;

    String description;

    Integer price;
}
