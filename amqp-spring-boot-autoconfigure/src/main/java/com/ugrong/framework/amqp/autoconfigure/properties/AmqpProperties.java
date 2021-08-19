package com.ugrong.framework.amqp.autoconfigure.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "app.config.amqp")
public class AmqpProperties {

	/**
	 * 死信的交换机名
	 */
	private String deadLetterExchange = "dead-Letter-exchange";

	/**
	 * 死信的路由key
	 */
	private String deadLetterRoutingKey = "dead-letter-routing-key";

	/**
	 * 死信的队列名
	 */
	private String deadLetterQueue = "dead-letter-queue";

	/**
	 * 默认的路由key 当交换机与队列一一对应时可以使用该key
	 */
	private String defaultRoutingKey = "default-routing-key";

	/**
	 * 是否启用延时消息，默认不启用，启用时需要安装[rabbitmq-delayed-message-exchange]插件
	 */
	private boolean enableDelayedMessage = false;

	/**
	 * 最大重试次数（包含首次 所以该值应>1才有重试效果）默认：3
	 */
	private Integer retryMaxAttempts = 3;

	/**
	 * 重试的时间间隔 默认：3000，单位：毫秒
	 */
	private Long retryInitialInterval = 3000L;

	/**
	 * 重试的延迟倍数 默认：1； retryInitialInterval=5000l,retryMultiplier=2,则第一次重试为5秒，第二次为10秒，第三次为20秒...
	 */
	private Double retryMultiplier = 1D;

	/**
	 * 重试的最大间隔 默认：3000，单位：毫秒
	 */
	private Long retryMaxInterval = 3000L;

}
