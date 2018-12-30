package com.thoughtmechanix.licenses.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.thoughtmechanix.licenses.clients.OrganizationDiscoveryClient;
import com.thoughtmechanix.licenses.clients.OrganizationFeignClient;
import com.thoughtmechanix.licenses.clients.OrganizationRestTemplateClient;
import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.model.License;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.LicenseRepository;

@Service
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    ServiceConfig config;


    @Autowired
    OrganizationFeignClient organizationFeignClient;

    @Autowired
    OrganizationRestTemplateClient organizationRestClient;

    @Autowired
    OrganizationDiscoveryClient organizationDiscoveryClient;


    private Organization retrieveOrgInfo(String organizationId, String clientType){
        Organization organization = null;

        switch (clientType) {
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
            	System.out.println("I am using the default client");
                organization = organizationRestClient.getOrganization(organizationId);
        }

        return organization;
    }

    @HystrixCommand(
            commandProperties= {
            	@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="8000"),
        		@HystrixProperty(name="metrics.rollingStats.timeInMilliseconds", value="12000"),//要监控的窗口时间长度
        		@HystrixProperty(name="metrics.rollingStats.numBuckets", value="4"), //分次收集数据，次数要能被监控时间整除
        		@HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="4"), //监控阶段最小需要达到的请求次数
    			@HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="30"),//失败次数比例，如果达到30%
    			@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="30000") //断路后,再度尝试等待时间窗口
            },
        	fallbackMethod = "buildFallbackLicense",
        	threadPoolKey = "licenseByOrgThreadPool",
        	threadPoolProperties = {
        		@HystrixProperty(name = "coreSize", value="30"),
        		@HystrixProperty(name = "maxQueueSize", value="11")
        	}
        )
    public License getLicense(String organizationId,String licenseId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);

        Organization org = retrieveOrgInfo(organizationId, clientType);

        return license
                .withOrganizationName( org.getName())
                .withContactName( org.getContactName())
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() )
                .withComment(config.getExampleProperty());
    }

    
    @SuppressWarnings("unused")
	private License buildFallbackLicense(String organizationId,String licenseId, String clientType) {
    	License lic = new License();
    	lic.withId(licenseId);
    	lic.withProductName("sorry the licsence is not found now, please try it later.");
    	return lic;
    }
    
    
    @HystrixCommand(
        commandProperties= {
        	@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="8000"),
    		@HystrixProperty(name="metrics.rollingStats.timeInMilliseconds", value="12000"),//要监控的窗口时间长度
    		@HystrixProperty(name="metrics.rollingStats.numBuckets", value="4"), //分次收集数据，次数要能被监控时间整除
    		@HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="10"), //监控阶段最小需要达到的请求次数
			@HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="30"),//失败次数比例，如果达到30%
			@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="10000") //断路后,再度尝试等待时间窗口
        },
    	fallbackMethod = "buildFallbackLicenseList",
    	threadPoolKey = "licenseByOrgThreadPool",
    	threadPoolProperties = {
    		@HystrixProperty(name = "coreSize", value="30"),
    		@HystrixProperty(name = "maxQueueSize", value="11")
    	}
    )
    public List<License> getLicensesByOrg(String organizationId){
//    	randomlyRunLong();
    	List<License> lics = licenseRepository.findByOrganizationId( organizationId );     	
        return lics;
    }

    @SuppressWarnings("unused")
	private void randomlyRunLong() {
    	Random r = new Random();
    	int rn = r.nextInt(3)+1;
    	if(rn == 3) sleep();
    }
    
    private void sleep() {
    	try {
    		Thread.sleep(11000);
    	}catch(InterruptedException e){
    		e.printStackTrace();
    	}
    }
    
    protected List<License> buildFallbackLicenseList(String orgId){
    	List<License> fallbackList = new ArrayList<>();
    	License mockLicense = new License().withId("0000000-00-00000");
    	mockLicense.withOrganizationId(orgId).withProductName("Sorry no licensing informaton.");
    	fallbackList.add(mockLicense);
    	return fallbackList;
    }
    
    
    public void saveLicense(License license){
        license.withId( UUID.randomUUID().toString());

        licenseRepository.save(license);

    }

    public void updateLicense(License license){
      licenseRepository.save(license);
    }

    public void deleteLicense(License license){
        licenseRepository.deleteById( license.getLicenseId());
    }

}
