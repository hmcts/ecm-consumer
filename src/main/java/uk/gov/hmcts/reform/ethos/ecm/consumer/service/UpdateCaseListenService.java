package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import com.google.gson.Gson;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
public class UpdateCaseListenService {

    @Value("${queue.create-updates.listen.connection-string}")
    private String queueCreateUpdatesListenString;
    @Value("${queue.create-updates.queue-name}")
    private String queueCreateUpdatesName;

    @Value("${queue.update-case.listen.connection-string}")
    private String queueUpdateCaseSendString;
    @Value("${queue.update-case.send.connection-string}")
    private String queueUpdateCaseListenString;
    @Value("${queue.update-case.queue-name}")
    private String queueUpdateCaseName;

    private Gson GSON = new Gson();

    @Autowired
    public UpdateCaseListenService() {

    }

    //@Scheduled(fixedRate = 100000)
    public void updateCaseListen() {
        log.info("Running updateCaseSend job....");
        log.info("queueCreateUpdatesListenString" + queueCreateUpdatesListenString);
        log.info("queueCreateUpdatesName" + queueCreateUpdatesName);
        log.info("queueUpdateCaseListenString" + queueUpdateCaseListenString);
        log.info("queueUpdateCaseSendString" + queueUpdateCaseSendString);
        log.info("queueUpdateCaseName" + queueUpdateCaseName);
    }

    //@Scheduled(fixedRate = 100000)
//    public void sendMessages() throws Exception {
//        log.info("Waiting on messages");
//        QueueClient receiveClient = new QueueClient(new ConnectionStringBuilder(queueUpdateCaseListenString, queueUpdateCaseName), ReceiveMode.PEEKLOCK);
//        registerReceiver(receiveClient);
//        receiveClient.close();
//    }
//
//    void registerReceiver(QueueClient queueClient) throws Exception {
//        // register the RegisterMessageHandler callback
//        queueClient.registerMessageHandler
//            (new IMessageHandler() {
//
//                // callback invoked when the message handler loop has obtained a message
//                 public CompletableFuture<Void> onMessageAsync(IMessage message) {
//                     // receives message is passed to callback
//                     if (message.getLabel() != null &&
//                         message.getContentType() != null &&
//                         message.getLabel().contentEquals("Scientist") &&
//                         message.getContentType().contentEquals("application/json")) {
//
//                         byte[] body = message.getBody();
//                         printBody(body, message);
//                     }
//                     return CompletableFuture.completedFuture(null);
//                 }
//
//                 // callback invoked when the message handler has an exception to report
//                 public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
//                     System.out.printf(exceptionPhase + "-" + throwable.getMessage());
//                 }
//             },
//             // 1 concurrent call, messages are auto-completed, auto-renew duration
//             new MessageHandlerOptions(1, true, Duration.ofMinutes(1)));
//    }
//
//
//
//    private void printBody(byte[] body, IMessage message) {
//        Map scientist = GSON.fromJson(new String(body, UTF_8), Map.class);
//
//        System.out.printf(
//            "\n\t\t\t\tMessage received: \n\t\t\t\t\t\tMessageId = %s, \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
//                "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\",  \n\t\t\t\t\t\tContent: [ firstName = %s, name = %s ]\n",
//            message.getMessageId(),
//            message.getSequenceNumber(),
//            message.getEnqueuedTimeUtc(),
//            message.getExpiresAtUtc(),
//            message.getContentType(),
//            scientist != null ? scientist.get("firstName") : "",
//            scientist != null ? scientist.get("name") : "");
//    }

}
