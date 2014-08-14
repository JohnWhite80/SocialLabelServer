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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "T_USER")
public class User {

	@Id
	@GenericGenerator(name="idGenerator", strategy="uuid")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "idGenerator")
	private String id;

	@Column(length = 128, nullable = false, unique = true)
	private String email;

	@Column(length = 128, nullable = false)
	private String username;

	@Column(length = 256, nullable = false)
	private String password;

	@Column(length = 256)
	private String picture = " ";

	@Column(length = 256)
	private String sex = " ";

	@Column(length = 256)
	private String birthday = " ";

	@Column(length = 256)
	private String city = " ";

	@OneToMany(targetEntity = com.github.sociallabel.entity.UserTag.class, cascade = CascadeType.REMOVE, mappedBy = "user")
	private Set<UserTag> userTags = new HashSet<UserTag>();
	
	@OneToMany(targetEntity = com.github.sociallabel.entity.UserRelation.class, cascade = CascadeType.REMOVE, mappedBy = "sourceUser")
	@JsonIgnore
	private Set<UserRelation> following = new HashSet<UserRelation>(); 
	
	@OneToMany(targetEntity = com.github.sociallabel.entity.UserRelation.class, cascade = CascadeType.REMOVE, mappedBy = "targetUser")
	@JsonIgnore
	private Set<UserRelation> followers = new HashSet<UserRelation>(); 

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Set<UserTag> getUserTags() {
		return userTags;
	}

	public void setUserTags(Set<UserTag> userTags) {
		this.userTags = userTags;
	}

	public Set<UserRelation> getFollowing() {
		return following;
	}

	public void setFollowing(Set<UserRelation> following) {
		this.following = following;
	}

	public Set<UserRelation> getFollowers() {
		return followers;
	}

	public void setFollowers(Set<UserRelation> followers) {
		this.followers = followers;
	}


}
