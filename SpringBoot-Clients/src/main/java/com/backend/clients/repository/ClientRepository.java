package com.backend.clients.repository;

import com.backend.clients.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByDni(String dni);
    Optional<Client> findByPhone(String phone);


}
