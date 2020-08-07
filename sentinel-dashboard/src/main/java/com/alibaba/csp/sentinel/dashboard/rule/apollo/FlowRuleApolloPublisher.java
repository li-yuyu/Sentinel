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
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.auth.UserHolder;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;

/**
 * @author Lyle
 * @since 2020年7月27日 上午9:42:28
 */
@Component("flowRuleApolloPublisher")
public class FlowRuleApolloPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {

	@Value("${app.id}")
	String appId;
	@Value("${apollo.rule.namespace}")
	String ruleNamespace;
	@Value("${spring.profiles.active}")
	private String active;
	@Autowired
	private ApolloOpenApiClient apolloOpenApiClient;
	@Autowired
	private Converter<List<FlowRuleEntity>, String> converter;

	@Override
	public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
		AssertUtil.notEmpty(app, "app name cannot be empty");
		if (rules == null) {
			return;
		}

		// Increase the configuration
		String ruleKey = ApolloConfigUtil.getFlowRuleKey(app);
		OpenItemDTO openItemDTO = new OpenItemDTO();
		openItemDTO.setKey(ruleKey);
		openItemDTO.setValue(converter.convert(rules));
		openItemDTO.setComment(app + "流控规则");
		openItemDTO.setDataChangeCreatedBy(UserHolder.getCurrentUser().getId());
		apolloOpenApiClient.createOrUpdateItem(appId, ApolloConfigUtil.getEnv(active),
				ApolloConfigUtil.getCluster(active), ruleNamespace, openItemDTO);

		// Release configuration
		NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
		namespaceReleaseDTO.setEmergencyPublish(true);
		namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
		namespaceReleaseDTO.setReleasedBy(UserHolder.getCurrentUser().getId());
		namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
		apolloOpenApiClient.publishNamespace(appId, ApolloConfigUtil.getEnv(active),
				ApolloConfigUtil.getCluster(active), ruleNamespace, namespaceReleaseDTO);
	}
}
