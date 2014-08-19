package com.github.sociallabel.entity;

import javax.persistence.Column;
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
@Table(name = "T_USER_TAG_SUBJECT")
public class UserTagSubject {

	@Id
	@GenericGenerator(name="idGenerator", strategy="uuid")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "idGenerator")
	private String id;
	
	@Column(length = 128, nullable = true)
	private String subject;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "USER_TAG_ID", nullable = false)
	@JsonIgnore
	private UserTag userTag;
	
	@Column(nullable = true)
	private long createDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public UserTag getUserTag() {
		return userTag;
	}

	public void setUserTag(UserTag userTag) {
		this.userTag = userTag;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}		
	
}
