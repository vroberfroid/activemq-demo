package be.fgov.minfin.activemqdemo.controller;

import be.fgov.minfin.esbsoa.ems.ccncsi.common.domain.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "api")
@EnableJms
public class Sender {

    @Value("${ccngateway.generator.queue.toeurope}")
    private String queue;

    @Autowired
    JmsMessagingTemplate jmsTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMddHHmmss");

    @PostMapping(path = "send")
    public void sendMessage() throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("CD002C.xml").toURI());
        Stream<String> lines = Files.lines(path);
        String payload = lines.collect(Collectors.joining("\n"));
        lines.close();
        System.out.println("Message sent:\n" + payload);

        Map<String,Object> headers = headers();
        Message message = MessageBuilder.withPayload(payload).copyHeaders(headers).build();
        jmsTemplate.send(queue, message);
    }

    public static Map<String,Object> headers() {
        Map<String, Object> map = new HashMap<>();
        String messageId = String.format("%s%06d%02d", LocalDateTime.now().format(DATE_FORMATTER), (new Random()).nextInt(999999), 1);
        String correlationId = String.format("%s%06d%02d", LocalDateTime.now().format(DATE_FORMATTER), (new Random()).nextInt(999999), 2);
        map.put(Headers.APPLICATION, "NCTS");
        map.put(Headers.APPLICATION_SENDER, "NCTS");
        map.put(Headers.CORRELATION_ID, correlationId);
        map.put(Headers.MESSAGE_ID, messageId);
        map.put(Headers.CONTENT_TYPE, "application/xml");
        map.put(Headers.COUNTRY_CODE, "IV");
        map.put(Headers.MESSAGE_TYPE, "CD002C-MSG.NCTS");
        map.put(Headers.QUEUE_BASE_NAME, "CORE-IECA-05");
        map.put(Headers.QUEUE_MESSAGE_TYPE, "REQUEST");
        return map;
    }
}
