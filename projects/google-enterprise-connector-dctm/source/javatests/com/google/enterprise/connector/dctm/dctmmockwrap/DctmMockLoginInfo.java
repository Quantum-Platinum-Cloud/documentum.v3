package com.google.enterprise.connector.dctm.dctmmockwrap;

import com.google.enterprise.connector.dctm.dfcwrap.ILoginInfo;

public class DctmMockLoginInfo implements ILoginInfo {
	private String user;
	private String password;
	
	public DctmMockLoginInfo(){
		user=null;
		password=null;
	}
	
	public void setUser(String usr){
		this.user=usr;
	}
	
	public void setPassword(String pwd){
		this.password=pwd;
	}
<<<<<<< .mine

	public String getUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}
=======

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
>>>>>>> .r76
}
