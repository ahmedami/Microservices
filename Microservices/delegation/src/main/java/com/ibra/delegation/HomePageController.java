package com.ibra.delegation;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//public class HomePageController implements ErrorController
public class HomePageController
{
	@RequestMapping("/")
	String home()
	{
		return "This is a home page, which is under construction :)";
	}
//	@RequestMapping("/error")
//	String error()
//	{
//		return "There is something wrong :(";
//	}
//	@Override
//	public String getErrorPath()
//	{
//		return "/error";
//	}
}
