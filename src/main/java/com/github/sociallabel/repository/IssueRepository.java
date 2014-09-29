package com.github.sociallabel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.github.sociallabel.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, String>, JpaSpecificationExecutor<Issue> {
	
	List<Issue> findByTitle(String title);
	
}

