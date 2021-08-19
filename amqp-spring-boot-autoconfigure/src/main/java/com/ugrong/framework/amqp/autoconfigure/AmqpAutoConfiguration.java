package com.ugrong.framework.amqp.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import com.ugrong.framework.amqp.IMessagePublisher;
import com.ugrong.framework.amqp.IQueueHandlerContainer;
import com.ugrong.framework.amqp.autoconfigure.properties.AmqpProperties;
import com.ugrong.framework.amqp.container.SpringQueueHandlerContainer;
import com.ugrong.framework.amqp.converter.JacksonMessageConverter;
import com.ugrong.framework.amqp.message.MessageRecover;
import com.ugrong.framework.amqp.message.RabbitMessageListener;
import com.ugrong.framework.amqp.publisher.RabbitMessagePublisherImpl;

@Configuration
@EnableRabbit
@ConditionalOnClass(RabbitTemplate.class)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@EnableConfigurationProperties(AmqpProperties.class)
@AllArgsConstructor
@Slf4j
public class AmqpAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public IQueueHandlerContainer queueHandlerContainer(AmqpProperties amqpProperties, ConfigurableListableBeanFactory beanFactory) {
		return new SpringQueueHandlerContainer(amqpProperties, beanFactory);
	}

	@Bean
	@ConditionalOnMissingBean
	public IMessagePublisher messagePublisher(AmqpProperties amqpProperties, RabbitTemplate rabbitTemplate) {
		return new RabbitMessagePublisherImpl(amqpProperties, rabbitTemplate);
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageRecover messageRecover(IMessagePublisher publisher) {
		return new MessageRecover(publisher);
	}

	@Bean
	@ConditionalOnMissingBean
	public RetryOperationsInterceptor messageRetryOperationsInterceptor(MessageRecover messageRecover, AmqpProperties amqpProperties) {
		return RetryInterceptorBuilder
				.stateless()
				.maxAttempts(amqpProperties.getRetryMaxAttempts())
				.backOffOptions(amqpProperties.getRetryInitialInterval(), amqpProperties.getRetryMultiplier(),
						amqpProperties.getRetryMaxInterval())
				.recoverer(messageRecover)
				.build();
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageConverter messageConverter() {
		return new JacksonMessageConverter();
	}

	@Bean
	@ConditionalOnMissingBean
	public RabbitMessageListener rabbitMessageListener(MessageConverter messageConverter,
			IQueueHandlerContainer container) {
		return new RabbitMessageListener(messageConverter, container);
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory,
			RetryOperationsInterceptor interceptor,
			RabbitMessageListener messageListener) throws InterruptedException {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		container.setExposeListenerChannel(true);
		container.setMaxConcurrentConsumers(1);
		container.setConcurrentConsumers(1);
		//container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认
		container.setMessageListener(messageListener);
		container.setAdviceChain(interceptor);
		//重试次数超过上面的设置之后是否丢弃(消费者listener抛出异常，是否重回队列，默认true：重回队列， false为不重回队列(结合死信交换机))
		container.setDefaultRequeueRejected(false);
		return container;
	}
}
