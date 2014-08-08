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
@Table(name = "T_USER_TAG")
public class UserTag {
	
	@Id
	@GenericGenerator(name="idGenerator", strategy="uuid")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "idGenerator")
	private String id;
	
	@Column(length = 128, nullable = true)
	private String subject;
	
	@Column(columnDefinition="BIGINT default '0'")
	private long peopleNumbers = 0;

	@ManyToOne(optional = false)
	@JoinColumn(name = "USER_ID", nullable = false)
	@JsonIgnore
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "TAG_ID", nullable = false)
	@JsonIgnore
	private Tag tag;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getPeopleNumbers() {
		return peopleNumbers;
	}

	public void setPeopleNumbers(long peopleNumbers) {
		this.peopleNumbers = peopleNumbers;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
	public String getImage(){
		return user.getPicture();
	}

}
