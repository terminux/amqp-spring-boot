package com.ugrong.framework.amqp.container;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

import com.ugrong.framework.amqp.annotation.HandlerApi;
import com.ugrong.framework.amqp.autoconfigure.properties.AmqpProperties;
import com.ugrong.framework.amqp.handler.MethodProxyQueueHandler;
import com.ugrong.framework.amqp.utils.MessageQueueUtil;

@Slf4j
public class SpringQueueHandlerContainer extends AbstractQueueHandlerContainer implements ApplicationContextAware, SmartInitializingSingleton {

	private static ApplicationContext context;

	public SpringQueueHandlerContainer(AmqpProperties amqpProperties, ConfigurableListableBeanFactory beanFactory) {
		super(amqpProperties, beanFactory);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	@Override
	public void init() {
		if (context == null) {
			return;
		}
		// init queue handler from method
		String[] beanDefinitionNames = context.getBeanNamesForType(Object.class, false, true);
		for (String beanDefinitionName : beanDefinitionNames) {
			Object bean = context.getBean(beanDefinitionName);

			Map<Method, HandlerApi> annotatedMethods = null;   // referred to ：org.springframework.context.event.EventListenerMethodProcessor.processBean
			try {
				annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
						new MethodIntrospector.MetadataLookup<HandlerApi>() {
							@Override
							public HandlerApi inspect(Method method) {
								return AnnotatedElementUtils.findMergedAnnotation(method, HandlerApi.class);
							}
						});
			}
			catch (Throwable ex) {
				log.error("message queue handler method resolve error for bean[" + beanDefinitionName + "].", ex);
			}
			if (ObjectUtils.isEmpty(annotatedMethods)) {
				continue;
			}
			for (Map.Entry<Method, HandlerApi> methodHandlerEntry : annotatedMethods.entrySet()) {
				Method handlerMethod = methodHandlerEntry.getKey();
				HandlerApi handlerApi = methodHandlerEntry.getValue();
				if (handlerApi == null) {
					continue;
				}
				String queueName = handlerApi.queueName();
				Assert.isTrue(StringUtils.isNotBlank(queueName),
						MessageQueueUtil.errorMsg("queue handler [queueName] invalid", bean, handlerMethod));

				Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
				Assert.notEmpty(parameterTypes, MessageQueueUtil.errorMsg("queue handler method parameter is empty.",
						bean, handlerMethod));

				handlerMethod.setAccessible(true);
				// registry handler
				this.put(queueName, handlerApi.order(), new MethodProxyQueueHandler<>(bean, handlerMethod));
			}
		}
		SimpleMessageListenerContainer listenerContainer = context.getBean(SimpleMessageListenerContainer.class);
		Set<String> queueNames = this.getQueueNames();
		log.debug("Listen queues:[{}]", queueNames);
		if (ObjectUtils.isNotEmpty(queueNames)) {
			listenerContainer.setQueueNames(queueNames.toArray(new String[0]));
		}
	}

	@Override
	public void afterSingletonsInstantiated() {
		this.init();
	}
}
