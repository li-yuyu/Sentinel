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

/**
 * @author Lyle
 * @since 2020年7月27日 下午2:44:17 
 */
public final class ApolloConfigUtil {

    public static final String FLOW_RULE_KEY_POSTFIX = "-flow-rules";
    public static final String AUTHORITY_RULE_KEY_POSTFIX = "-authority-rules";
    public static final String DEGRADE_RULE_KEY_POSTFIX = "-degrade-rules";
    public static final String PARAM_FLOW_RULE_KEY_POSTFIX = "-param-flow-rules";
    public static final String SYSTEM_RULE_KEY_POSTFIX = "-system-rules";

    private ApolloConfigUtil() {
    }

    public static String getFlowRuleKey(String appName) {
        return String.format("%s%s", appName, FLOW_RULE_KEY_POSTFIX);
    }
    public static String getAuthorityRuleKey(String appName) {
    	return String.format("%s%s", appName, AUTHORITY_RULE_KEY_POSTFIX);
    }
    public static String getDegradeRuleKey(String appName) {
    	return String.format("%s%s", appName, DEGRADE_RULE_KEY_POSTFIX);
    }
    public static String getParamFlowRuleKey(String appName) {
    	return String.format("%s%s", appName, PARAM_FLOW_RULE_KEY_POSTFIX);
    }
    public static String getSystemRuleKey(String appName) {
    	return String.format("%s%s", appName, SYSTEM_RULE_KEY_POSTFIX);
    }
}
