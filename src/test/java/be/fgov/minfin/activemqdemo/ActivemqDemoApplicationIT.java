package be.fgov.minfin.activemqdemo;

import be.fgov.minfin.activemqdemo.controller.Sender;
import be.fgov.minfin.activemqdemo.listener.Receiver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ActiveProfiles("mock")
@SpringBootTest
class ActivemqDemoApplicationIT {

    @Autowired
    Sender sender;

    @SpyBean
    Receiver receiver;

    @Test
    void testSender() throws URISyntaxException, IOException {
       sender.send("CD501C", "1234", "ECS", "CTA", "DE");
       verify(receiver, timeout(5000).times(1)).receiveMessage(any(), any());
    }

}
