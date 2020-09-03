package com.alibaba.csp.sentinel.dashboard.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

public class RedisUtils {

	public static final String CURRENT_TIME_SCRIPT;
	
	static {
		StringBuilder sb = new StringBuilder();
		sb.append("local time = redis.call(\"time\") ");
		sb.append("return time[1]*1000 + time[2]/1000");
		CURRENT_TIME_SCRIPT = sb.toString();
	}

	public static Long currentTimeMillis(RedisTemplate<String, Long> redisTemplate) {
		return (Long) redisTemplate.execute(RedisScript.of(CURRENT_TIME_SCRIPT, Long.class), null, new String[] { "" });
	}
}
