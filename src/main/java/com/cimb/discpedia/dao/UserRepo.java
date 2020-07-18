package com.cimb.discpedia.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cimb.discpedia.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {
	public Optional<User> findByUsername(String username);
	public User findByEmail(String email);
	
	@Query(value = "SELECT * FROM User WHERE username = ?1" , nativeQuery = true)
	public Iterable<User> findUsername(String username);
}
