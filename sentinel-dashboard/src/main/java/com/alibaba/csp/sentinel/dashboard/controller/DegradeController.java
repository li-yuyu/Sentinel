/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.controller;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemDegradeRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.service.ApolloAuthManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Controller regarding APIs of degrade rules. Refactored since 1.8.0.
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
@RestController
@RequestMapping("/degrade")
public class DegradeController {

	private final Logger logger = LoggerFactory.getLogger(DegradeController.class);

	@Autowired
	private InMemDegradeRuleStore repository;
	@Autowired
	@Qualifier("degradeRuleApolloProvider")
	private DynamicRuleProvider<List<DegradeRuleEntity>> ruleProvider;
	@Autowired
	@Qualifier("degradeRuleApolloPublisher")
	private DynamicRulePublisher<List<DegradeRuleEntity>> rulePublisher;
	@Autowired
	ApolloAuthManager apolloAuthManager;

	@ResponseBody
	@RequestMapping("/rules.json")
	@AuthAction(PrivilegeType.READ_RULE)
	public Result<List<DegradeRuleEntity>> queryMachineRules(String app) {
		if (StringUtil.isEmpty(app)) {
			return Result.ofFail(-1, "app can't be null or empty");
		}
		try {
			List<DegradeRuleEntity> rules = ruleProvider.getRules(app);
			rules = repository.saveAll(rules);
			return Result.ofSuccess(rules);
		} catch (Throwable throwable) {
			logger.error("queryApps error:", throwable);
			return Result.ofThrowable(-1, throwable);
		}
	}

	@PostMapping("/rule")
	@AuthAction(PrivilegeType.WRITE_RULE)
	public Result<DegradeRuleEntity> apiAddRule(HttpServletRequest request, @RequestBody DegradeRuleEntity entity) {
		Result<DegradeRuleEntity> checkResult = checkEntityInternal(entity);
		if (checkResult != null) {
			return checkResult;
		}

		try {
			if (!apolloAuthManager.hasPermission(request, entity.getApp())) {
				return Result.ofFail(-1,
						MessageFormat.format("无权限,需要在Apollo拥有AppId:{0}的application.properties发布权限", entity.getApp()));
			}
		} catch (Exception e) {
			return Result.ofThrowable(-1, e);
		}

		Date date = new Date();
		entity.setGmtCreate(date);
		entity.setGmtModified(date);
		try {
			entity = repository.save(entity);
		} catch (Throwable t) {
			logger.error("Failed to add new degrade rule, app={}, ip={}", entity.getApp(), entity.getIp(), t);
			return Result.ofThrowable(-1, t);
		}
		if (!publishRules(entity.getApp())) {
			logger.warn("Publish degrade rules failed, app={}", entity.getApp());
		}
		return Result.ofSuccess(entity);
	}

	@PutMapping("/rule/{id}")
	@AuthAction(PrivilegeType.WRITE_RULE)
	public Result<DegradeRuleEntity> apiUpdateRule(HttpServletRequest request, @PathVariable("id") Long id,
			@RequestBody DegradeRuleEntity entity) {
		if (id == null || id <= 0) {
			return Result.ofFail(-1, "id can't be null or negative");
		}
		DegradeRuleEntity oldEntity = repository.findById(id);
		if (oldEntity == null) {
			return Result.ofFail(-1, "Degrade rule does not exist, id=" + id);
		}

		try {
			if (!apolloAuthManager.hasPermission(request, oldEntity.getApp())) {
				return Result.ofFail(-1, MessageFormat.format("无权限,需要在Apollo拥有AppId:{0}的application.properties发布权限",
						oldEntity.getApp()));
			}
		} catch (Exception e) {
			return Result.ofThrowable(-1, e);
		}

		entity.setApp(oldEntity.getApp());
		entity.setIp(oldEntity.getIp());
		entity.setPort(oldEntity.getPort());
		entity.setId(oldEntity.getId());
		Result<DegradeRuleEntity> checkResult = checkEntityInternal(entity);
		if (checkResult != null) {
			return checkResult;
		}

		entity.setGmtCreate(oldEntity.getGmtCreate());
		entity.setGmtModified(new Date());
		try {
			entity = repository.save(entity);
		} catch (Throwable t) {
			logger.error("Failed to save degrade rule, id={}, rule={}", id, entity, t);
			return Result.ofThrowable(-1, t);
		}
		if (!publishRules(entity.getApp())) {
			logger.warn("Publish degrade rules failed, app={}", entity.getApp());
		}
		return Result.ofSuccess(entity);
	}

