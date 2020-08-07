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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.dashboard.auth.ApolloAuthServiceImpl;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.service.ApolloAuthManager;
import com.alibaba.csp.sentinel.dashboard.util.CookieUtil;

/**
 * @author cdfive
 * @since 1.6.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	private AuthService<HttpServletRequest> authService;

	@Autowired
	private ApolloAuthManager apolloAuthManager;

	@PostMapping("/login")
	public Result<AuthService.AuthUser> login(HttpServletRequest request, HttpServletResponse response, String username,
			String password) {
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			LOGGER.error("Login failed: Invalid username or password, username=" + username);
			return Result.ofFail(-1, "Invalid username or password");
		}

		try {
			String apolloSessionId = apolloAuthManager.login(username, password);
			if (apolloSessionId == null) {
				return Result.ofFail(-1, "Invalid username or password");
			}
			
			CookieUtil.set(response, ApolloAuthManager.APOLLO_SESSIONID_KEY, apolloSessionId, false);
			AuthService.AuthUser authUser = new ApolloAuthServiceImpl.ApolloAuthUserImpl(username);
			return Result.ofSuccess(authUser);
		} catch (Exception e) {
			LOGGER.warn("登录异常", e);
			return Result.ofFail(-1, "登录异常,请联系管理员");
		}
	}

	@PostMapping(value = "/logout")
	public Result<?> logout(HttpServletRequest request, HttpServletResponse response) {
		request.getSession().invalidate();
		CookieUtil.remove(request, response, ApolloAuthManager.APOLLO_SESSIONID_KEY);
		return Result.ofSuccess(null);
	}

	@PostMapping(value = "/check")
	public Result<?> check(HttpServletRequest request) {
		AuthService.AuthUser authUser = authService.getAuthUser(request);
		if (authUser == null) {
			return Result.ofFail(-1, "Not logged in");
		}
		return Result.ofSuccess(authUser);
	}
}
