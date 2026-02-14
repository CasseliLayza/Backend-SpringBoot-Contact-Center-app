package com.backend.clients.service.imp;

import com.backend.clients.model.Client;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ClientService {

    List<Client> getAllClients();

    Client getClientById(Long id);

    Client getClientByDni(String dni);

    Client registerClient(Client client);

    Client updateClient(Long id, Client client);

    void deleteClient(Long id);


}
