///*
// * Copyright 1999-2018 Alibaba Group Holding Ltd.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.csp.sentinel.dashboard.rule.apollo;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
//import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
//import com.alibaba.csp.sentinel.datasource.Converter;
//import com.alibaba.csp.sentinel.util.StringUtil;
//import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
//import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
//import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
//
///**
// * @author Lyle
// * @since 2020年7月27日 上午9:42:17 
// */
//@Component("paramFlowRuleApolloProvider")
//public class ParamFlowRuleApolloProvider implements DynamicRuleProvider<List<ParamFlowRuleEntity>> {
//
//	@Value("${app.id}")
//	String appId;
//    @Autowired
//    private ApolloOpenApiClient apolloOpenApiClient;
//    @Autowired
//    private Converter<String, List<ParamFlowRuleEntity>> converter;
//
//    @Override
//    public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {
//        String ruleKey = ApolloConfigUtil.getFlowRuleKey(appName);
//        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, "DEV", "default", "BASE.sentinel-rule");
//        String rules = openNamespaceDTO
//            .getItems()
//            .stream()
//            .filter(p -> p.getKey().equals(ruleKey))
//            .map(OpenItemDTO::getValue)
//            .findFirst()
//            .orElse("");
//
//        if (StringUtil.isEmpty(rules)) {
//            return new ArrayList<>();
//        }
//        return converter.convert(rules);
//    }
//}
