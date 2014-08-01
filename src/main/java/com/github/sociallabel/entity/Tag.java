package com.github.sociallabel.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="T_TAG")
public class Tag {
	
	@Id
	@GenericGenerator(name="idGenerator", strategy="uuid")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "idGenerator")
	private String id;
	
	@Column(length = 128, nullable = false, unique = true)
	private String name;
	
	@OneToMany(targetEntity = com.github.sociallabel.entity.UserTag.class, cascade = CascadeType.REMOVE, mappedBy = "tag")
	private Set<UserTag> userTags = new HashSet<UserTag>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<UserTag> getUserTags() {
		return userTags;
	}

	public void setUserTags(Set<UserTag> userTags) {
		this.userTags = userTags;
	}		
	
	
}