	@DeleteMapping("/rule/{id}")
	@AuthAction(PrivilegeType.DELETE_RULE)
	public Result<Long> delete(HttpServletRequest request, @PathVariable("id") Long id) {
		if (id == null) {
			return Result.ofFail(-1, "id can't be null");
		}

		DegradeRuleEntity oldEntity = repository.findById(id);
		if (oldEntity == null) {
			return Result.ofSuccess(null);
		}

		try {
			if (!apolloAuthManager.hasPermission(request, oldEntity.getApp())) {
				return Result.ofFail(-1, MessageFormat.format("无权限,需要在Apollo拥有AppId:{0}的application.properties发布权限",
						oldEntity.getApp()));
			}
		} catch (Exception e) {
			return Result.ofThrowable(-1, e);
		}

		try {
			repository.delete(id);
		} catch (Throwable throwable) {
			logger.error("Failed to delete degrade rule, id={}", id, throwable);
			return Result.ofThrowable(-1, throwable);
		}
		if (!publishRules(oldEntity.getApp())) {
			logger.warn("Publish degrade rules failed, app={}", oldEntity.getApp());
		}
		return Result.ofSuccess(id);
	}

	private boolean publishRules(String app) {
		List<DegradeRuleEntity> rules = repository.findAllByApp(app);
		try {
			rulePublisher.publish(app, rules);
			return true;
		} catch (Exception e) {
			logger.error("publishRules failed, type={}", "degrade", e);
			return false;
		}
	}

	private <R> Result<R> checkEntityInternal(DegradeRuleEntity entity) {
		if (StringUtil.isBlank(entity.getApp())) {
			return Result.ofFail(-1, "app can't be blank");
		}
		if (StringUtil.isBlank(entity.getLimitApp())) {
			return Result.ofFail(-1, "limitApp can't be null or empty");
		}
		if (StringUtil.isBlank(entity.getResource())) {
			return Result.ofFail(-1, "resource can't be null or empty");
		}
		Double threshold = entity.getCount();
		if (threshold == null || threshold < 0) {
			return Result.ofFail(-1, "invalid threshold: " + threshold);
		}
		Integer recoveryTimeoutSec = entity.getTimeWindow();
		if (recoveryTimeoutSec == null || recoveryTimeoutSec <= 0) {
			return Result.ofFail(-1, "recoveryTimeout should be positive");
		}
		Integer strategy = entity.getGrade();
		if (strategy == null) {
			return Result.ofFail(-1, "circuit breaker strategy cannot be null");
		}
		if (strategy < CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType()
				|| strategy > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
			return Result.ofFail(-1, "Invalid circuit breaker strategy: " + strategy);
		}
		if (entity.getMinRequestAmount() == null || entity.getMinRequestAmount() <= 0) {
			return Result.ofFail(-1, "Invalid minRequestAmount");
		}
		if (entity.getStatIntervalMs() == null || entity.getStatIntervalMs() <= 0) {
			return Result.ofFail(-1, "Invalid statInterval");
		}
		if (strategy == RuleConstant.DEGRADE_GRADE_RT) {
			Double slowRatio = entity.getSlowRatioThreshold();
			if (slowRatio == null) {
				return Result.ofFail(-1, "SlowRatioThreshold is required for slow request ratio strategy");
			} else if (slowRatio < 0 || slowRatio > 1) {
				return Result.ofFail(-1, "SlowRatioThreshold should be in range: [0.0, 1.0]");
			}
		} else if (strategy == RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO) {
			if (threshold > 1) {
				return Result.ofFail(-1, "Ratio threshold should be in range: [0.0, 1.0]");
			}
		}
		return null;
	}

}
