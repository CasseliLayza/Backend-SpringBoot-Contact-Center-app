package com.backend.clients.service.imp;

import com.backend.clients.model.Client;
import com.backend.clients.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        return clientRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClientByDni(String dni) {
        return clientRepository.findByDni(dni).orElse(null);
    }

    @Override
    @Transactional
    public Client registerClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public Client updateClient(Long id, Client client) {
        return clientRepository.findById(id)
                .map(existingClient -> {
                    existingClient.setName(client.getName());
                    existingClient.setDni(client.getDni());
                    existingClient.setPhone(client.getPhone());
                    existingClient.setEmail(client.getEmail());
                    existingClient.setAddress(client.getAddress());
                    existingClient.setCity(client.getCity());
                    existingClient.setBalance(client.getBalance());
                    existingClient.setIsActive(client.getIsActive());
                    return clientRepository.save(existingClient);
                })
                .orElse(null);
    }

    @Override
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);

    }
}
