package com.ugrong.framework.amqp.samples;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ugrong.framework.amqp.IMessagePublisher;
import com.ugrong.framework.amqp.samples.event.TestEvent;

@SpringBootTest
class AmqpSamplesApplicationTests {

	@Autowired
	private IMessagePublisher publisher;

	@Test
	void publishMessageTest() throws InterruptedException {
		for (int i = 0; i <= 5; i++) {
			TestEvent event = new TestEvent(i, "action" + i);
			publisher.publish(event);
		}
		Thread.sleep(2000);
	}
}
