package com.github.sociallabel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.sociallabel.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, String> {
	
	List<Tag> findByName(String name);

	 @Query(" from Tag t where t.name like %?1% ")
	List<Tag> findByTagContaining(String nameTag);
}
//	@SuppressWarnings("rawtypes")
//	List<UserTag> findByUsers(Set users);

