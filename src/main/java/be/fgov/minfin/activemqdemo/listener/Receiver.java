package be.fgov.minfin.activemqdemo.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class Receiver {

    @Transactional
    @JmsListener(destination = "${ccngateway.generator.queue.fromeurope}")
    public void receiveMessage(String payload, @Headers Map<String, Object> headers) {
        System.out.println("Message received from Europe <" + payload + ">");
        headers.forEach((k,v)->System.out.println("Header Key : " + k + " Value : " + v));
    }
}
