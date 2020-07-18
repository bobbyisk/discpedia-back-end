package com.cimb.discpedia.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.cimb.discpedia.entity.Product;

public interface ProductRepo extends JpaRepository<Product, Integer>, PagingAndSortingRepository<Product, Integer> {
	@Query(value = "SELECT * FROM product WHERE price >=?1 and price <= ?2 AND title LIKE %?3% ORDER BY title ASC", nativeQuery = true)
	public Page<Product> getProducts(double minPrice, double maxPrice, String title, Pageable pageable);
	
	@Query(value = "SELECT * FROM product WHERE price >=?1 and price <= ?2 AND title LIKE %?3% ORDER BY price ASC", nativeQuery = true)
	public Page<Product> getProductsSortPriceAsc(double minPrice, double maxPrice, String title, Pageable pageable);
	
	@Query(value = "SELECT * FROM product WHERE price >=?1 and price <= ?2 AND title LIKE %?3% ORDER BY price DESC", nativeQuery = true)
	public Page<Product> getProductsSortPriceDesc(double minPrice, double maxPrice, String title, Pageable pageable);
	
	@Query(value = "SELECT * FROM prodgen pg JOIN product p ON p.id = pg.product_id JOIN genre g ON g.id = pg.genre_id WHERE genre_name = ?1 AND price >= ?2 AND price <= ?3 AND title LIKE %?4% ORDER BY title ASC", nativeQuery = true)
	public Page<Product> getProductsWithGenre(String genre, double minPrice, double maxPrice, String title, Pageable pageable);

	@Query(value = "SELECT * FROM prodgen pg JOIN product p ON p.id = pg.product_id JOIN genre g ON g.id = pg.genre_id WHERE genre_name = ?1 AND price >= ?2 AND price <= ?3 AND title LIKE %?4% ORDER BY price ASC", nativeQuery = true)
	public Page<Product> getProductsWithGenrePriceAsc(String genre, double minPrice, double maxPrice, String title, Pageable pageable);
	
	@Query(value = "SELECT * FROM prodgen pg JOIN product p ON p.id = pg.product_id JOIN genre g ON g.id = pg.genre_id WHERE genre_name = ?1 AND price >= ?2 AND price <= ?3 AND title LIKE %?4% ORDER BY price DESC", nativeQuery = true)
	public Page<Product> getProductsWithGenrePriceDesc(String genre, double minPrice, double maxPrice, String title, Pageable pageable);

	
	
}
