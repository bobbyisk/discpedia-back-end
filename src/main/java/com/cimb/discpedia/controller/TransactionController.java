package com.cimb.discpedia.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import com.cimb.discpedia.dao.TransactionRepo;
import com.cimb.discpedia.dao.UserRepo;
import com.cimb.discpedia.entity.Transaction;
import com.cimb.discpedia.entity.User;

@RestController
@RequestMapping("/transaction")
@CrossOrigin
public class TransactionController {
	private String uploadPath = System.getProperty("user.dir") + "/src/main/resources/static/images/";
	
	@Autowired
	private UserRepo userRepo;

    @Autowired
    private TransactionRepo transactionRepo;
    
    @PostMapping("/{userId}")
    public Transaction inputTransaction(@PathVariable int userId, @RequestBody Transaction transaction) {
    	User findUser = userRepo.findById(userId).get();
    	transaction.setUser(findUser);
    	
    	return transactionRepo.save(transaction);
    }
    
    @GetMapping("/{userId}")
    public Iterable<Transaction> getTransaction(@PathVariable int userId){
    	User findUser = userRepo.findById(userId).get();
    	return transactionRepo.findByUserId(userId);
    }
    
    @GetMapping()
    public Iterable<Transaction> getAllTransaction(){
    	return transactionRepo.findAll();
    }
    
    @PutMapping("/buktiPembayaran/{transactionId}")
    public Transaction uploadBuktiPembayaran(@PathVariable int transactionId, @RequestParam("file") MultipartFile file) {
    	Transaction findTransaction = transactionRepo.findById(transactionId).get();
    	Date date = new Date();
    	
    	String fileDownloadUri = "No image";
    	
    	if (file.toString()!="Optional.empty") {
			String fileExtension = file.getContentType().split("/")[1];
			String fileName = "TRF-"+ date.getTime()+ "." + fileExtension;
			
			Path path = Paths.get(StringUtils.cleanPath(uploadPath) + fileName);
			
			try {
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}

			fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/transaction/download/")
					.path(fileName).toUriString();
		}
    	
    	findTransaction.setBuktiTrf(fileDownloadUri);
    	
    	return transactionRepo.save(findTransaction);
    }
    
    @GetMapping("/download/{fileName:.+}")
	public ResponseEntity<Object> downloadFile(@PathVariable String fileName){
		Path path = Paths.get(uploadPath, fileName);
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
    
    @PatchMapping("/{transactionId}/status")
    public Transaction transactionStatus(@PathVariable int transactionId, @RequestBody Transaction transaction) {
    	Transaction findTransaction = transactionRepo.findById(transactionId).get();
    	findTransaction.setStatus(transaction.getStatus());
    	findTransaction.setBuyAccDate(transaction.getBuyAccDate());
    	findTransaction.setAlasan(transaction.getAlasan());
    	
    	return transactionRepo.save(findTransaction);

    }
}
