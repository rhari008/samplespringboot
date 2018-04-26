package com.example.sample.rest.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jayway.jsonpath.JsonPath;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfo;
import com.sap.xs2.security.container.UserInfoException;

@RestController
public class ExampleRestController {
	private static final String USAGE_URL = "https://abacus-usage-collector.cf.stagingaws.hanavlab.ondemand.com/v1/metering/collected/usage";
	private static final String USAGE_TEMPLATE = "{\n" +
        "  \"consumer_id\": \"sampleasdsadsaConsumerId\",\n" +
        "  \"start\": %s,\n" +
        "  \"end\": %s,\n" +
        "  \"measured_usage\": [\n" +
        "    {\n" +
        "      \"measure\": \"SAMPLE_SAAS\",\n" +
        "      \"quantity\": \"1500\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"organization_id\": \"idz:%s\",\n" +
        "  \"plan_id\": \"standard\",\n" +
        "  \"resource_id\": \"%s\",\n" +
        "  \"resource_instance_id\": \"sampleResourceInstanceId\",\n" +
        "  \"space_id\": \"na\"\n" +
        "}";
private Logger logger = LoggerFactory.getLogger(this.getClass());

@Autowired
Optional<OAuth2RestOperations> restTemplate;

@Autowired
@Qualifier(value = "vcap")
Optional<Object> vcap;

@GetMapping(path = "/usage", produces = { "text/html" })
public String sayHello() {
	Optional<UserInfo> userInfo = obtainUserInfo();
	userInfo.ifPresent(this::reportUsageForUser);
	return buildHelloMessage(userInfo);
}

private void reportUsageForUser(UserInfo userInfo) {
	try {
	    String identityZone = userInfo.getIdentityZone();
	    if (restTemplate.isPresent()) {
		ResponseEntity<String> entity = restTemplate.get().postForEntity(USAGE_URL, buildUsageReport(identityZone), String.class);
		if (entity.getStatusCodeValue() > 399)
		    logger.error(String.format("Failed to update usage for Tenant '%s' due to: %s", identityZone, entity.getBody()));
		else
		    logger.info(String.format("Updated usage for Tenant '%s'", identityZone));
	    } else
		logger.info("Rest template is not defined");
	} catch (Exception e) {
	    logger.error("Failed to obtain customer TenantID. Not reporting usage", e);
	}
}

private String buildUsageReport(String identityZone) {
	long now = System.currentTimeMillis();
	String resourceID = (String) vcap.map(o -> JsonPath.read(o, "$.metering[0].credentials.resource_id")).orElse("none");
	return String.format(USAGE_TEMPLATE, now - 1000, now, identityZone, resourceID);
}

private String buildHelloMessage(Optional<UserInfo> userInfo) {
	StringBuffer helloMessage = new StringBuffer("Hello ");
	userInfo.ifPresent(info -> consumeUserInfo(info, helloMessage));
	return helloMessage.toString();
}

private void consumeUserInfo(UserInfo userInfo, StringBuffer helloMessage) {
	// Use xs security lib to read user id and tenant id from security context
	try {
	    helloMessage.append(userInfo.getLogonName());
	    helloMessage.append("\n");

	    String idzone = userInfo.getIdentityZone();
	    helloMessage.append("TenantId = ");
	    helloMessage.append(idzone);
	    helloMessage.append("\n");
	    logger.info("sayHello endpoint called successfully for tenant: " + idzone);

	} catch (Exception e) {
	    logger.error("Failed to build Hello message", e);
	}
}

private Optional<UserInfo> obtainUserInfo() {
	UserInfo userInfo = null;
	try {
	    userInfo = SecurityContext.getUserInfo();
	} catch (Exception e) {
	    logger.error("Failed to obtain user info", e);
	}
	return Optional.ofNullable(userInfo);
}

@GetMapping(produces = { "text/html" })
public String sayHelloNoUsage() {
	
	StringBuffer helloMessage = new StringBuffer("Hello ");

	// Use xs security lib to read user id and tenant id from security context
	UserInfo userInfo;
	try {
		userInfo = SecurityContext.getUserInfo();
		helloMessage.append(userInfo.getLogonName());
		helloMessage.append("\n");

		String idzone = userInfo.getIdentityZone();
		helloMessage.append("TenantId = ");
		helloMessage.append(idzone);
		helloMessage.append("\n");
		logger.info("sayHello endpoint called successfully for tenant: " + idzone);
		
	} catch (UserInfoException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return helloMessage.toString();
}
}
