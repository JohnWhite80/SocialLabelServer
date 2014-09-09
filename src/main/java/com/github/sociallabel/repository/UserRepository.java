package com.github.sociallabel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.github.sociallabel.entity.User;


public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User>{
	
	List<User> findByEmail(String email);
	
	List<User> findByUsername(String username);
	
//	@Query(" from User t inner join fetch t.following as ur where ur.sourceUser.id = ?1 ")
//	List<User> findBySourceId(String userId);

//	@Query("SELECT DISTINCT t FROM User t WHERE t.userTags IN ( SELECT ug FROM UserTag ug WHERE ug.followers.targetUserTag = ?1 )")
//	List<User> findByTagetId(String userId);

//	@SuppressWarnings("rawtypes")
//	List<User> findByUserTags(Set UserTags);
//	

	
}
