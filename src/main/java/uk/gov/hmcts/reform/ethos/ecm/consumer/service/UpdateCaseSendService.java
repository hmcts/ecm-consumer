package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import com.google.gson.reflect.TypeToken;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import uk.gov.hmcts.reform.ethos.ecm.consumer.config.QueueClientConfig;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UpdateCaseSendService {

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
    public UpdateCaseSendService() {

    }

    //@Scheduled(fixedRate = 100000)
    public void updateCaseSend() {
        log.info("Running updateCaseSend job....");
        log.info("queueCreateUpdatesListenString" + queueCreateUpdatesListenString);
        log.info("queueCreateUpdatesName" + queueCreateUpdatesName);
        log.info("queueUpdateCaseListenString" + queueUpdateCaseListenString);
        log.info("queueUpdateCaseSendString" + queueUpdateCaseSendString);
        log.info("queueUpdateCaseName" + queueUpdateCaseName);
    }

    //@Scheduled(fixedRate = 100000)
    @Scheduled(fixedDelay = 100000000, initialDelay = 200000)
    public void sendMessages() throws Exception {
        log.info("Connection done!!!");
        QueueClient sendClient = new QueueClient(new ConnectionStringBuilder(queueUpdateCaseSendString, queueUpdateCaseName), ReceiveMode.PEEKLOCK);
        sendMessagesAsync(sendClient).thenRunAsync(sendClient::closeAsync);
        sendClient.close();
    }

    CompletableFuture<Void> sendMessagesAsync(QueueClient sendClient) {
        List<HashMap<String, String>> data = generateMessageContent();

        List<CompletableFuture> tasks = new ArrayList<>();

        log.info("Start sending messages!!!");

        for (int i = 0; i < data.size(); i++) {

            Message message = generateMessage(Integer.toString(i), data, i);

            tasks.add(
                sendClient.sendAsync(message).thenRunAsync(() -> {
                    System.out.printf("\n\tMessage acknowledged: Id = %s", message.getMessageId());
                }));
        }

        return CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[tasks.size()]));
    }

    private List<HashMap<String, String>> generateMessageContent() {
        List<HashMap<String, String>> data =
            GSON.fromJson(
                "[" +
                    "{'name' = 'Einstein', 'firstName' = 'Albert'}," +
                    "{'name' = 'Heisenberg', 'firstName' = 'Werner'}," +
                    "{'name' = 'Curie', 'firstName' = 'Marie'}," +
                    "{'name' = 'Hawking', 'firstName' = 'Steven'}," +
                    "{'name' = 'Newton', 'firstName' = 'Isaac'}," +
                    "{'name' = 'Bohr', 'firstName' = 'Niels'}," +
                    "{'name' = 'Faraday', 'firstName' = 'Michael'}," +
                    "{'name' = 'Galilei', 'firstName' = 'Galileo'}," +
                    "{'name' = 'Kepler', 'firstName' = 'Johannes'}," +
                    "{'name' = 'Kopernikus', 'firstName' = 'Nikolaus'}" +
                    "]",
                new TypeToken<List<HashMap<String, String>>>() {}.getType());
        return data;
    }

    private Message generateMessage(String messageId, List<HashMap<String, String>> data, int i) {
        Message message = new Message(GSON.toJson(data.get(i), Map.class).getBytes(StandardCharsets.UTF_8));
        message.setContentType("application/json");
        message.setLabel("Scientist");
        message.setMessageId(messageId);
        message.setTimeToLive(Duration.ofMinutes(20));
        System.out.printf("\nMessage sending: Id = %s", message.getMessageId());
        return message;
    }
}
