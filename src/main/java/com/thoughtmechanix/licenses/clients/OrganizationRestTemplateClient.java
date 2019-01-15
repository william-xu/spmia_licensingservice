package com.thoughtmechanix.licenses.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.config.ServiceConfig;

@Component
public class OrganizationRestTemplateClient {
	private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);
	
    @Autowired
    ServiceConfig config;

    @Autowired
    OAuth2RestTemplate restTemplate;

    public Organization getOrganization(String organizationId){
   
        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(//这里的问题： 能否再度通过服务发现去发现zuul服务实例去调用组织服务，但是在引导类OAuth2RestTemplate处使用LoadBalanced注解会出错
//                		"http://zuulservice/api/organization/v1/organizations/{organizationId}",
                        config.getZuulServerAddr() + "/api/organization/v1/organizations/{organizationId}",
//                		  "http://192.168.43.80:9181/v1/organizations/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        return restExchange.getBody();
    }
}
