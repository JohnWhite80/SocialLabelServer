package com.github.sociallabel.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "T_USER_RELATION")
public class UserRelation {
	
	@Id
	@GenericGenerator(name="idGenerator", strategy="uuid")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "idGenerator")
	private String id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "SOURCE_USER_ID", nullable = false)
	@JsonIgnore
	private User sourceUser;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "TARGET_USERTAG_ID", nullable = false)
	@JsonIgnore
	private UserTag targetUserTag;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public User getSourceUser() {
		return sourceUser;
	}

	public void setSourceUser(User sourceUser) {
		this.sourceUser = sourceUser;
	}

	public UserTag getTargetUserTag() {
		return targetUserTag;
	}

	public void setTargetUserTag(UserTag targetUserTag) {
		this.targetUserTag = targetUserTag;
	}

}
