package com.github.sociallabel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sociallabel.entity.User;


public interface UserRepository extends JpaRepository<User, String>{
	
	List<User> findByEmail(String email);
	List<User> findByUsername(String username);

//	@SuppressWarnings("rawtypes")
//	List<User> findByUserTags(Set UserTags);
//	

	
}
