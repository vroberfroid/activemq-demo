package be.fgov.minfin.activemqdemo.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Component
public class Receiver {

    @Transactional
    @JmsListener(destination = "${ccngateway.generator.queue.fromeurope}")
    public void receiveMessage(String payload, @Headers Map<String, Object> headers) {
        System.out.println("Message received from Europe <" + payload + ">");
        headers.forEach((k,v)->System.out.println("Header Key : " + k + " Value : " + v));
    }
    @Transactional
    @JmsListener(destination = "${ccngateway.generator.queue.fromieca}")
    public void receiveIECAMessage(byte[] payload, @Headers Map<String, Object> headers) throws IOException {
        System.out.println("Message received from IECA <" + payload.toString() + ">");
        headers.forEach((k,v)->System.out.println("Header Key : " + k + " Value : " + v));
        String contentType = (String)headers.get("ContentType");
        if(contentType.contains("octet") || contentType.contains("zip")) {

            File outputFile = new File("/tmp/outputFile.zip");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(payload);
            }
        }
    }
}
