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
package com.alibaba.csp.sentinel.dashboard.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.csp.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.dashboard.util.CookieUtil;
import com.alibaba.csp.sentinel.dashboard.util.HttpUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author Lyle
 * @since 2020年8月5日 上午10:33:35
 */
@Service
public class ApolloAuthManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApolloAuthManager.class);

	public static final String APOLLO_SESSIONID_KEY = "apolloSessionId";

	@Value("${apollo.portal.url}")
	private String apolloPortalUrl;
	@Value("${spring.profiles.active}")
	private String active;

	/**
	 * @param username
	 * @param password
	 * @return sessionId
	 * @throws Exception
	 */
	public String login(String username, String password) throws Exception {
		HttpResponse response = HttpUtils.post(apolloPortalUrl + "/signin", Arrays
				.asList(new BasicNameValuePair("username", username), new BasicNameValuePair("password", password)));

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 302) {
			LOGGER.warn("status code : {}, result :{}", statusCode, EntityUtils.toString(response.getEntity()));
			throw new RuntimeException("Apollo登录异常");
		}

		Header location = response.getFirstHeader("Location");
		Header setCookie = response.getFirstHeader("Set-Cookie");
		if (location != null && location.getValue() != null && location.getValue().equals(apolloPortalUrl + "/")
				&& setCookie != null && setCookie.getValue() != null) {
			HeaderElement[] elements = setCookie.getElements();
			for (HeaderElement headerElement : elements) {
				if ("JSESSIONID".equals(headerElement.getName())) {
					return headerElement.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * @param sessionId
	 * @return username
	 * @throws Exception
	 */
	public String getUsername(HttpServletRequest request) throws Exception {
		String sessionId = CookieUtil.getValue(request, APOLLO_SESSIONID_KEY);

		HttpResponse response = HttpUtils.get(apolloPortalUrl + "/user",
				new BasicHeader("Cookie", "JSESSIONID=" + sessionId));

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			LOGGER.warn("status code : {}, result :{}", statusCode, EntityUtils.toString(response.getEntity()));
			throw new RuntimeException("Apollo communication exception");
		}

		Header contentType = response.getEntity().getContentType();
		if (contentType.getValue().startsWith("application/json")) {
			String string = EntityUtils.toString(response.getEntity());
			JSONObject jsonObject = JSON.parseObject(string);
			String username = jsonObject.getString("userId");
			return username;
		}

		return null;
	}

	public boolean hasPermission(HttpServletRequest request, String app) throws Exception {
		String sessionId = CookieUtil.getValue(request, APOLLO_SESSIONID_KEY);

		if (hasAllEnvPermission(sessionId, app)) {
			return true;
		}
		if (hasEnvPermission(sessionId, app)) {
			return true;
		}
		return false;
	}

	public List<String> getAppsByOwner(HttpServletRequest request) throws Exception {
		String sessionId = CookieUtil.getValue(request, APOLLO_SESSIONID_KEY);
		String username = getUsername(request);

		String url = MessageFormat.format("{0}/apps/by-owner?owner={1}&page=0&size=10000", apolloPortalUrl, username);

		HttpResponse response = HttpUtils.get(url, new BasicHeader("Cookie", "JSESSIONID=" + sessionId));

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			LOGGER.warn("status code : {}, result :{}", statusCode, EntityUtils.toString(response.getEntity()));
			throw new RuntimeException("Apollo communication exception");
		}

		Header contentType = response.getEntity().getContentType();
		if (contentType.getValue().startsWith("application/json")) {
			String string = EntityUtils.toString(response.getEntity());
			List<JSONObject> jsonArray = JSON.parseArray(string, JSONObject.class);
			List<String> list = new ArrayList<String>();
			for (JSONObject jsonObject : jsonArray) {
				list.add(jsonObject.getString("appId"));
			}
			return list;
		}
		throw new RuntimeException("登录已失效");
	}

	/**
	 * 拥有所有环境的发布权限
	 * 
	 * @param sessionId
	 * @param app
	 * @return
	 * @throws Exception
	 */
	private boolean hasAllEnvPermission(String sessionId, String app) throws Exception {
		String url = MessageFormat.format("{0}/apps/{1}/namespaces/{2}/permissions/ReleaseNamespace", apolloPortalUrl,
				app, "application");

		HttpResponse response = HttpUtils.get(url, new BasicHeader("Cookie", "JSESSIONID=" + sessionId));

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			LOGGER.warn("status code : {}, result :{}", statusCode, EntityUtils.toString(response.getEntity()));
			throw new RuntimeException("Apollo communication exception");
		}

		Header contentType = response.getEntity().getContentType();
		if (contentType.getValue().startsWith("application/json")) {
			String string = EntityUtils.toString(response.getEntity());
			JSONObject jsonObject = JSON.parseObject(string);
			return jsonObject.getBoolean("hasPermission");
		}

		return false;
	}

	/**
	 * 拥有当前环境的发布权限
	 * 
	 * @param sessionId
	 * @param app
	 * @return
	 * @throws Exception
	 */
	private boolean hasEnvPermission(String sessionId, String app) throws Exception {
		String url = MessageFormat.format("{0}/apps/{1}/envs/{2}/namespaces/{3}/permissions/ReleaseNamespace",
				apolloPortalUrl, app, ApolloConfigUtil.getEnv(active), "application");

		HttpResponse response = HttpUtils.get(url, new BasicHeader("Cookie", "JSESSIONID=" + sessionId));

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			LOGGER.warn("status code : {}, result :{}", statusCode, EntityUtils.toString(response.getEntity()));
			throw new RuntimeException("Apollo communication exception");
		}

		Header contentType = response.getEntity().getContentType();
		if (contentType.getValue().startsWith("application/json")) {
			String string = EntityUtils.toString(response.getEntity());
			JSONObject jsonObject = JSON.parseObject(string);
			return jsonObject.getBoolean("hasPermission");
		}

		return false;
	}

}
