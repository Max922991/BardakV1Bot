package com.example.bardakv1bot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Table(name = "orders")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "time_of_record", nullable = false)
    LocalDateTime timeOfRecord;

    @Column(name = "complex_washing")
    String complexWashing;

    @Column(name = "body_cleaning")
    String bodyСleaning;

    @Column(name = "quartz_application")
    String quartzApplication;

    @Column(name = "disk_cleaning")
    String diskCleaning;

    @Column(name = "skin_lotion")
    String skinLotion;

    @Column(name = "plastic_lotion")
    String plasticLotion;

    @Column(name = "anti_rain")
    String antiRain;

    boolean record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

}
