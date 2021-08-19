package com.ugrong.framework.amqp.utils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * 字节工具类
 */
@NoArgsConstructor(access = PRIVATE)
public class ByteUtil {

	/**
	 * Str 2 bytes byte [ ].
	 *
	 * @param string the string
	 * @return the byte [ ]
	 */
	public static byte[] str2Bytes(String string) {
		return Optional.ofNullable(string).map(str -> str.getBytes(StandardCharsets.UTF_8)).orElse(null);
	}

	/**
	 * Bytes 2 str string.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
	public static String bytes2Str(byte[] bytes) {
		return Optional.ofNullable(bytes).map(b -> new String(b, StandardCharsets.UTF_8)).orElse(null);
	}
}
