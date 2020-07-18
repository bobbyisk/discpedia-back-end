package com.cimb.discpedia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cimb.discpedia.dao.ProductRepo;
import com.cimb.discpedia.dao.TransactionDetailsRepo;
import com.cimb.discpedia.dao.TransactionRepo;
import com.cimb.discpedia.dao.UserRepo;
import com.cimb.discpedia.entity.Product;
import com.cimb.discpedia.entity.Transaction;
import com.cimb.discpedia.entity.TransactionDetails;
import com.cimb.discpedia.entity.User;
import com.cimb.discpedia.util.EmailUtil;

@RestController
@RequestMapping("/transactionDetails")
@CrossOrigin
public class TransactionDetailsController {
	String message = "";
	
	@Autowired
    private TransactionDetailsRepo transactionDetailsRepo;
	
	@Autowired
    private TransactionRepo transactionRepo;
	
	@Autowired
	private ProductRepo productRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private EmailUtil emailUtil;
	
	@PostMapping("/{transactionId}/{productId}")
	public TransactionDetails inputTransactionDetails(@PathVariable int transactionId, @PathVariable int productId, @RequestBody TransactionDetails transactionDetails) {
		Transaction findTransaction = transactionRepo.findById(transactionId).get();
		Product findProduct = productRepo.findById(productId).get();
		
		transactionDetails.setTransaction(findTransaction);
		transactionDetails.setProduct(findProduct);
		
		return transactionDetailsRepo.save(transactionDetails);
	}
	
	@GetMapping
    public Iterable<TransactionDetails> getTransactionDetails(){
        return transactionDetailsRepo.findAll();
    }
	
	@GetMapping("/{transactionId}")
    public Iterable<TransactionDetails> getTransactionDetailsByTransactionId(@PathVariable int transactionId){
    	Transaction findTransaction = transactionRepo.findById(transactionId).get();
    	return transactionDetailsRepo.findByTransactionId(transactionId);
    }
	
	@GetMapping("/invoice/{transactionId}")
	public void SendInvoice(@PathVariable int transactionId) {
		Transaction findTransaction = transactionRepo.findById(transactionId).get();
		int idTransaction = findTransaction.getId();
		String userTransaction = findTransaction.getUser().getUsername();
		String dateTransaction = findTransaction.getBuyDate();
		String statusTransaction = findTransaction.getStatus();
		int priceTransaction = findTransaction.getTotalPrice();
		String userEmail = findTransaction.getUser().getEmail();
		
		message += "<h1>Discpedia</h1>\n";
		message += "<b>Invoice</b>\r\n";
		message += "<p>Transaction ID: " + idTransaction + "</p>\r\n";
		message += "<p>Username: " + userTransaction + "<p>\r\n";
		message += "<p>Date: " + dateTransaction + "</p>\r\n";
		message += "<p>Status: " + statusTransaction + "</p>\r\n";
		
		message += "<br />";
		
		message += "<table>\r\n"+
				   "<thead>\r\n"+
		   		   "<tr>\r\n"+
				   "<th>Product</th>\r\n"+
		   		   "<th>Price</th>\r\n"+
				   "<th>Qty</th>\r\n"+
		   		   "<th>Total Price</th>\r\n"+
		   		   "</tr>\r\n"+
				   "</thead>\r\n"+
		   		   "<tbody>\r\n";
		
		findTransaction.getTransactionDetails().forEach(val -> {
			message += "<tr>\r\n"+
					   "<td>" + val.getProduct().getTitle() + "</td>\r\n"+
					   "<td>Rp" + val.getProduct().getPrice() + "</td>\r\n"+
					   "<td>" + val.getQty() + "</td>\r\n"+
					   "<td>Rp" + val.getTotalPrice() + "</td>\r\n"+
					   "</tr>\r\n";
		});
		message += "</tbody>\r\n</table>";
		
		emailUtil.sendEmail(userEmail, "Invoice", message);			
	}
}
