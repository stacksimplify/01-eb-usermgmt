package com.stacksimplify.restservices.authorizationserver.users.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacksimplify.restservices.authorizationserver.users.entities.SimpleMessage;

@RestController
public class HelloWorldController {

	@GetMapping("/hello")
	public String simpleHello() {
		
        Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
        String strDate = dateFormat.format(date);
        
		return "Hello World-CICD-V5: "+strDate;
	}
	
	
	@GetMapping("/hello-bean")
	public SimpleMessage helloBean() {
		return new SimpleMessage("Hello World Bean", "V1");
	}
	
	
}
