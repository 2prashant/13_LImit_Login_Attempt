package com.becoder.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.becoder.entity.User;
import com.becoder.repository.UserRepo;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Override
	public User saveUser(User user,String url) {
		// TODO Auto-generated method stub
		
	    String	password=passwordEncoder.encode(user.getPassword());
		user.setPassword(password);
		user.setRole("ROLE_USER");
		
		user.setEnable(false);
		user.setVerificationCode(UUID.randomUUID().toString());
		
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		
		
		User newuser =userRepo.save(user);
		
		if(newuser!=null)
		{
			sendEmail(newuser, url);
		}
		
		return newuser;
	}
	
	@Override
	public void sendEmail(User user, String url) {
		// TODO Auto-generated method stub
		
		String from="prashant91209755506@gmail.com";// (sender)
		String to=user.getEmail();//which user (reciver)
		String subject="Account Verfication";//subject
		String content="Dear[[name]],<br>"+"please click the link below to verfiy your registration: <br>"
		             +"<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"+"Thank you,"+"prashant";
		
		try {
			
			MimeMessage message=mailSender.createMimeMessage();
			MimeMessageHelper helper=new MimeMessageHelper(message);
			
			helper.setFrom(from,"prashant");
			helper.setTo(to);
			helper.setSubject(subject);
			
			content=content.replace("[[name]]", user.getName());
			String siteUrl=url+"/verify?code="+user.getVerificationCode();
			
			content=content.replace("[[URL]]", siteUrl);
			
			helper.setText(content, true);
			
			mailSender.send(message);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	@Override
	public boolean verifyAccount(String veroficationCode) {
		// TODO Auto-generated method stub
		
		User user=userRepo.findByVerificationCode(veroficationCode);
		if(user==null)
		{
			return false;
		}else {
			user.setEnable(true);
			 user.setVerificationCode(null);
			 userRepo.save(user); 
			return true;
		}
		
	}

	@Override
	public void removeSessionMessage() {
		// TODO Auto-generated method stub
		
    HttpSession	 session=((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest().getSession();
    session.removeAttribute("msg");
		
	}

	@Override
	public void increaseFailedAttempt(User user) {
		// TODO Auto-generated method stub
		
		int attempt=user.getFailedAttempt()+1;
		userRepo.updateFailedAttempt(attempt, user.getEmail());
		
	}

	//private static final long lock_duration_time=1*60*60*1000;//1hour
	private static final long lock_duration_time=10000;//1hour
	public static final long ATTEMPT_TIME=3;
	@Override
	public void resetAttempt(String email) {
		// TODO Auto-generated method stub
		userRepo.updateFailedAttempt(0, email);
		
	}

	@Override
	public void lock(User user) {
		// TODO Auto-generated method stub
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepo.save(user);
		
	}

	@Override
	public boolean unlockAccountTimeExpired(User user) {
		// TODO Auto-generated method stub
		long lockTimeInMills=user.getLockTime().getTime();
		long currentTimeMillis=System.currentTimeMillis();
		
		if(lockTimeInMills+lock_duration_time <currentTimeMillis)
		{
			user.setAccountNonLocked(true);
			user.setLockTime(null);
			user.setFailedAttempt(0);
			userRepo.save(user);
			return true;
		}
		return false;
	}

	

	


}
