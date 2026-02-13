package com.backend.clients.service.imp;

import com.backend.clients.entity.Call;
import com.backend.clients.entity.Client;
import com.backend.clients.repository.CallRepository;
import com.backend.clients.repository.ClientRepository;
import jakarta.annotation.PostConstruct;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.RedirectAction;
import org.asteriskjava.manager.action.SetVarAction;
import org.asteriskjava.manager.event.DtmfEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AmiListener {

    private final ManagerConnection managerConnection;
    private final ClientRepository clientRepository;
    private final CallRepository callRepository;
    private final ExecutorService executor =
            Executors.newFixedThreadPool(10);
    private static final Logger logger = LoggerFactory.getLogger(AmiListener.class);


    public AmiListener(ManagerConnection managerConnection, ClientRepository clientRepository, CallRepository callRepository) {
        this.managerConnection = managerConnection;
        this.clientRepository = clientRepository;
        this.callRepository = callRepository;
    }

    @PostConstruct
    public void start() throws AuthenticationFailedException, IOException, TimeoutException {
        managerConnection.login();
        /*managerConnection.addEventListener(event -> {
            System.out.println("Received event: " + event.getClass().getSimpleName());
            System.out.println(event);
        });

        System.out.println("AMI Listener started and connected to Asterisk server.");
*/
        /*managerConnection.addEventListener(event -> {
            if (event instanceof NewStateEvent e) {
                if ("Ringing".equals((e.getState()))) {
                    System.out.println("Incoming call detected: "
                            + e.getCallerIdNum());
                }
            }
        });*/

        /*OriginateAction originate = new OriginateAction();
        originate.setChannel("SIP/1000");
        originate.setContext("from-internal");
        originate.setExten("100");
        originate.setPriority(1);
        originate.setCallerId("javacall");

        managerConnection.sendAction(originate);*/

        managerConnection.addEventListener(event -> {
            //System.out.println(" event: " + event.getClass().getSimpleName());

            logger.info("Received event: " + event);


//            if (event instanceof NewChannelEvent e) {
//                System.out.println("Incoming call detected: " + phone);
//            }

            if (event instanceof DtmfEvent e) {

                if (!e.isEnd()) {
                    return;
                }

                String phone = e.getCallerIdNum();
                String digit = e.getDigit();
                String channel = e.getChannel();

                logger.info("Received DTMF digit: " + digit + " on channel: " + channel + " from phone: " + phone);

                executor.submit(() -> {

                    try {
                        procesarDTMF(digit, channel, phone);
                    } catch (IOException | InterruptedException | TimeoutException ex) {
                        throw new RuntimeException(ex);
                    }
                });

            }

        });

        logger.info("AMI Listener started and connected to Asterisk server.");


    }


    private void flujoLLamada(String phone, String channel) throws IOException, TimeoutException {
        Optional<Client> client = clientRepository.findByPhone(phone);

        logger.info("Client >>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + client);

        if (client.isEmpty()) {
            registerCall(phone, "No client");
            sendIVR(channel, "ivr-no-client");
            return;

        }

        if (!client.get().getIsActive()) {
            registerCall(phone, "Inactive client");
            sendIVR(channel, "ivr-client-inactive");
            return;
        }

        registerCall(phone, "Active client");
        sendIVR(channel, "ivr-client");


    }


    private void procesarDTMF(String opcion, String channel, String phone) throws IOException, TimeoutException, InterruptedException {

        switch (opcion) {
            case "1" -> consultaSaldo(channel, phone);
            case "2" -> consultaCitas(channel);
            default -> sendIVR(channel, "ivr-opcion-invalida");

        }
    }


    private void consultaSaldo(String channel, String phone) throws IOException, InterruptedException {

        Client client = clientRepository.findByPhone(phone)
                .map(c -> {
                    logger.info("Client found: " + c);
                    return c;
                }).orElseThrow(() -> {
                    logger.warn("No client found with phone: " + phone);
                    throw new RuntimeException("No client found with phone: " + phone);
                });

        String saldo = client.getBalance();
        registerCall(client.getPhone(), "Active client");
        sendIVR(channel, "ivr-tiene-saldo", saldo);

    }

    private void consultaCitas(String channel) {
    }


    private void sendIVR(String channel, String ivrContext) throws IOException, TimeoutException {

        RedirectAction action = new RedirectAction();
        action.setChannel(channel);
        action.setContext(ivrContext);
        action.setExten("s");
        action.setPriority(1);

        managerConnection.sendAction(action, null);

    }

    private void sendIVR(String channel, String ivrContext, String balance)
            throws IOException, InterruptedException {

        SetVarAction setVarAction = new SetVarAction();
        setVarAction.setChannel(channel);
        setVarAction.setVariable("BALANCE");
        setVarAction.setValue(balance);

        managerConnection.sendAction(setVarAction, null);

        RedirectAction action = new RedirectAction();
        action.setChannel(channel);
        action.setContext(ivrContext);
        action.setExten("s");
        action.setPriority(1);

        Thread.sleep(100);
        managerConnection.sendAction(action, null);

    }

    private void registerCall(String phone, String status) {
        Call call = new Call();
        call.setTelephone(phone);
        call.setStatus(status);
        call.setFecha(LocalDateTime.now());

        System.out.println("Registered call: " + call);
        callRepository.save(call);


    }

}
