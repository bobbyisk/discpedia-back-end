package com.cimb.discpedia.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cimb.discpedia.entity.Cart;

public interface CartRepo extends JpaRepository <Cart, Integer> {
	@Query(value = "SELECT * FROM cart WHERE user_id= ?1", nativeQuery = true)
    public Iterable<Cart> findByUserId(int userId);
}
