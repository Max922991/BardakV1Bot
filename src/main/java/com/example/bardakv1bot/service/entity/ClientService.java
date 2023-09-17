package com.example.bardakv1bot.service.entity;

import com.example.bardakv1bot.repository.ClientRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientService {

    final ClientRepo clientRepo;


}
