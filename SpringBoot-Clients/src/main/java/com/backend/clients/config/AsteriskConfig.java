package com.backend.clients.config;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsteriskConfig {
    @Value("${asterisk.host}")
    private String host;
    @Value("${asterisk.port}")
    private int port;
    @Value("${asterisk.username}")
    private String user;
    @Value("${asterisk.password}")
    private String pass;


    @Bean
    public ManagerConnection managerConnection() {
        ManagerConnectionFactory factory =
                new ManagerConnectionFactory(host, port, user, pass);
        return factory.createManagerConnection();
    }

}
