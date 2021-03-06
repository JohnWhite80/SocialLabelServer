package com.github.sociallabel.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

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
	
	@Column(columnDefinition="BIGINT default '0'")
	private String status = "0";

	@ManyToOne(optional = false)
	@JoinColumn(name = "USER_ID", nullable = false)
	@JsonIgnore
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "TAG_ID", nullable = false)
	@JsonIgnore
	private Tag tag;
	
	@OneToMany(targetEntity = com.github.sociallabel.entity.UserTagSubject.class, cascade = CascadeType.REMOVE, mappedBy = "userTag")
	@JsonIgnore
	private Set<UserTagSubject> userTagSubjects = new HashSet<UserTagSubject>();
	
	
	@OneToMany(targetEntity = com.github.sociallabel.entity.UserRelation.class, cascade = CascadeType.REMOVE, mappedBy = "targetUserTag")
	@JsonIgnore
	private Set<UserRelation> followers = new HashSet<UserRelation>(); 

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSubject() {
		if(StringUtils.isEmpty(subject)){
			return tag.getName();
		} else {
			return subject;	
		}				
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

	public Set<UserTagSubject> getUserTagSubjects() {
		return userTagSubjects;
	}

	public void setUserTagSubjects(Set<UserTagSubject> userTags) {
		this.userTagSubjects = userTags;
	}

	public Set<UserRelation> getFollowers() {
		return followers;
	}

	public void setFollowers(Set<UserRelation> followers) {
		this.followers = followers;
	}
}
