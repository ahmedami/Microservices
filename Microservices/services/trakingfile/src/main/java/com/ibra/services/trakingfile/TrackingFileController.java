package com.ibra.services.trakingfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;

@RestController
public class TrackingFileController
{
	@Autowired
	private TrackingFileService trackingFileService;

	@RequestMapping("/tracking/{countryCode}")
	public String getFile(Model model, @PathVariable("countryCode") String countryCode, HttpServletRequest request) throws FileNotFoundException
	{
		//TrackingFileService1 trackingFileService1 = new TrackingFileService1();
//		return trackingFileService.buildHTML(request, countryCode);
		return "tracking file for country [" + countryCode + "] is loading; please wait!";
	}
}
