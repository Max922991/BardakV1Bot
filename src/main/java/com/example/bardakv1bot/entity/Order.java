package com.example.bardakv1bot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    @Column(name = "time_of_record")
    String timeOfRecord;

    String weekDay;

    @Column(name = "is_verified")
    Boolean record;

    @Column(name = "is_completed")
    Boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    Client client;

    @ManyToMany
    @JoinTable(
            name = "order_services",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    List<Service> services;

    public void addService(Service service) {
        if (services == null) {
            services = new ArrayList<>();
        }
        services.add(service);
    }

    public void deleteService(Service service) {
        if (!services.contains(service)) {
            throw new NoSuchElementException();
        }
        services.remove(service);
    }
}
