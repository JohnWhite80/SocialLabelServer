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

@Entity
@Table(name="T_CLIENT")
public class Client {

	@Id
	@GenericGenerator(name="idGenerator", strategy="uuid")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "idGenerator")
	private String id;
	
	@Column(length = 128, nullable = false, unique = true)
	private String version;
	
	@Column(length = 128, nullable = false, unique = true)
	private String path;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "PARENT_ID", nullable = true)
	private Client parent;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Client getParent() {
		return parent;
	}

	public void setParent(Client parent) {
		this.parent = parent;
	}		

}
