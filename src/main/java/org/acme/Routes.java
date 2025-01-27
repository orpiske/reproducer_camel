package org.acme;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class Routes extends RouteBuilder {
    private static final Logger LOG = Logger.getLogger(Routes.class);

    @ConfigProperty(name = "scenario.name", defaultValue = "jms")
    String scenario;

    @Override
    public void configure() throws Exception {
        LOG.infof("Preparing test for scenario %s", scenario);

        ; // around 20'000 messages/sec
//        String scenario = "jms-to-kafka"; // around 4000 messages/sec
//        String scenario = "jms-to-kafka-tx"; // around 150 messages/sec

//        String scenario = "kafka"; // around 100'000 messages/sec
//        String scenario = "kafka-to-jms"; // around 150 messages/sec
//        String scenario = "kafka-to-jms-manual-commit"; // around 90 messages/sec

        if(scenario.equals("jms")) {

            // curl -X POST localhost:18080/hello/send-jms?count=1000

            from("jms:queue:my-queue?concurrentConsumers=10")
                    .bean("my-bean", "fromJMS");

        } else if(scenario.equals("jms-to-kafka")) {

            // curl -X POST localhost:18080/hello/send-jms?count=1000

            from("jms:queue:my-queue?concurrentConsumers=10&asyncConsumer=true")
                    .bean("my-bean", "fromJMS")
                    .to("kafka:my-topic");

            from("kafka:my-topic")
                    .bean("my-bean", "fromKafka");

        }  else if(scenario.equals("jms-to-kafka-tx")) {

            // curl -X POST localhost:18080/hello/send-jms?count=1000

            from("jms:queue:my-queue?concurrentConsumers=10&transacted=true")
                    .bean("my-bean", "fromJMS")
                    .to("kafka:my-topic");

            from("kafka:my-topic")
                    .bean("my-bean", "fromKafka");


        } else if (scenario.equals("kafka-to-jms")) {

            // curl -X POST localhost:18080/hello/send-kafka?count=1000

            from("kafka:my-topic")
                .bean("my-bean", "fromKafka")
                .to("jms:queue:my-queue");

            from("jms:queue:my-queue?concurrentConsumers=10")
                    .bean("my-bean", "fromJMS");

        } else if (scenario.equals("kafka-to-jms-manual-commit")) {

            // curl -X POST localhost:18080/hello/send-kafka?count=1000

            from("kafka:my-topic?allowManualCommit=true&autoCommitEnable=false")
                .bean("my-bean", "fromKafka")
                .to("jms:queue:my-queue")
                    .bean("my-bean", "commitKafka");

            from("jms:queue:my-queue?concurrentConsumers=10")
                    .bean("my-bean", "fromJMS");

        } else if (scenario.equals("kafka")) {

            // curl -X POST localhost:18080/hello/send-kafka?count=1000

            from("kafka:my-topic")
                    .bean("my-bean", "fromKafka");

        }
    }
}
