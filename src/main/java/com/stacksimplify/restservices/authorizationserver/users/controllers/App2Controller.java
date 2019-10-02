package com.stacksimplify.restservices.authorizationserver.users.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacksimplify.restservices.authorizationserver.users.entities.AppInfo;

@RestController
@RequestMapping("/app2")
public class App2Controller {
	
	@GetMapping("/hello1")
	public AppInfo hello1() {
		return new AppInfo("App2", "/hello1");
	}
	@GetMapping("/hello2")
	public AppInfo hello2() {
		return new AppInfo("App2", "/hello2");
	}


}
