package campeonato.com.Campeonato.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PARTIDA = "exchange.partida";
    public static final String QUEUE_PARTIDA = "queue.partida";
    public static final String ROUTING_KEY_PARTIDA = "routing.partida";

    public static final String EXCHANGE_PARTIDA_ERRO = "exchange.partida.erro";
    public static final String QUEUE_PARTIDA_ERRO = "queue.partida.erro";
    public static final String ROUTING_KEY_ERRO = "routing.partida.erro";


    @Bean
    public DirectExchange exchangePartida() {
        return new DirectExchange(EXCHANGE_PARTIDA);
    }

    @Bean
    public Queue queuePartida() {
        return QueueBuilder.durable(QUEUE_PARTIDA)
                .withArgument("x-dead-letter-exchange", EXCHANGE_PARTIDA_ERRO)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_ERRO)
                .build();
    }

    @Bean
    public Binding bindingPartida(Queue queuePartida, DirectExchange exchangePartida) {
        return BindingBuilder.bind(queuePartida).to(exchangePartida).with(ROUTING_KEY_PARTIDA);
    }


    @Bean
    public DirectExchange exchangePartidaErro() {
        return new DirectExchange(EXCHANGE_PARTIDA_ERRO);
    }

    @Bean
    public Queue queuePartidaErro() {
        return QueueBuilder.durable(QUEUE_PARTIDA_ERRO).build();
    }

    @Bean
    public Binding bindingPartidaErro(Queue queuePartidaErro, DirectExchange exchangePartidaErro) {
        return BindingBuilder.bind(queuePartidaErro).to(exchangePartidaErro).with(ROUTING_KEY_ERRO);
    }
}
