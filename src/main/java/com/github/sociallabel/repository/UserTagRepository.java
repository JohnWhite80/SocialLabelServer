package com.github.sociallabel.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sociallabel.entity.UserTag;


public interface UserTagRepository extends JpaRepository<UserTag, String>{

	
}
