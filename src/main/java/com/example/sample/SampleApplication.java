package com.example.sample;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.Collections;
import java.util.Optional;
@SpringBootApplication
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
	
	@Bean(name = {"vcap"})
	public Optional<Object> vcap(){
		try {
		    JSONObject vcap_services = new JSONObject(System.getenv("VCAP_SERVICES"));
		    return Optional.of(Configuration.defaultConfiguration().jsonProvider().parse(vcap_services.toString()));
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		return Optional.empty();
		
	}
	
	@Bean
	public Optional<OAuth2RestOperations> restTemplate() {
        return vcap().map(jsonObject -> {
            DefaultOAuth2ClientContext defaultOAuth2ClientContext = new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest());
            OAuth2RestTemplate template = new OAuth2RestTemplate(fullAccessesDetails(jsonObject), defaultOAuth2ClientContext);
            System.out.println("-------------- This line is reached ----------------1"+ jsonObject);
            template.setInterceptors(Collections.singletonList((request, body, execution) -> {
                request.getHeaders().setContentType(MediaType.APPLICATION_JSON);                
                return execution.execute(request, body);
            }));

	    @SuppressWarnings("unused")
			OAuth2AccessToken accessToken = template.getAccessToken();//for caching
            return template;
        });
	}

    private OAuth2ProtectedResourceDetails fullAccessesDetails(Object document) {
        String host = "https://cppmindia.authentication.eu10.hana.ondemand.com"; //Change the URL to match
        //String clientId = JsonPath.read(document, "$.metering[0].credentials.client_id");
        String clientId = "sb-sampleapplication!t216";//JsonPath.read(document, "$.credentials.client_id");
        //String secret = JsonPath.read(document, "$.metering[0].credentials.client_secret");
        String secret = "7zAN+t0Hzz8bp+6uKh3xQnJuYVU=";//JsonPath.read(document, "$.credentials.client_secret");
        String tokenUrl = host + "/oauth/token?grant_type=client_credentials";

        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
        resource.setAccessTokenUri(tokenUrl);
        resource.setClientId(clientId);
        resource.setClientSecret(secret);
        System.out.println("This line is reached ----------------");
        return resource;
    }
}
