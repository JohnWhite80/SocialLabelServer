package com.github.sociallabel.entity;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel(Tag.class)
public class Tag_ {

	public static volatile SingularAttribute<Tag, String> id;
	public static volatile SingularAttribute<Tag, String> name;
	public static volatile SetAttribute<Tag, UserTag> userTags;
}
