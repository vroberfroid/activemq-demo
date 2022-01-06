package be.fgov.minfin.activemqdemo.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    @JmsListener(destination = "${ccngateway.generator.queue.fromeurope}")
    public void receiveMessage(String payload) {
        System.out.println("Message received from Europe <" + payload + ">");
    }
}
