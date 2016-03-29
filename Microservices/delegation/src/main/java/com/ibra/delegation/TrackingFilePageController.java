package com.ibra.delegation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;

@RestController
public class TrackingFilePageController
{
	private static final Logger LOG = LoggerFactory.getLogger(TrackingFilePageController.class);

	@Autowired
	TrackingFilePageService trackingFilePageService;
	@Autowired
	private LoadBalancerClient loadBalancer;
	private RestTemplate restTemplate = new RestTemplate();

	@RequestMapping("/tracking")
	String homePage()
	{
		return "Please specify the country for which you want to see the file. Example ../tracking-file/de";
	}

//	public TrackingFilePageController(TrackingFilePageService trackingFilePageService)
//	{
//		this.trackingFilePageService = trackingFilePageService;
//	}

	@RequestMapping("/tracking/{country}")
	public String getFile(Model model, @PathVariable("country") String country, @RequestHeader(value = "Authorization") String authorizationHeader,
						  Principal currentUser)
//	public String getFile(Model model, @PathVariable("country") String country)
	{
//		LOG.info("countrry: User={}, Auth={}, called with countrry={}", currentUser.getName(), authorizationHeader, country);
		String res = trackingFilePageService.getFile(country);
		model.addAttribute("file", res);
		return res;
	}

}
