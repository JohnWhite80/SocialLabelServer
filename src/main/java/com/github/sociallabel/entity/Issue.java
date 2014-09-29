package com.github.sociallabel.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="T_ISSUE")
public class Issue {

	@Id
	@GenericGenerator(name="idGenerator", strategy="uuid")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "idGenerator")
	private String id;
	
	@Column(length = 128, nullable = false, unique = true)
	private String title;
	
	@Lob
	@Column(length = 512, nullable = false)
	private String description;
	
	@Column(columnDefinition="BIGINT default '0'", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date createTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
		
}
