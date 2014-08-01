package com.github.sociallabel.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	public void updateProfile(String userId, String filename, MultipartFile file)
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
		user.setPicture(f.getAbsolutePath());
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

	@Transactional
	public List<UserTag> recommend(String email) {
		List<User> t = userRepository.findByEmail(email);
		Set<UserTag> tag = t.get(0).getUserTags();
		List<UserTag> rtback = new ArrayList<UserTag>();
		Iterator<UserTag> it = tag.iterator();
		while (it.hasNext()) {
			UserTag rt = it.next();
			// String rtname=rt.getName();
			rtback.add(rt);
		}
		return rtback;
	}
}
