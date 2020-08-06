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
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
//import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
//import com.alibaba.csp.sentinel.datasource.Converter;
//import com.alibaba.csp.sentinel.util.AssertUtil;
//import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
//import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
//import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
//
///**
// * @author Lyle
// * @since 2020年7月27日 上午9:42:28 
// */
//@Component("paramFlowRuleApolloPublisher")
//public class ParamFlowRuleApolloPublisher implements DynamicRulePublisher<List<ParamFlowRuleEntity>> {
//
//	@Value("${app.id}")
//	String appId;
//    @Autowired
//    private ApolloOpenApiClient apolloOpenApiClient;
//    @Autowired
//    private Converter<List<ParamFlowRuleEntity>, String> converter;
//
//    @Override
//    public void publish(String app, List<ParamFlowRuleEntity> rules) throws Exception {
//        AssertUtil.notEmpty(app, "app name cannot be empty");
//        if (rules == null) {
//            return;
//        }
//
//        // Increase the configuration
//        String ruleKey = ApolloConfigUtil.getParamFlowRuleKey(app);
//        OpenItemDTO openItemDTO = new OpenItemDTO();
//        openItemDTO.setKey(ruleKey);
//        openItemDTO.setValue(converter.convert(rules));
//        openItemDTO.setComment(app+"热点参数流控规则");
//        openItemDTO.setDataChangeCreatedBy("liyuyu");
//        apolloOpenApiClient.createOrUpdateItem(appId, "DEV", "default", "BASE.sentinel-rule", openItemDTO);
//
//        // Release configuration
//        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
//        namespaceReleaseDTO.setEmergencyPublish(true);
//        namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
//        namespaceReleaseDTO.setReleasedBy("liyuyu");
//        namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
//        apolloOpenApiClient.publishNamespace(appId, "DEV", "default", "BASE.sentinel-rule", namespaceReleaseDTO);
//    }
//}
