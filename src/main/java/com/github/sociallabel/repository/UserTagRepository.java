package com.github.sociallabel.repository;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.sociallabel.entity.UserTag;


public interface UserTagRepository extends JpaRepository<UserTag, String>{
	
	@Query(" from UserTag t where t.tag.id = ?1")
	List<UserTag> findByTagId(String tagId, Pageable page);
	
	@Query(" from UserTag t where t.tag.id = ?1 and t.status = ?2")
	List<UserTag> findByTagIdAndStatus(String tagId, String status, Pageable page);
	
}
