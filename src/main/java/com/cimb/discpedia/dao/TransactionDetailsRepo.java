package com.cimb.discpedia.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cimb.discpedia.entity.TransactionDetails;

public interface TransactionDetailsRepo extends JpaRepository<TransactionDetails, Integer>{
	@Query(value = "SELECT * FROM transaction_details where transaction_id = ?1",nativeQuery = true)
	public Iterable<TransactionDetails> findByTransactionId(int transactionId);
}
