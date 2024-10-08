package com.becoder.service;

import org.springframework.stereotype.Service;

import com.becoder.entity.User;


public interface UserService {
     
	public User saveUser(User user,String url);
	
	public void removeSessionMessage();
	
	public void sendEmail(User user,String path);
	
	public boolean verifyAccount(String veroficationCode);
	
	public void increaseFailedAttempt(User user);
	public void resetAttempt(String email);
	public void lock(User user);
	public boolean unlockAccountTimeExpired(User user);
}
