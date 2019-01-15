package com.thoughtmechanix.licenses.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceConfig{

  @Value("${example.property}")
  private String exampleProperty="";

  @Value("${redis.server}")
  private String redisServer="";

  @Value("${redis.port}")
  private String redisPort="";

  @Value("${signing.key}")
  private String jwtSigningKey="";

  @Value("${hystrix.commandproperties.errorRequestThresholdPercentage}")
  private String errorRequestThresholdPercentage="";
  
  @Value("${zuul.server.addr}")
  private String zuulServerAddr="";

  public String getErrorRequestThresholdPercentage() {
	return errorRequestThresholdPercentage;
  }

  public String getJwtSigningKey() {
    return jwtSigningKey;
  }

  public String getExampleProperty(){
    return exampleProperty;
  }

  public String getRedisServer(){
    return redisServer;
  }

  public Integer getRedisPort(){
    return new Integer( redisPort ).intValue();
  }
  
  public String getZuulServerAddr() {
	return zuulServerAddr;
  }
  
}
