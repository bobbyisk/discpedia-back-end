package com.cimb.discpedia.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cimb.discpedia.dao.GenreRepo;
import com.cimb.discpedia.dao.ProductRepo;
import com.cimb.discpedia.dao.UserRepo;
import com.cimb.discpedia.entity.Genre;
import com.cimb.discpedia.entity.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/product")
@CrossOrigin
public class ProductController {
	private String uploadPath = System.getProperty("user.dir") + "/src/main/resources/static/images/";
	
	@Autowired
	private ProductRepo productRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private GenreRepo genreRepo;
	
//	@PostMapping
//	public Product addProduct(@RequestBody Product product) {
//		return productRepo.save(product);
//	}
	
	@PostMapping
	public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("productData") String productString) throws JsonMappingException, JsonProcessingException {
		Date date = new Date();
	
		Product product = new ObjectMapper().readValue(productString, Product.class);
		
		// Register / POST user ke database, beserta dengan link ke profile Picture
		
		String fileExtension = file.getContentType().split("/")[1];
		String newFileName = "PROD-" + date.getTime() + "." + fileExtension;
		
		// Get file's original name || can generate our own
		String fileName = StringUtils.cleanPath(newFileName);
		
		// Create path to upload destination + new file name
		Path path = Paths.get(StringUtils.cleanPath(uploadPath) + fileName);

		try {
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/product/download/")
				.path(fileName).toUriString();
		
		product.setImg(fileDownloadUri);
		
		productRepo.save(product);
		
		// http://localhost:8080/documents/download/PROD-123456.jpg
		
		return fileDownloadUri;
	}
	
	@GetMapping("/download/{fileName:.+}") // halo.jpg menandakan parameter fileName ada extensionnya
	public ResponseEntity<Object> downloadFile(@PathVariable String fileName){
		Path path = Paths.get(uploadPath + fileName);
		Resource resource = null;
		
		try {
			resource = new UrlResource(path.toUri());
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
		
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}	
	
	@PutMapping("/{productId}/stockGudang/{stock}/{stockLama}")
	public String updateProduct(@RequestParam("file") MultipartFile file, @RequestParam("productData") String productString, @PathVariable int productId, @PathVariable int stock, @PathVariable int stockLama) throws JsonMappingException, JsonProcessingException {
		Date date = new Date();
		Product findProduct = productRepo.findById(productId).get();
		findProduct = new ObjectMapper().readValue(productString, Product.class);
		String fileDownloadUri = findProduct.getImg();
		
		if(file.toString() != "Optional.empty") {
			String fileExtension = file.getContentType().split("/")[1];
			String fileName = "PROD-" + date.getTime() + "." + fileExtension;
			
			Path path = Paths.get(StringUtils.cleanPath(uploadPath)  + fileName);

			try {
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/product/download/")
					.path(fileName).toUriString();
		}
		
		findProduct.setImg(fileDownloadUri);
		findProduct.setStock_gudang(findProduct.getStock_gudang() - (stock - stockLama));
		productRepo.save(findProduct);
		
		return fileDownloadUri;
	}
	
	@GetMapping
	public Page<Product> getProducts(Pageable pageable) {
		return productRepo.findAll(pageable);
	}
	
	@GetMapping("/all")
	public Iterable<Product> getAllProducts(Pageable pageable) {
		return productRepo.findAll(pageable);
	}
	
	@GetMapping("/{id}")
	public Product getProductById(@PathVariable int id) {
		return productRepo.findById(id).get();
	}
	
//	@PutMapping("/{productId}")
//	public Product updateProduct(@PathVariable int productId, @RequestBody Product product) {
//		Product findProduct = productRepo.findById(productId).get();
//		product.setId(productId);
//		return productRepo.save(product);
//	}
	
	@DeleteMapping("/{productId}")
	public void deleteProductById(@PathVariable int productId) {
		Product findProduct = productRepo.findById(productId).get();
		
		findProduct.getGenre().forEach(genre -> {
			List<Product> genreProduct = genre.getProduct();
			genreProduct.remove(findProduct);
			genreRepo.save(genre);
		});
		
		findProduct.setGenre(null);
		productRepo.deleteById(productId);
	}
	
	@PostMapping("/{productId}/genre/{genreId}")
	public Product addGenreToProduct(@PathVariable int productId, @PathVariable int genreId) {
		Product findProduct = productRepo.findById(productId).get();
		Genre findGenre = genreRepo.findById(genreId).get();
		
		findProduct.getGenre().add(findGenre);
		return productRepo.save(findProduct);
	}
	
	@PatchMapping("edit/{productId}")
	public Product editStockAndSold(@PathVariable int productId, @RequestBody Product product) {
		Product findProduct = productRepo.findById(productId).get();
		findProduct.setStock(findProduct.getStock() - product.getStock());
		findProduct.setSold(findProduct.getSold() + product.getSold());
		
		return productRepo.save(findProduct);
		
	}
	
	@GetMapping("/{minPrice}/{maxPrice}/{orderBy}/{sort}")
	public Page<Product> filterAndSort(@PathVariable double minPrice, @PathVariable double maxPrice, @PathVariable String orderBy, @PathVariable String sort, @RequestParam String title, Pageable pageable){
		if(maxPrice == 0) maxPrice = 99999999;
		if(orderBy.equals("title") && sort.equals("ASC")) {
			return productRepo.getProducts(minPrice, maxPrice, title, pageable);
		} else if(orderBy.equals("price") && sort.equals("ASC")) {
			return productRepo.getProductsSortPriceAsc(minPrice, maxPrice, title, pageable);
		} else if(orderBy.equals("price") && sort.equals("DESC")) {
			return productRepo.getProductsSortPriceDesc(minPrice, maxPrice, title, pageable);
		} else {
			return productRepo.getProducts(minPrice, maxPrice, title, pageable);
		}
	}
	
	@GetMapping("/withGenre/{minPrice}/{maxPrice}/{orderBy}/{sort}")
	public Page<Product> filterAndSortWithGenre(@PathVariable double minPrice, @PathVariable double maxPrice, @PathVariable String orderBy, @PathVariable String sort, @RequestParam String title, @RequestParam String genre, Pageable pageable){
		if(maxPrice == 0) maxPrice = 99999999;
		if(orderBy.equals("title") && sort.equals("ASC")) {
			return productRepo.getProductsWithGenre(genre, minPrice, maxPrice, title, pageable);
		} else if(orderBy.equals("price") && sort.equals("ASC")) {
			return productRepo.getProductsWithGenrePriceAsc(genre, minPrice, maxPrice, title, pageable);
		} else if(orderBy.equals("price") && sort.equals("DESC")) {
			return productRepo.getProductsWithGenrePriceDesc(genre, minPrice, maxPrice, title, pageable);
		} else {
			return productRepo.getProductsWithGenre(genre, minPrice, maxPrice, title, pageable);
		}
	}
	
	
}
