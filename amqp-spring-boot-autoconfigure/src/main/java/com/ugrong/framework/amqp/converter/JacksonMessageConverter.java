package com.ugrong.framework.amqp.converter;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.ugrong.framework.amqp.utils.ByteUtil;
import com.ugrong.framework.amqp.utils.JsonUtil;

/**
 * jackson json 序列化转化器
 */
@Slf4j
public class JacksonMessageConverter extends AbstractMessageConverter {

	@Override
	protected Message createMessage(Object obj, MessageProperties properties) {
		byte[] body = ByteUtil.str2Bytes(this.toJsonStr(obj));
		return buildMessage(obj, body, properties);
	}

	private Message buildMessage(Object obj, byte[] body, MessageProperties properties) {
		properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		properties.setContentEncoding(StandardCharsets.UTF_8.name());
		properties.setType(obj.getClass().getName());
		properties.setContentLength(body.length);
		// 设置消息持久化
		properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
		return new Message(body, properties);
	}

	protected String toJsonStr(Object obj) {
		return JsonUtil.toJsonStr(obj);
	}

	protected Object parseJsonStr(String jsonStr, String className) throws MessageConversionException {
		try {
			return JsonUtil.parseJsonStr(jsonStr, Class.forName(className));
		}
		catch (ClassNotFoundException e) {
			log.error("Failed to convert message content.", e);
			throw new MessageConversionException(e.getMessage());
		}
	}

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		String type = message.getMessageProperties().getType();
		if (StringUtils.isNotBlank(type)) {
			String jsonBody = ByteUtil.bytes2Str(message.getBody());
			return this.parseJsonStr(jsonBody, type);
		}
		return null;
	}
}