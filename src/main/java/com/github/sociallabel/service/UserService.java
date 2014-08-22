package com.github.sociallabel.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.sociallabel.APIException;
import com.github.sociallabel.entity.Tag;
import com.github.sociallabel.entity.User;
import com.github.sociallabel.entity.UserRelation;
import com.github.sociallabel.entity.UserTag;
import com.github.sociallabel.entity.UserTagSubject;
import com.github.sociallabel.repository.TagRepository;
import com.github.sociallabel.repository.UserRelationRepository;
import com.github.sociallabel.repository.UserRepository;
import com.github.sociallabel.repository.UserTagRepository;
import com.github.sociallabel.repository.UserTagSubjectRepository;
import com.github.sociallabel.util.SecurityUtil;

@Service("userService")
public class UserService {

	private @Value("${app.image.store.path}")
	String path;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private UserTagRepository userTagRepository;
	@Autowired
	private UserRelationRepository userRelationRepository;
	@Autowired
	private UserTagSubjectRepository userTagSubjectRepository;

	@Transactional
	public User addUser(User u) {
		if (u.getEmail() == null || u.getUsername() == null
				|| u.getPassword() == null) {
			throw new APIException(400, "bad request");
		}
		List<User> users = userRepository.findByEmail(u.getEmail());
		if (users.size() > 0) {
			throw new APIException(400, "duplicated login name");
		}
		// Encrypt password for security
		u.setPassword(SecurityUtil.encrypt(u.getPassword()));
		return userRepository.saveAndFlush(u);
	}

	@Transactional
	public void updateUser(User u) {
		User user = (User) userRepository.findByEmail(u.getEmail());
		user.setSex(u.getSex());
		user.setBirthday(u.getBirthday());
		user.setCity(u.getCity());
		userRepository.saveAndFlush(user);
	}

	@Transactional
	public void updateProfile(String userId, String birthday, String city, String sex, String filename, MultipartFile file)
			throws Exception {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		String ext = filename.substring(filename.indexOf(".") + 1,
				filename.length());
		File f = new File(path + File.separator + System.currentTimeMillis()
				+ "." + ext);
		file.transferTo(f);		
		user.setPicture(f.getName());
		user.setBirthday(birthday);
		user.setCity(city);
		user.setSex(sex);
		userRepository.saveAndFlush(user);
	}

	public User validate(String email, String password) {
		if (email != null && password != null) {
			List<User> users = userRepository.findByEmail(email);
			if (users != null && users.size() > 0) {
				User user = users.get(0);
				if(SecurityUtil.encrypt(password).equals(user.getPassword())){
					return user;
				}
			}
		}
		throw new APIException(400, "invalid user");
	}
	
	public User validateByUserId(String userId, String password) {
		if (userId != null && password != null) {
			User user = userRepository.findOne(userId);
			if (user != null) {
				if(SecurityUtil.encrypt(password).equals(user.getPassword())){
					return user;
				}
			}
		}
		throw new APIException(400, "invalid user");
	}

	@Transactional
	public void addtag(String userId, List<Tag> tags) {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		if (tags != null) {
			for (Tag t : tags) {
				List<Tag> tagList = tagRepository.findByName(t.getName());
				Tag tag = null;
				if (tagList.isEmpty()) {
					tag = tagRepository.save(t);
				} else {
					tag = tagList.get(0);
				}
				UserTag ut = new UserTag();
				ut.setTag(tag);
				ut.setUser(user);
				userTagRepository.save(ut);
			}
		}
	}
	
	public List<Map> recommend(String userId) {
		List<Map> result = new ArrayList<Map>();				
		User t = userRepository.findOne(userId);
		Set<UserTag> tags = t.getUserTags();
		Iterator<UserTag> it = tags.iterator();
		QPageRequest pageTag = new QPageRequest(0, 5);
		QPageRequest pageUserTag = new QPageRequest(0, 2);
		while (it.hasNext()) {
			UserTag rt = it.next();
			String name = rt.getTag().getName();
			List<Tag> names = tagRepository.findByNameLikeOrderByNameDesc(name, pageTag);
			for(Tag tag: names) {
				List<UserTag> userTags = userTagRepository.findByTagId(tag.getId(), pageUserTag);
				if(!userTags.isEmpty()) {
					Map map  = new HashMap();
					map.put("name", tag.getName());
					List uts = new ArrayList();
					for(UserTag ut : userTags){
						Map m = new HashMap();
						m.put("id", ut.getId());
						m.put("name", ut.getSubject());
						m.put("userId", ut.getUser().getId());
						m.put("nickName", ut.getUser().getUsername());
						m.put("image", ut.getUser().getPicture());
						m.put("status", ut.getStatus());
						uts.add(m);
					}
					map.put("userTags", uts);
					result.add(map);
				}				
			}			
		}
		return result;
	}
	
