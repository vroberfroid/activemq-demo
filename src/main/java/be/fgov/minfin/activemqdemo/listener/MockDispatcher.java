package be.fgov.minfin.activemqdemo.listener;

import be.fgov.minfin.activemqdemo.controller.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mock that simulate Taxud: it reads the toEurope queue in a local ActiveMQ
 * and send the response to the fromEurope queue.
 */
@Profile("mock")
@Component
public class MockDispatcher {
    @Value("${ccngateway.generator.queue.fromeurope}")
    private String queue;

    @Autowired
    JmsMessagingTemplate jmsTemplate;
    @JmsListener(destination = "${ccngateway.generator.queue.toeurope}")
    public void receiveMessage(String payload) throws URISyntaxException, IOException {
        System.out.println("** MOCK ** Message received in ActiveMQ <" + payload + ">");
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("CD002A.edifact").toURI());
        Stream<String> lines = Files.lines(path);
        String response = lines.collect(Collectors.joining("\n"));
        lines.close();
        System.out.println("*** MOCK ** Message sent from Mock to ActiveMQ:\n" + response);

        Message message = MessageBuilder.withPayload(response)
                .copyHeaders(Sender.headers("CD002A", "1234", "NCTS", "IE", "IECA", "xml")).build();
        jmsTemplate.send(queue, message);
    }
}
