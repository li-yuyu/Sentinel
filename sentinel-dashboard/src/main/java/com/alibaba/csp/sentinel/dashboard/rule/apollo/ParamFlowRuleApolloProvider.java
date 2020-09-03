package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;

/**
 * @author Lyle
 * @since 2020年7月27日 上午9:42:17
 */
@Component("paramFlowRuleApolloProvider")
public class ParamFlowRuleApolloProvider implements DynamicRuleProvider<List<ParamFlowRuleEntity>> {

	@Value("${app.id}")
	String appId;
	@Value("${apollo.rule.namespace}")
	String ruleNamespace;
	@Value("${spring.profiles.active}")
	private String active;
	@Autowired
	private ApolloOpenApiClient apolloOpenApiClient;
	@Autowired
	private Converter<String, List<ParamFlowRuleEntity>> converter;

	@Override
	public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {
		String ruleKey = ApolloConfigUtil.getParamFlowRuleKey(appName);
		OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, ApolloConfigUtil.getEnv(active),
				ApolloConfigUtil.getCluster(active), ruleNamespace);
		String rules = openNamespaceDTO.getItems().stream().filter(p -> p.getKey().equals(ruleKey))
				.map(OpenItemDTO::getValue).findFirst().orElse("");

		if (StringUtil.isEmpty(rules)) {
			return new ArrayList<>();
		}
		return converter.convert(rules);
	}
}
