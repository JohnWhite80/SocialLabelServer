package com.github.sociallabel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.sociallabel.entity.User;


public interface UserRepository extends JpaRepository<User, String>{
	
	List<User> findByEmail(String email);
	
	List<User> findByUsername(String username);
	
	@Query(" from User t inner join t.following as ur where ur.sourceUser.id = ?1 ")
	List<User> findBySourceId(String userId);

	@Query(" from User t inner join t.following as ur where ur.targetUser.id = ?1 ")
	List<User> findByTagetId(String userId);

//	@SuppressWarnings("rawtypes")
//	List<User> findByUserTags(Set UserTags);
//	

	
}
