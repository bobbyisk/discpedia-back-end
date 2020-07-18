package com.cimb.discpedia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cimb.discpedia.dao.GenreRepo;
import com.cimb.discpedia.dao.ProductRepo;
import com.cimb.discpedia.entity.Genre;

@RestController
@RequestMapping("/genre")
@CrossOrigin
public class GenreController {
	@Autowired
	private ProductRepo productRepo;
	
	@Autowired 
	private GenreRepo genreRepo;
	
	@PostMapping
	public Genre addGenre(@RequestBody Genre genre) {
		return genreRepo.save(genre);
	}
	
	@GetMapping
	public Iterable<Genre> getGenre() {
		return genreRepo.findAll();
	}
	
	@DeleteMapping("/{genreId}")
	public void deleteGenre(@PathVariable int genreId) {
		Genre findGenre = genreRepo.findById(genreId).get();
		
		findGenre.getProduct().forEach(product -> {
			List<Genre> productGenre = product.getGenre();
			productGenre.remove(findGenre);
			productRepo.save(product);
		});
		
		findGenre.setGenreName(null);
		genreRepo.deleteById(genreId);
	}
}
