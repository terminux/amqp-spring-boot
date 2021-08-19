package com.ugrong.framework.amqp;

import java.util.concurrent.TimeUnit;

public interface IMessagePublisher {

	/**
	 * 发送一条消息
	 *
	 * @param event 消息体
	 */
	void publish(IEvent event);

	/**
	 * 发送一条延时消息；
	 * 需要安装 rabbitmq-delayed-message-exchange 插件，
	 *
	 * @param event     消息体
	 * @param delay     延时时间
	 * @param delayUnit 延时时间单位
	 */
	void publish(IEvent event, long delay, TimeUnit delayUnit);

	/**
	 * 发送消息到死信队列
	 *
	 * @param event 消息体
	 */
	void publish2DeadLetter(IEvent event);
}
