package com.stacksimplify.restservices.authorizationserver.users.entities;

public class AppInfo {
	
	private String appname;
	private String apipath;
	public AppInfo(String appname, String apipath) {
		super();
		this.appname = appname;
		this.apipath = apipath;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getApipath() {
		return apipath;
	}
	public void setApipath(String apipath) {
		this.apipath = apipath;
	}

	


	

}
