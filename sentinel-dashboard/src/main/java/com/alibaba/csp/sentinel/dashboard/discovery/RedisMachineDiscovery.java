package com.alibaba.csp.sentinel.dashboard.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.util.RedisLock;
import com.alibaba.csp.sentinel.util.AssertUtil;

@Component
public class RedisMachineDiscovery implements MachineDiscovery {

	public static final String LOCK_PREFIX = "sentinel:discovery:lock:";
	public static final String APPS_KEY = "sentinel:discovery:apps";

	@Autowired
	@Qualifier("stringRedisTemplate")
	public RedisTemplate<String, String> stringRedisTemplate;
	@Autowired
	@Qualifier("appInfoRedisTemplate")
	public RedisTemplate<String, AppInfo> appInfoRedisTemplate;

	@Override
	public long addMachine(MachineInfo machineInfo) {
		AssertUtil.notNull(machineInfo, "machineInfo cannot be null");

		AppInfo appInfo = new AppInfo(machineInfo.getApp(), machineInfo.getAppType());
		appInfo.addMachine(machineInfo);

		BoundHashOperations<String, String, AppInfo> boundHashOps = appInfoRedisTemplate.boundHashOps(APPS_KEY);

		if (boundHashOps.putIfAbsent(machineInfo.getApp(), appInfo)) {
			return 1;
		}

		String lockKey = LOCK_PREFIX + machineInfo.getApp();

		RedisLock redisLock = new RedisLock(stringRedisTemplate, lockKey, 60 * 1000);
		try {
			if (redisLock.tryLockAndSpinWait()) {
				AppInfo info = boundHashOps.get(machineInfo.getApp());
				info.addMachine(machineInfo);
				boundHashOps.put(machineInfo.getApp(), info);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			redisLock.unlock();
		}
		return 1;
	}

	@Override
	public boolean removeMachine(String app, String ip, int port) {
		AssertUtil.assertNotBlank(app, "app name cannot be blank");

		String lockKey = LOCK_PREFIX + app;

		RedisLock redisLock = new RedisLock(stringRedisTemplate, lockKey, 60L);
		try {
			if (redisLock.tryLock()) {
				BoundHashOperations<String, String, AppInfo> boundHashOps = appInfoRedisTemplate.boundHashOps(APPS_KEY);

				AppInfo appInfo = boundHashOps.get(app);
				if (appInfo != null) {
					appInfo.removeMachine(ip, port);
					boundHashOps.put(app, appInfo);
				}
				return true;
			}
		} finally {
			redisLock.unlock();
		}
		return false;
	}

	@Override
	public List<String> getAppNames() {
		BoundHashOperations<String, String, AppInfo> boundHashOps = appInfoRedisTemplate.boundHashOps(APPS_KEY);
		return new ArrayList<>(boundHashOps.keys());
	}

	@Override
	public AppInfo getDetailApp(String app) {
		AssertUtil.assertNotBlank(app, "app name cannot be blank");
		BoundHashOperations<String, String, AppInfo> boundHashOps = appInfoRedisTemplate.boundHashOps(APPS_KEY);
		return boundHashOps.get(app);
	}

	@Override
	public Set<AppInfo> getBriefApps() {
		BoundHashOperations<String, String, AppInfo> boundHashOps = appInfoRedisTemplate.boundHashOps(APPS_KEY);
		return new HashSet<>(boundHashOps.values());
	}

	@Override
	public void removeApp(String app) {
		AssertUtil.assertNotBlank(app, "app name cannot be blank");
		BoundHashOperations<String, String, AppInfo> boundHashOps = appInfoRedisTemplate.boundHashOps(APPS_KEY);
		boundHashOps.delete(app);
	}

}
