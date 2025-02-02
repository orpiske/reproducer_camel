package org.acme;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

@ApplicationScoped
@Identifier("my-bean")
public class MyBean {

    private static final Logger log = LoggerFactory.getLogger(MyBean.class);

    static LongAdder countJMS = new LongAdder();
    static LongAdder countKafka = new LongAdder();

    public void fromJMS(String body) {
        // log.info("received jms " + body);
        countJMS.increment();
    }

    public void fromKafka(String body) {
        // log.info("received kafka " + body);
        countKafka.increment();
    }

    public void commitKafka(Exchange exchange) {
        KafkaManualCommit manualCommit = exchange.getIn().getHeader("CamelKafkaManualCommit", KafkaManualCommit.class);
        manualCommit.commit();
    }

    @Scheduled(every = "1s")
    public void logLastJms() {
        int value = countJMS.intValue();
        if (value != 0) {
            int rate = (int) (value * 1.0);
            String s = "Received from Jms: " + value + " in last 1 sec (" + rate + " messages/s)";
            log.info(s);
            countJMS.reset();
        }
    }

    @Scheduled(every = "1s")
    public void logLastKafka() {
        int value = countKafka.intValue();
        if (value != 0) {
            countKafka.reset();
            int rate = (int) (value * 1.0);
            String s = "Received from Kafka: " + value + " in last 1 sec (" + rate + " messages/s)";
            log.info(s);
        }
    }
}