	public List<Map> recommendByTagName(String userId, String tagname) {
		List<Map> result = new ArrayList<Map>();				
		QPageRequest pageTag = new QPageRequest(0, 5);
		QPageRequest pageUserTag = new QPageRequest(0, 2);
		List<Tag> names = tagRepository.findByNameLikeOrderByNameDesc("%" + tagname + "%", pageTag);
		for(Tag tag: names) {
			List<UserTag> userTags = userTagRepository.findByTagId(tag.getId(), pageUserTag);
			if(!userTags.isEmpty()) {
				Map map = new HashMap();
				map.put("name", tag.getName());
				List uts = new ArrayList();
				for(UserTag ut : userTags){
					Map m = new HashMap();
					m.put("id", ut.getId());
					m.put("name", ut.getSubject());
					m.put("userId", ut.getUser().getId());
					m.put("nickName", ut.getUser().getUsername());
					m.put("image", ut.getUser().getPicture());
					m.put("status", ut.getStatus());
					uts.add(m);
				}
				map.put("userTags", uts);
				result.add(map);	
			}				
		}
		return result;
	}

	public File getImage(String filename) {
		File f = new File(path + File.separator + filename);
		if(!f.exists()) {
			throw new APIException(404, "file not found");
		}
		return f;
	}

	public UserTag findUserTagById(String id) {
		UserTag result = userTagRepository.findOne(id);
		if( result != null) {
			return result;
		}
		throw new APIException(400, "invalid userTag");
	}
	
	@Transactional
	public UserTag setUserTagStatus(String id, String userId, String status, String subject) {
		UserTag result = userTagRepository.findOne(id);
		if( result != null) {
			if(!result.getUser().getId().equals(userId)) {
				throw new APIException(400, "permission denied");		
			}
			if(subject != null && !"".equals(subject)) {
				UserTagSubject entity = new UserTagSubject();
				entity.setSubject(subject);
				entity.setUserTag(result);
				entity.setCreateDate(System.currentTimeMillis());
				userTagSubjectRepository.save(entity);
				result.setSubject(subject);
			}
			result.setStatus(status);
			userTagRepository.save(result);
			return result;
		}
		throw new APIException(400, "invalid userTag");
	}

	public Map getProfile(String userId) {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		Map result = new HashMap();
		result.put("id", user.getId());
		result.put("nickName", user.getUsername());
		result.put("image", user.getPicture());
		result.put("sex", user.getSex());
		result.put("birthday", user.getBirthday());
		result.put("city", user.getCity());
		Set<UserTag> userTags = user.getUserTags();
		List uts = new ArrayList();
		for(UserTag ut : userTags){
			Map m = new HashMap();
			m.put("id", ut.getId());			
			m.put("name", ut.getSubject());
			m.put("tagName", ut.getTag().getName());
			m.put("userId", ut.getUser().getId());
			m.put("nickName", ut.getUser().getUsername());
			m.put("image", ut.getUser().getPicture());
			m.put("status", ut.getStatus());
			uts.add(m);
		}
		result.put("userTags", uts);
		return result;
	}

	@Transactional
	public void firendship(String userId, String targetId, String action) {
		User user = userRepository.findOne(userId);
		UserTag target = userTagRepository.findOne(targetId);
		if(user == null || target == null) {
			throw new APIException(400, "user not exist");
		}
		List<UserRelation> relations = userRelationRepository.findBySourceIdAndTargetId(userId, targetId);
		if("create".equals(action)) {
			if(relations.isEmpty()) {
				UserRelation ur = new UserRelation();
				ur.setSourceUser(user);
				ur.setTargetUser(target.getUser());
				userRelationRepository.save(ur);
			}
		} else if("destroy".equals(action)) {
			if(!relations.isEmpty()) {
				userRelationRepository.delete(relations.get(0));
			}
		} else {
			throw new APIException(400, "missed action type");
		}
	}

	public List<Map<String, String>> firends(String userId) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		List<User> friends = userRepository.findBySourceId(userId);
		for(User u : friends) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("userId", u.getId());
			map.put("nickName", u.getUsername());
			map.put("image", u.getPicture());
			result.add(map);
		}
		return result;
	}

	public List<Map<String, String>> followers(String userId) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		List<User> friends = userRepository.findByTagetId(userId);
		for(User u : friends) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("userId", u.getId());
			map.put("nickName", u.getUsername());
			map.put("image", u.getPicture());
			result.add(map);
		}
		return result;
	}
	
	@Transactional
	public void updateUserTag(String id, String userId, String tagName){
		UserTag result = userTagRepository.findOne(id);
		if( result != null) {
			if(!result.getUser().getId().equals(userId)) {
				throw new APIException(400, "permission denied");		
			}
			List<Tag> tagList = tagRepository.findByName(tagName);
			Tag tag = null;			
			if (tagList.isEmpty()) {
				tag = new Tag();
				tag.setName(tagName);
				tag = tagRepository.save(tag);
			} else {
				tag = tagList.get(0);
			}
			result.setTag(tag);
			userTagRepository.save(result);
			return;
		}
		throw new APIException(400, "invalid userTag");
	}

	@Transactional
	public void countRoom(String roomId) {
		UserTag result = userTagRepository.findOne(roomId);
		if( result != null) {
			result.setPeopleNumbers(result.getPeopleNumbers() + 1);
			userTagRepository.saveAndFlush(result);
			return;
		}
		throw new APIException(400, "invalid userTag");
	}
}
