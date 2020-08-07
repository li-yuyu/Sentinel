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

	public static String getEnv(String active) {
		String activeLower = active.toLowerCase();
		if ("dev".equals(activeLower)) {
			return "dev";
		}
		if ("test1".equals(activeLower)) {
			return "lpt";
		}
		if ("test2".equals(activeLower)) {
			return "lpt";
		}
		if ("release".equals(activeLower)) {
			return "lpt";
		}
		if ("uat".equals(activeLower)) {
			return "uat";
		}
		if ("pro".equals(activeLower)) {
			return "pro";
		}
		return null;
	}

	public static String getCluster(String active) {
		String activeLower = active.toLowerCase();
		if ("dev".equals(activeLower)) {
			return "default";
		}
		if ("test1".equals(activeLower)) {
			return "test1";
		}
		if ("test2".equals(activeLower)) {
			return "test2";
		}
		if ("release".equals(activeLower)) {
			return "release";
		}
		if ("uat".equals(activeLower)) {
			return "default";
		}
		if ("pro".equals(activeLower)) {
			return "default";
		}
		return null;
	}
}
