package com.ugrong.framework.amqp.exception;

/**
 * 自定义异常
 */
public class AmqpException extends RuntimeException {

	private Object result;

	public AmqpException(String message, Object result) {
		super(message);
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
