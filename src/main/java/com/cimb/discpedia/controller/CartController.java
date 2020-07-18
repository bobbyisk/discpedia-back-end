package com.cimb.discpedia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cimb.discpedia.dao.CartRepo;
import com.cimb.discpedia.dao.ProductRepo;
import com.cimb.discpedia.dao.UserRepo;
import com.cimb.discpedia.entity.Cart;
import com.cimb.discpedia.entity.Product;
import com.cimb.discpedia.entity.User;

@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartController {
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
    private ProductRepo productRepo;

    @Autowired
    private CartRepo cartRepo;
    
    @GetMapping
    public Iterable<Cart> getCart(){
    	return cartRepo.findAll();
    }
    
    @GetMapping("user/{userId}")
    public Iterable<Cart> getUserCart(@PathVariable int userId){
    	return cartRepo.findByUserId(userId);
    }
    
    @PostMapping("/add/{userId}/{productId}")
    public Cart addToCart(@PathVariable int userId, @PathVariable int productId, @RequestBody Cart cart) {
    	User findUser = userRepo.findById(userId).get();
    	Product findProduct = productRepo.findById(productId).get();
    	
    	cart.setUser(findUser);
    	cart.setProduct(findProduct);
    	
    	return cartRepo.save(cart);
    }
    
    @PutMapping("/add/{cartId}")
    public Cart updateCart(@PathVariable int cartId) {
    	Cart findCart = cartRepo.findById(cartId).get();
    	findCart.setQty(findCart.getQty() + 1);
    	
    	return cartRepo.save(findCart);
    }
    
    @DeleteMapping("/delete/{cartId}")
    public void deleteCart(@PathVariable int cartId) {
    	Cart findCart = cartRepo.findById(cartId).get();
    	cartRepo.deleteById(cartId);
    }
}
