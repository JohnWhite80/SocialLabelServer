package com.github.sociallabel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.github.sociallabel.entity.Client;

public interface ClientRepository extends JpaRepository<Client, String>, JpaSpecificationExecutor<Client> {
	
	List<Client> findByVersion(String version);
	
	@Query(" from Client c where c.parent is null ")
	List<Client> findCurrentClient();
	
}

