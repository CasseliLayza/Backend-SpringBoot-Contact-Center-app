package com.backend.clients.service.imp;

import com.backend.clients.model.Call;
import com.backend.clients.model.CallSession;
import com.backend.clients.model.Client;
import com.backend.clients.repository.CallRepository;
import com.backend.clients.repository.ClientRepository;
import jakarta.annotation.PostConstruct;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.RedirectAction;
import org.asteriskjava.manager.action.SetVarAction;
import org.asteriskjava.manager.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AmiListener {

    private static final Logger logger = LoggerFactory.getLogger(AmiListener.class);
    private final ManagerConnection managerConnection;
    private final ClientRepository clientRepository;
    private final CallRepository callRepository;
    private final ExecutorService executor =
            Executors.newFixedThreadPool(10);

    private final Map<String, CallSession> sessions = new ConcurrentHashMap<>();


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

        logger.info("AMI Listener started and connected to Asterisk server.");
*/
        /*managerConnection.addEventListener(event -> {
            if (event instanceof NewStateEvent e) {
                if ("Ringing".equals((e.getState()))) {
                    logger.info("Incoming call detected: "
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
            //logger.info(" event: " + event.getClass().getSimpleName());

            logger.info("Received event: " + event);


            if (event instanceof NewChannelEvent e) {
                logger.info("New channel event detected: " + e.getChannel() + " Caller ID: " + e.getCallerIdNum());
                String uniqueId = e.getUniqueId();
                String callerId = e.getCallerIdNum();

                CallSession session = new CallSession(uniqueId, LocalDateTime.now());
                session.setCallerId(callerId);
                sessions.put(uniqueId, session);

                logger.info("Session created for uniqueId " + uniqueId + ": " + session);

            }

            if (event instanceof DtmfEvent e) {

                if (!e.isEnd()) {
                    return;
                }

                String phone = e.getCallerIdNum();
                String digit = e.getDigit();
                String channel = e.getChannel();
                String uniqueId = e.getUniqueId();

                CallSession session = sessions.get(uniqueId);

                if (session != null) {
                    session.setSelectedOption(digit);
                    sessions.put(uniqueId, session);
                    logger.info("Session found for uniqueId " + uniqueId + ": " + session);
                }

                logger.info("Received DTMF digit: " + digit + " on channel: " + channel + " from phone: " + phone);

                executor.submit(() -> {

                    try {
                        procesarDTMF(digit, channel, phone, uniqueId);
                    } catch (IOException | InterruptedException | TimeoutException ex) {
                        throw new RuntimeException(ex);
                    }
                });

            }


            if (event instanceof SoftHangupRequestEvent e) {
                String uniqueId = e.getUniqueId();
                CallSession session = sessions.get(uniqueId);

                if (session != null) {
                    Duration duration = Duration.between(
                            session.getStartTime(), LocalDateTime.now());

                    long seconds = duration.getSeconds();
                    logger.info("Call ended for session: " + session);
                    logger.info("Call duration for session " + uniqueId + ": " + seconds + " seconds");

                    registerCall(session.getCallerId(),
                            "FINISHED",
                            e.getChannel(),
                            uniqueId,
                            session.getSelectedOption(),
                            "result",
                            String.valueOf(seconds)
                    );

                    sessions.remove(uniqueId);

                }
            }

        });

        logger.info("AMI Listener started and connected to Asterisk server.");


    }


    private void flujoLLamada(String phone, String channel) throws IOException, TimeoutException {
        Optional<Client> client = clientRepository.findByPhone(phone);

        logger.info("Client >>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + client);

        if (client.isEmpty()) {
            //registerCall(phone, "No client");
            sendIVR(channel, "ivr-no-client");
            return;

        }

        if (!client.get().getIsActive()) {
            //registerCall(phone, "Inactive client");
            sendIVR(channel, "ivr-client-inactive");
            return;
        }

        //registerCall(phone, "Active client", channel);
        sendIVR(channel, "ivr-client");


    }


    private void procesarDTMF(String opcion, String channel, String phone, String uniqueId) throws IOException, TimeoutException, InterruptedException {

        switch (opcion) {
            case "1" -> consultaSaldo(channel, phone, opcion, uniqueId);
            case "2" -> consultaCitas(channel);
            default -> sendIVR(channel, "ivr-opcion-invalida");

        }
    }


    private void consultaSaldo(String channel, String phone, String option, String uniqueId) throws IOException, InterruptedException {

        Client client = clientRepository.findByPhone(phone)
                .map(c -> {
                    logger.info("Client found: " + c);
                    return c;
                }).orElseThrow(() -> {
                    logger.warn("No client found with phone: " + phone);
                    throw new RuntimeException("No client found with phone: " + phone);
                });

        String saldo = client.getBalance();
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

    private void registerCall(String phone,
                              String status,
                              String channel,
                              String uniqueId,
                              String optionSelected,
                              String result,
                              String duration
    ) {
        Call call = new Call();
        call.setTelephone(phone);
        call.setStatus(status);
        call.setDateTime(LocalDateTime.now());
        call.setChannel(channel);
        call.setUniqueId(uniqueId);
        call.setOptionSelected(optionSelected);
        call.setResult(result);
        call.setDuration(duration);


        logger.info("Registered call: " + call);
        callRepository.save(call);


    }

}
