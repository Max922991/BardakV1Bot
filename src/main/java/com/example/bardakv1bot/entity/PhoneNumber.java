package com.example.bardakv1bot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * @author nerzon
 */
@Getter
@Setter
@Entity
@Table(name = "phone_number")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhoneNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "number", length = 12)
    String phoneNumber = "+7";

    @OneToOne
    @JoinColumn(name = "client_id")
    Client client;
    public void minusDigit() {
        if (phoneNumber.length() > 2) {
            phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 1);
        }
    }

    public void plusDigit(String digit) {
        if (phoneNumber.length() < 12) {
            phoneNumber += digit;
        }
    }

    public void drop() {
        phoneNumber = "+7";
    }

}
