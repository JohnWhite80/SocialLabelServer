package com.github.sociallabel.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Repository;

import com.github.sociallabel.entity.Tag;
import com.github.sociallabel.entity.Tag_;
import com.github.sociallabel.entity.UserTag;

@Repository("tupleRepository")
public class TupleRepository {
	
	@PersistenceContext(unitName="persistenceUnit")
	EntityManager em;

	public List<Tuple> findAllRecommendedUser(Set<String> idsNotIn, QPageRequest pageable) {
		// TODO Auto-generated method stub
		if(idsNotIn == null) idsNotIn = Collections.emptySet();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<Tag> tag = cq.from(Tag.class);
		SetJoin<Tag, UserTag> userTags = tag.join(Tag_.userTags, JoinType.LEFT);
		cq.select(cb.tuple(tag, cb.count(userTags)));		
		
//		Predicate notin = cb.not(tag.get(Tag_.id).in(idsNotIn));
//				
//		cq.where(notin);
		cq.groupBy(tag.get(Tag_.id));
		cq.orderBy(cb.desc(cb.count(userTags)));
		
		TypedQuery<Tuple> query = em.createQuery(cq);
		
		query.setFirstResult(pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());
		
		return query.getResultList();
	}	

}
