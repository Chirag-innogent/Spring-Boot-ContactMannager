package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepositary extends JpaRepository<Contact, Integer> {
	//pagination...
	
	
	
	@Query(" from Contact as c where c.user.id = :userId ")
	//current page page
	//contact per page
	public Page<Contact> findByUser(@Param ("userId") int userId,Pageable pageable);

}
