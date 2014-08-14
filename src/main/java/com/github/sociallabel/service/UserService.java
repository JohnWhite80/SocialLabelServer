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
import com.github.sociallabel.entity.UserTag;
import com.github.sociallabel.repository.TagRepository;
import com.github.sociallabel.repository.UserRepository;
import com.github.sociallabel.repository.UserTagRepository;
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
		QPageRequest page = new QPageRequest(0, 5);
		while (it.hasNext()) {
			UserTag rt = it.next();
			String name = rt.getTag().getName();
			List<Tag> names = tagRepository.findByNameLikeOrderByNameDesc(name, page);
			for(Tag tag: names) {
				List<UserTag> userTags = userTagRepository.findByTagId(tag.getId(), page);
				if(!userTags.isEmpty()) {
					Map map = new HashMap();
					map.put("name", tag.getName());
					map.put("userTags", userTags);
					result.add(map);	
				}				
			}			
		}
		return result;
	}
	
	public List<Map> recommendByTagName(String userId, String tagname) {
		List<Map> result = new ArrayList<Map>();				
		QPageRequest page = new QPageRequest(0, 5);
		List<Tag> names = tagRepository.findByNameLikeOrderByNameDesc(tagname, page);
		for(Tag tag: names) {
			List<UserTag> userTags = userTagRepository.findByTagId(tag.getId(), page);
			if(!userTags.isEmpty()) {
				Map map = new HashMap();
				map.put("name", tag.getName());
				map.put("userTags", userTags);
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
	public UserTag setUserTagStatus(String id, String userId, String status) {
		UserTag result = userTagRepository.findOne(id);
		if( result != null) {
			if(!result.getUser().getId().equals(userId)) {
				throw new APIException(400, "permission denied");		
			}
			result.setStatus(status);
			userTagRepository.saveAndFlush(result);
			return result;
		}
		throw new APIException(400, "invalid userTag");
	}

	public User getProfile(String userId) {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		return user;
	}
}
