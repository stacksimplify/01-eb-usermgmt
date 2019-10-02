package com.stacksimplify.restservices.authorizationserver.users.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacksimplify.restservices.authorizationserver.users.entities.AppInfo;

@RestController
@RequestMapping("/app1")
public class App1Controller {
	
	@GetMapping("/hello1")
	public AppInfo hello1() {
		return new AppInfo("App1", "/hello1");
	}
	@GetMapping("/hello2")
	public AppInfo hello2() {
		return new AppInfo("App1", "/hello2");
	}


}
