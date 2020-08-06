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
package com.alibaba.csp.sentinel.dashboard.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.controller.AuthController;
import com.alibaba.csp.sentinel.dashboard.service.ApolloAuthManager;
import com.alibaba.csp.sentinel.dashboard.util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author cdfive
 * @since 1.6.0
 */
@Component
@Primary
@ConditionalOnProperty(name = "auth.enabled", matchIfMissing = true)
public class ApolloWebAuthServiceImpl implements AuthService<HttpServletRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApolloWebAuthServiceImpl.class);

	public static final String WEB_SESSION_KEY = "session_sentinel_admin";

	@Autowired
	private ApolloAuthManager apolloAuthManager;

	@Override
	public AuthUser getAuthUser(HttpServletRequest request) {
		String apolloSessionId = CookieUtil.getValue(request, ApolloAuthManager.APOLLO_SESSIONID_KEY);
		String username;

		try {
			username = apolloAuthManager.getUsername(apolloSessionId);
		} catch (Exception e) {
			LOGGER.warn("Apollo communication exception", e);
			return null;
		}

		if (username == null) {
			return null;
		}
		return new ApolloWebAuthUserImpl(username);
	}
	
	@Override
	public boolean authTarget(HttpServletRequest request, String app, PrivilegeType privilegeType) throws Exception {
		
		if (privilegeType.equals(PrivilegeType.WRITE_RULE) || privilegeType.equals(PrivilegeType.DELETE_RULE)) {
			String apolloSessionId = CookieUtil.getValue(request, ApolloAuthManager.APOLLO_SESSIONID_KEY);
			return apolloAuthManager.hasPermission(apolloSessionId, app);
		}
		return true;
	}

	public static final class ApolloWebAuthUserImpl implements AuthUser {

		private String username;

		public ApolloWebAuthUserImpl(String username) {
			this.username = username;
		}

		@Override
		public boolean authTarget(String target, PrivilegeType privilegeType) {
			return true;
		}

		@Override
		public boolean isSuperUser() {
			return false;
		}

		@Override
		public String getNickName() {
			return username;
		}

		@Override
		public String getLoginName() {
			return username;
		}

		@Override
		public String getId() {
			return username;
		}
	}

}
