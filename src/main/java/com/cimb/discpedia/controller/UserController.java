package com.cimb.discpedia.controller;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cimb.discpedia.dao.UserRepo;
import com.cimb.discpedia.entity.Product;
import com.cimb.discpedia.entity.User;
import com.cimb.discpedia.util.EmailUtil;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
	@Autowired
	private UserRepo userRepo;
	
	private PasswordEncoder pwEncoder = new BCryptPasswordEncoder();
	
	@Autowired
	private EmailUtil emailUtil;
	
	@GetMapping
	public Iterable<User> getUser() {
		return userRepo.findAll();
	}
	
	@PostMapping
	public User registerUser(@RequestBody User user) {
		Optional<User> findUser = userRepo.findByUsername(user.getUsername());
		
		if(findUser.toString() != "Optional.empty") {
			throw new RuntimeException("Username already exists!");
		} else {
			String encodedPassword = pwEncoder.encode(user.getPassword());
			String verifyToken = pwEncoder.encode(user.getUsername() + user.getEmail());
			
			user.setPassword(encodedPassword);
			user.setVerified(false);
			user.setVerifyToken(verifyToken);
			
			User savedUser = userRepo.save(user);
			savedUser.setPassword(null);
			
			String linkToVerify = "http://localhost:8000/verify/" + user.getUsername() + "?token=" + verifyToken;
			
			String message = "<h1>Selamat! Registrasi Berhasil</h1>\n";
			message += "Akun dengan username " + user.getUsername() + " telah terdaftar!\n";
			message += "Klik <a href=\"" + linkToVerify + "\">link ini</a> untuk verifikasi email anda.";
			
			emailUtil.sendEmail(user.getEmail(), "Registrasi Akun", message);
			
			return savedUser;
		}
	}
	
	@GetMapping("/login")
	public User getLoginUser(@RequestParam String username, @RequestParam String password) {
		User findUser = userRepo.findByUsername(username).get();
		
		if(pwEncoder.matches(password, findUser.getPassword())) {
			findUser.setPassword(null);
			return findUser;
		} 
		
		return null;
	}
	
	@GetMapping("/username")
	public Iterable<User> getUsername(@RequestParam String username) {
		return userRepo.findUsername(username);
	}
	
	@GetMapping("/{userId}")
	public User getUserById(@PathVariable int userId) {
		return userRepo.findById(userId).get();
	}
	
	@PutMapping("/{userId}")
	public User updateUser(@PathVariable int userId, @RequestBody User user) {
		User findUser = userRepo.findById(userId).get();
		user.setId(userId);
		return userRepo.save(user);
	}
	
	@GetMapping("/keeplogin")
	public User tetapLogin(@RequestParam int id) {
		return userRepo.findById(id).get();
	}
	
	@PostMapping("/forgot")
	@Transactional
	public User forgotPassword(@RequestBody User user) {
		User findUser = userRepo.findByEmail(user.getEmail());
//		if(findUser.toString() != "Optional.empty") {
//			throw new RuntimeException("No Email & Wrong Email");
//		}
		
		String message = "<h1>Reset Password</h1>\n ";
        message +="Click this <a href=\"http://localhost:3000/reset/"+findUser.getId()+"/"+findUser.getVerifyToken()+"\">link</a> to reset your password.";
        emailUtil.sendEmail(user.getEmail(), "Email Confirmation", message);
		
		return findUser;
	}
	
	@GetMapping("/reset/{userId}/{userVerif}")
	public User getUserById(@PathVariable int userId, @PathVariable String userVerif) {
		User findUser = userRepo.findById(userId).get();
		return findUser;
	}
	
	@PutMapping("/reset")
	public User resetPassword(@RequestBody User user) {
		User findUser = userRepo.findById(user.getId()).get();
		String encodedPassword = pwEncoder.encode(user.getPassword());
		
		user.setPassword(encodedPassword);
		userRepo.save(user);
		
		return user;
	}
	
	@PutMapping("/editPassword")
	public User changePassword(@RequestBody User user, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword) {
		User findUser = userRepo.findById(user.getId()).get();
		
		if(pwEncoder.matches(oldPassword, findUser.getPassword())) {
			String encodedPassword = pwEncoder.encode(newPassword);
			
			findUser.setPassword(encodedPassword);
			userRepo.save(findUser);
			return findUser;
		}
		throw new RuntimeException("Old password not same.");
	}
}
