package be.fgov.minfin.activemqdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.lang.Nullable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

@Configuration
public class ActivemqConfig {

    @Bean
    public JmsMessagingTemplate jmsMessagingTemplate(@Autowired JmsTemplate jmsTemplate) {
        return new JmsMessagingTemplate(jmsTemplate);
    }

    @Bean
    public DynamicDestinationResolver destinationResolver() {
        return new DynamicDestinationResolver() {
            @Override
            public Destination resolveDestinationName(@Nullable Session session, String destinationName, boolean pubSubDomain) throws JMSException {
                if (destinationName.startsWith("VirtualTopic.")) {
                    pubSubDomain = true;
                }
                return super.resolveDestinationName(session, destinationName, pubSubDomain);
            }
        };
    }
}
