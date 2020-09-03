package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * 
 * @author Lyle
 * @since 2020年8月21日 下午1:52:31
 */
@Component
@Primary
public class RedisMetricsRepository implements MetricsRepository<MetricEntity> {

	private static final long MAX_METRIC_LIVE_TIME_MS = 1000 * 60 * 5;
	private static final long RESOURCE_RANK_TIME_MS = 1000 * 60;

	/**
	 * sentinel:resources:{app} -> zset{resource}
	 */
	public static final String RESOURCES_ZSET_KEY_PATTERN = "sentinel:resources:{0}";
	/**
	 * sentinel:mertrics:{app}:{resource} -> hash{timestamp : metric}
	 */
	public static final String METRICS_HASH_KEY_PATTERN = "sentinel:mertrics:{0}:{1}";

	@Autowired
	@Qualifier("stringRedisTemplate")
	public RedisTemplate<String, String> stringRedisTemplate;

	@Autowired
	@Qualifier("metricEntityRedisTemplate")
	public RedisTemplate<String, MetricEntity> metricEntityRedisTemplate;

	@Override
	public void save(MetricEntity entity) {
		long currentTimeMillis = TimeUtil.currentTimeMillis();

		String hashKey = MessageFormat.format(METRICS_HASH_KEY_PATTERN, entity.getApp(), entity.getResource());
		BoundHashOperations<String, Long, MetricEntity> boundHashOps = metricEntityRedisTemplate.boundHashOps(hashKey);

		String zsetKey = MessageFormat.format(RESOURCES_ZSET_KEY_PATTERN, entity.getApp());
		BoundZSetOperations<String, String> boundZSetOps = stringRedisTemplate.boundZSetOps(zsetKey);

		double score = 0;
		List<Long> expiredKeys = new ArrayList<Long>();

		Map<Long, MetricEntity> entries = boundHashOps.entries();
		Set<Entry<Long, MetricEntity>> entrySet = entries.entrySet();
		for (Entry<Long, MetricEntity> entry : entrySet) {
			if (entry.getKey() < currentTimeMillis - MAX_METRIC_LIVE_TIME_MS) {
				expiredKeys.add(entry.getKey());
			} else if (entry.getKey() > currentTimeMillis - RESOURCE_RANK_TIME_MS) {
				// Using last minute b_qps as score.
				score = score + entry.getValue().getBlockQps();
			}
		}

		if (!CollectionUtils.isEmpty(expiredKeys)) {
			boundHashOps.delete(expiredKeys.toArray());
		}
		boundHashOps.put(entity.getTimestamp().getTime(), entity);
		boundHashOps.expire(MAX_METRIC_LIVE_TIME_MS, TimeUnit.MILLISECONDS);
		boundZSetOps.add(entity.getResource(), score);
		boundZSetOps.expire(MAX_METRIC_LIVE_TIME_MS, TimeUnit.MILLISECONDS);
	}

	@Override
	public void saveAll(Iterable<MetricEntity> metrics) {
		if (metrics == null) {
			return;
		}
		metrics.forEach(this::save);
	}

	@Override
	public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
		List<MetricEntity> results = new ArrayList<>();
		if (StringUtil.isBlank(app)) {
			return results;
		}

		String hashKey = MessageFormat.format(METRICS_HASH_KEY_PATTERN, app, resource);

		BoundHashOperations<String, Long, MetricEntity> boundHashOps = metricEntityRedisTemplate.boundHashOps(hashKey);
		Map<Long, MetricEntity> metricsMap = boundHashOps.entries();
		if (metricsMap == null) {
			return results;
		}

		for (Entry<Long, MetricEntity> entry : metricsMap.entrySet()) {
			if (entry.getKey() >= startTime && entry.getKey() <= endTime) {
				results.add(entry.getValue());
			}
		}
		return results;
	}

	@Override
	public List<String> listResourcesOfApp(String app) {
		List<String> results = new ArrayList<>();
		if (StringUtil.isBlank(app)) {
			return results;
		}

		String zsetKey = MessageFormat.format(RESOURCES_ZSET_KEY_PATTERN, app);
		BoundZSetOperations<String, String> boundZSetOps = stringRedisTemplate.boundZSetOps(zsetKey);

		// Order by last minute b_qps DESC.
		Set<String> resources = boundZSetOps.reverseRange(0, Long.MAX_VALUE);
		if (resources == null) {
			return results;
		}
		results.addAll(resources);
		return results;
	}
}
