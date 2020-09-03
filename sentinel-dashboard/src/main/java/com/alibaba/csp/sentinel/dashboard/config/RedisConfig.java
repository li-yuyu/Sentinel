package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean("stringRedisTemplate")
	public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());

		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Bean("longRedisTemplate")
	public RedisTemplate<String, Long> longRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Long> redisTemplate = new RedisTemplate<String, Long>();

		FastJsonRedisSerializer<Long> valueSerializer = new FastJsonRedisSerializer<Long>(Long.class);

		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(valueSerializer);
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(valueSerializer);

		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Bean("appInfoRedisTemplate")
	public RedisTemplate<String, AppInfo> appInfoRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, AppInfo> redisTemplate = new RedisTemplate<String, AppInfo>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		FastJsonRedisSerializer<Object> valueSerializer = new FastJsonRedisSerializer<Object>(Object.class);
		FastJsonRedisSerializer<AppInfo> hashValueSerializer = new FastJsonRedisSerializer<AppInfo>(AppInfo.class);

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(valueSerializer);
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(hashValueSerializer);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Bean("metricEntityRedisTemplate")
	public RedisTemplate<String, MetricEntity> metricEntityRedisTemplate(
			RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, MetricEntity> redisTemplate = new RedisTemplate<String, MetricEntity>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		FastJsonRedisSerializer<Object> valueSerializer = new FastJsonRedisSerializer<Object>(Object.class);
		FastJsonRedisSerializer<Object> hashKeySerializer = new FastJsonRedisSerializer<Object>(Object.class);
		FastJsonRedisSerializer<MetricEntity> hashValueSerializer = new FastJsonRedisSerializer<MetricEntity>(
				MetricEntity.class);

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(valueSerializer);
		redisTemplate.setHashKeySerializer(hashKeySerializer);
		redisTemplate.setHashValueSerializer(hashValueSerializer);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}
}
