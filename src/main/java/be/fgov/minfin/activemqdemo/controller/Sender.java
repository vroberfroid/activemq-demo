package be.fgov.minfin.activemqdemo.controller;

import be.fgov.minfin.esbsoa.ems.ccncsi.common.domain.Headers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final Map<String, String> queueMap = new HashMap<>();
    static {
        queueMap.put("ECS.CTA.CORE", "COR1-03");
        queueMap.put("ECS.CTA.ADMIN", "ADM1-03");
        queueMap.put("ECS.IECA.CORE", "CORE-IECA-04");
        queueMap.put("ECS.IECA.ADMIN", "ADMIN-IECA-04");
        queueMap.put("NCTS.CTA.CORE", "COR1-03");
        queueMap.put("NCTS.CTA.ADMIN", "ADM1-03");
        queueMap.put("NCTS.IECA.CORE", "CORE-IECA-05");
        queueMap.put("NCTS.IECA.ADMIN", "ADMIN-IECA-05");
    }

    @PostMapping(path = "send")
    public void send(@RequestParam String messageType, @RequestParam String mrn,
                                  @RequestParam String applicationName, @RequestParam String context, @RequestParam String country) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(messageType + ".xml").toURI());
        Stream<String> lines = Files.lines(path);
        String payload = lines.collect(Collectors.joining("\n"));
        lines.close();
        if (StringUtils.isNotEmpty(mrn)) {
            payload = payload.replace("$MRN", mrn);
        }

        Map<String, Object> headers = headers(messageType, mrn,applicationName,country, context);
        printMessage(headers, payload);
        Message message = MessageBuilder.withPayload(payload).copyHeaders(headers).build();
        jmsTemplate.send(queue, message);
    }


    public static Map<String, Object> headers(String messageType, String correlationId, String applicationName, String country, String context) {
        Map<String, Object> map = new HashMap<>();
        String messageId = String.format("%s%06d%02d", LocalDateTime.now().format(DATE_FORMATTER), (new Random()).nextInt(999999), 1);
        if(StringUtils.isEmpty(correlationId)) {
            correlationId = "23DE37D25B5E6FC0B9";
        }
        map.put(Headers.APPLICATION, applicationName);
        map.put(Headers.APPLICATION_SENDER, applicationName);
        map.put(Headers.CORRELATION_ID, correlationId);
        map.put(Headers.MESSAGE_ID, messageId);
        map.put(Headers.CONTENT_TYPE, "application/xml");
        map.put(Headers.COUNTRY_CODE, country);
        map.put(Headers.MESSAGE_TYPE, messageType + "-MSG." + applicationName);
        map.put(Headers.QUEUE_BASE_NAME, getQueueBaseName(messageType, applicationName, context));
        map.put(Headers.QUEUE_MESSAGE_TYPE, "REQUEST");
        return map;
    }

    public static Map<String, Object> headersAesCta906() {
        Map<String, Object> map = new HashMap<>();
        String messageId = String.format("%s%06d%02d", LocalDateTime.now().format(DATE_FORMATTER), (new Random()).nextInt(999999), 1);
        String correlationId = "23DE37D25B5E6FC0B9";
        map.put(Headers.APPLICATION, "ECS");
        map.put(Headers.APPLICATION_SENDER, "ECS");
        map.put(Headers.CORRELATION_ID, correlationId);
        map.put(Headers.MESSAGE_ID, messageId);
        map.put(Headers.CONTENT_TYPE, "application/xml");
        map.put(Headers.COUNTRY_CODE, "DE");
        map.put(Headers.MESSAGE_TYPE, "CD906C-MSG.ECS");
        map.put(Headers.QUEUE_BASE_NAME, "ADM1-03");
        map.put(Headers.QUEUE_MESSAGE_TYPE, "REQUEST");
        return map;
    }

    void printMessage(Map<String, Object> headers, String payload) {
        System.out.println("Message sent:\n" + payload);
        headers.entrySet().forEach(entry -> System.out.println(entry.getKey() + " " + entry.getValue()));
    }

    public static String getQueueBaseName(String messageType, String applicationName, String context) {
        String queueType = messageType.equals("CD906C") ? "ADMIN" : "CORE";
        return queueMap.get(applicationName + "." + context + "." + queueType);

  }
}
