package com.ugrong.framework.amqp.message;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;

import com.ugrong.framework.amqp.IEvent;
import com.ugrong.framework.amqp.IMessagePublisher;
import com.ugrong.framework.amqp.exception.AmqpException;

/**
 * 消息消费失败之后的回调
 */
@AllArgsConstructor
public class MessageRecover implements MessageRecoverer {

	private final IMessagePublisher messagePublisher;

	@Override
	public void recover(Message message, Throwable cause) {
		Throwable rootCause = ExceptionUtils.getRootCause(cause);
		if (rootCause instanceof AmqpException) {
			Object result = ((AmqpException) rootCause).getResult();
			if (result instanceof IEvent) {
				//发送到死信队列
				messagePublisher.publish2DeadLetter((IEvent) result);
			}
		}
	}
}