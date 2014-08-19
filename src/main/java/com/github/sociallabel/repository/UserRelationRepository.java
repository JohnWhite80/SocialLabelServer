package com.github.sociallabel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.sociallabel.entity.UserRelation;


public interface UserRelationRepository extends JpaRepository<UserRelation, String>{
	
	@Query(" from UserRelation t where t.sourceUser.id = ?1 and t.targetUser.id=?2 ")
	List<UserRelation> findBySourceIdAndTargetId(String souceId, String targetId);

	@Query(" from UserRelation t where t.sourceUser.id = ?1 ")
	List<UserRelation> findBySourceId(String userId);
}
