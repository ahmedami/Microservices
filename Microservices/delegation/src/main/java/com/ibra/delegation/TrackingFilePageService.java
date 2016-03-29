package com.ibra.delegation;

import com.ibra.delegation.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class TrackingFilePageService
{
	private RestTemplate restTemplate = new RestTemplate();
	@Autowired
	private LoadBalancerClient loadBalancer;
	@Autowired
	Util util;
	private static final Logger LOG = LoggerFactory.getLogger(TrackingFilePageService.class);

//	protected String serviceUrl;
//
//	public TrackingFilePageService(String serviceUrl)
//	{
//		this.serviceUrl = serviceUrl.startsWith("http") ? serviceUrl : "http://" + serviceUrl;
//	}

	public String getFile(String country)

	{
		URI uri = util.getServiceUrl("TRACKING-FILE-SERVICE", "http://localhost:8081");
		String url = uri.toString() + "/tracking/" + country;
		ResponseEntity<String> resultStr = restTemplate.getForEntity(url, String.class);
		LOG.debug("GetProduct http-status: {}", resultStr.getStatusCode());
		LOG.debug("GetProduct body: {}", resultStr.getBody());
		return resultStr.toString();
	}
}
