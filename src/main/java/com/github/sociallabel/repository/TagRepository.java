package com.github.sociallabel.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.sociallabel.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, String>, JpaSpecificationExecutor<Tag> {
	
	List<Tag> findByName(String name);

	@Query(" from Tag t where t.name like %?1% ")
	List<Tag> findByTagContaining(String name);
	
	
	List<Tag> findByNameLikeOrderByNameDesc(String name, Pageable pageable);
	
}
//	@SuppressWarnings("rawtypes")
//	List<UserTag> findByUsers(Set users);

