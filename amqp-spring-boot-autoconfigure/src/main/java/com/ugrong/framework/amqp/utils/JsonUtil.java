package com.ugrong.framework.amqp.utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Json util.
 */
@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class JsonUtil {

	private final static ObjectMapper mapper;

	private final static String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	private final static String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

	static {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		//由于js的number只能表示15个数字，Long类型数字用String格式返回
		builder.serializerByType(Long.class, ToStringSerializer.instance);
		builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
		builder.serializerByType(long.class, ToStringSerializer.instance);

		//日期类型转换
		builder.simpleDateFormat(DEFAULT_DATETIME_PATTERN);

		//LocalDateTime按照 "yyyy-MM-dd HH:mm:ss"的格式进行序列化、反序列化
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

		//LocalDate按照 "yyyy-MM-dd"的格式进行序列化、反序列化
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
		javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
		javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));

		//是否缩放排列输出，默认false
		// builder.indentOutput(true);
		builder.timeZone("Asia/Shanghai");

		builder.modules(
				//识别Java8时间
				new ParameterNamesModule(),
				new Jdk8Module(),
				javaTimeModule
		);

		//启用
		builder.featuresToEnable(
				DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, //允许单个数值当做数组处理
				DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, //禁止使用int代表Enum的order()來反序列化Enum, 抛出异常
				JsonParser.Feature.ALLOW_SINGLE_QUOTES //识别单引号
		);
		mapper = builder.build();
	}

	/**
	 * Gets mapper.
	 *
	 * @return the mapper
	 */
	public static ObjectMapper getMapper() {
		return mapper;
	}

	/**
	 * 对象转json字符串.
	 *
	 * @param object the object
	 * @return the json string
	 */
	public static String toJsonStr(Object object) {
		if (Objects.nonNull(object)) {
			try {
				return mapper.writeValueAsString(object);
			}
			catch (JsonProcessingException e) {
				log.error("Failed to write value as json string.", e);
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	/**
	 * json字符串转java对象.
	 *
	 * @param <T>     the type parameter
	 * @param jsonStr the json str
	 * @param clazz   the clazz
	 * @return the t
	 */
	public static <T> T parseJsonStr(String jsonStr, Class<T> clazz) {
		if (StringUtils.isNotBlank(jsonStr) && Objects.nonNull(clazz)) {
			try {
				return mapper.readValue(jsonStr, clazz);
			}
			catch (IOException e) {
				log.error("Failed to convert json string to java bean.", e);
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	/**
	 * json字符串转java对象.
	 *
	 * @param <T>       the type parameter
	 * @param jsonStr   the json str
	 * @param reference the reference
	 * @return the t
	 */
	public static <T> T parseJsonStr(String jsonStr, TypeReference<T> reference) {
		if (StringUtils.isNotBlank(jsonStr) && Objects.nonNull(reference)) {
			try {
				return mapper.readValue(jsonStr, reference);
			}
			catch (JsonProcessingException e) {
				log.error("Failed to read json string value.", e);
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
