package com.github.sociallabel.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.sociallabel.APIException;
import com.github.sociallabel.entity.Client;
import com.github.sociallabel.entity.Issue;
import com.github.sociallabel.entity.Tag;
import com.github.sociallabel.entity.User;
import com.github.sociallabel.entity.UserRelation;
import com.github.sociallabel.entity.UserTag;
import com.github.sociallabel.entity.UserTagSubject;
import com.github.sociallabel.repository.ClientRepository;
import com.github.sociallabel.repository.IssueRepository;
import com.github.sociallabel.repository.TagRepository;
import com.github.sociallabel.repository.UserRelationRepository;
import com.github.sociallabel.repository.UserRepository;
import com.github.sociallabel.repository.UserTagRepository;
import com.github.sociallabel.repository.UserTagSubjectRepository;
import com.github.sociallabel.util.SecurityUtil;

@Service("userService")
public class UserService {

	private @Value("${app.image.store.path}")
	String imgPath;
	private @Value("${app.client.store.path}")
	String clientPath;
	@Autowired
	private IssueRepository issueRepository;
	@Autowired
	private ClientRepository clientRepository;
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
	@Autowired
	private TupleRepository tupleRepository;
	@Autowired
	private JPushService jpushService;
	
	private ConcurrentHashMap<String, Set<String>> recommendedMap = new ConcurrentHashMap<String, Set<String>>();
	

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
	public void updateProfile(String userId, String nickName, String birthday, String city, String sex, String filename, MultipartFile file)
			throws Exception {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		String ext = filename.substring(filename.indexOf(".") + 1,
				filename.length());
		File f = new File(imgPath + File.separator + System.currentTimeMillis()
				+ "." + ext);
		file.transferTo(f);		
		user.setPicture(f.getName());
		if (StringUtils.isNotEmpty(nickName)) {
			user.setUsername(nickName);	
		}
		if (StringUtils.isNotEmpty(birthday)) {
			user.setBirthday(birthday);
		}
		if (StringUtils.isNotEmpty(city)) {
		    user.setCity(city);
		}
		if (StringUtils.isNotEmpty(sex)) {
			user.setSex(sex);	
		}	
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
	
	@Transactional
	public void deleteUserTag(String userId, String tagName) {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		Set<UserTag> userTags = user.getUserTags();
		if(userTags != null) {
			for(UserTag ut: userTags) {
				if(ut.getTag().getName().equals(tagName)) {
					userTagRepository.delete(ut);
				}
			}
		}	
	}	
	
	public List<Map> recommend(String userId, Integer page) {
		List<Map> result = new ArrayList<Map>();			
		QPageRequest pageUserTag = new QPageRequest(0, 2);
		if (page == null || page < 0) {
			User t = userRepository.findOne(userId);
			Set<UserTag> relation = convert(t.getFollowing());
			Set<UserTag> tags = t.getUserTags();
			Iterator<UserTag> it = tags.iterator();
			QPageRequest pageTag = new QPageRequest(0, 5);				
			Set<String> tagIds = new HashSet<String>();  
			while (it.hasNext()) {
				UserTag rt = it.next();
				String name = rt.getTag().getName();
				List<Tag> names = tagRepository.findByNameLikeOrderByNameDesc(
						name, pageTag);
				for (Tag tag : names) {
					List<UserTag> userTags = userTagRepository.findByTagId(
							tag.getId(), pageUserTag);
					if (!userTags.isEmpty()) {
						Map map = new HashMap();
						map.put("name", tag.getName());
						tagIds.add(tag.getId());
						List uts = new ArrayList();
						for (UserTag ut : userTags) {
							Map m = new HashMap();
							m.put("id", ut.getId());
							m.put("name", ut.getSubject());
							m.put("userId", ut.getUser().getId());
							m.put("nickName", ut.getUser().getUsername());
							m.put("image", ut.getUser().getPicture());
							m.put("status", ut.getStatus());
							if (relation.contains(ut)) {
								m.put("followed", "1");
							} else {
								m.put("followed", "0");
							}
							uts.add(m);
						}
						map.put("userTags", uts);
						result.add(map);
					}
				}
			}
			recommendedMap.put(userId, tagIds);
		} else {
			User t = userRepository.findOne(userId);
			Set<UserTag> relation = convert(t.getFollowing());
			QPageRequest pageable = new QPageRequest(page.intValue(), 20);
			Set<String> ids = recommendedMap.get(userId);
			List<Tuple> tags = tupleRepository.findAllRecommendedUser(ids, pageable);			
			for (Tuple tuple : tags) {
				Tag tag = (Tag) tuple.get(0);
				if(ids != null && ids.contains(tag.getId())) break;
				List<UserTag> userTags = userTagRepository.findByTagId(
						tag.getId(), pageUserTag);
				if (!userTags.isEmpty()) {
					Map map = new HashMap();
					map.put("name", tag.getName());
					List uts = new ArrayList();
					for (UserTag ut : userTags) {
						Map m = new HashMap();
						m.put("id", ut.getId());
						m.put("name", ut.getSubject());
						m.put("userId", ut.getUser().getId());
						m.put("nickName", ut.getUser().getUsername());
						m.put("image", ut.getUser().getPicture());
						m.put("status", ut.getStatus());
						if (relation.contains(ut)) {
							m.put("followed", "1");
						} else {
							m.put("followed", "0");
						}
						uts.add(m);
					}
					map.put("userTags", uts);
					result.add(map);
				}
			}
		}
		return result;
	}
	
	public List<Map> recommendInMemory(String userId, Integer page) {
		List<Map> result = new ArrayList<Map>();
		User u = userRepository.findOne(userId);
		Set<UserTag> relation = convert(u.getFollowing());
		QPageRequest pageUserTag = new QPageRequest(0, 10);
		List<Tag> tags = findRecommendTags(u, page);
		for (Tag tag : tags) {
			List<UserTag> userTags = userTagRepository.findByTagId(
					tag.getId(), pageUserTag);
			if (!userTags.isEmpty()) {
				Map map = new HashMap();
				map.put("name", tag.getName());
				List uts = new ArrayList();
				for (UserTag ut : userTags) {
					Map m = new HashMap();
					m.put("id", ut.getId());
					m.put("name", ut.getSubject());
					m.put("userId", ut.getUser().getId());
					m.put("nickName", ut.getUser().getUsername());
					m.put("image", ut.getUser().getPicture());
					m.put("status", ut.getStatus());
					if (relation.contains(ut)) {
						m.put("followed", "1");
					} else {
						m.put("followed", "0");
					}
					uts.add(m);
				}
				map.put("userTags", uts);
				result.add(map);
			}
		}
		return result;
	}
	
	public List<Tag> findRecommendTags(User u, Integer page){
		List<Tag> result = new ArrayList<Tag>();
		List<Tag> all = tagRepository.findAll();
		Set<Tag> like = new HashSet<Tag>();
		List<Tag> left = new ArrayList<Tag>();
		
		Set<UserTag> tags = u.getUserTags();
		Iterator<UserTag> it = tags.iterator();
		QPageRequest pageTag = new QPageRequest(0, 5);				  
		while (it.hasNext()) {
			UserTag rt = it.next();
			String name = rt.getTag().getName();
			List<Tag> names = tagRepository.findByNameLikeOrderByNameDesc(
					name, pageTag);
			for(Tag t:names){
				if(like.size() < 10 && isValid(u, t)) {
					like.add(t);
				}
			}
		}
		
		for(Tag t:all){
			if(!like.contains(t) && isValid(u, t)) {
				left.add(t);
			}
		}		
		
		Collections.sort(left, new Comparator<Tag>(){

			@Override
			public int compare(Tag t1, Tag t2) {
				int s1 = t1.getUserTags().size();
				int s2 = t2.getUserTags().size();
				return s2 - s1;
			}
			
		});
		
		int defaultSize = 10;
		int cPage = 0;
		int lSize = like.size() > defaultSize ? defaultSize : like.size();
		int total = left.size() + lSize;

		if (page == null || page < 0) {
			cPage = 0;
			result.addAll(like);
			if (result.size() < defaultSize) {
				if (left.size() + result.size() >= defaultSize) {
					result.addAll(left.subList(0, defaultSize - result.size()));
				} else {
					result.addAll(left);
				}
			} else {
				result = result.subList(0, defaultSize);
			}			
		} else {
			cPage = page + 1;
			int start = cPage * defaultSize;
			int end = (cPage+1) * defaultSize;
			if(start <= total) {
				if(end <= total){
					result.addAll(left.subList(start-lSize, end-lSize));
				} else {
					result.addAll(left.subList(start-lSize, left.size()));
				}
			}
		}
				
		return result;
	}
	
	private boolean isValid(User u, Tag t) {
		boolean result = false;
		Set<UserTag> userTags = t.getUserTags();
		if(!userTags.isEmpty()) {
			for(UserTag ut: userTags) {				
				if(!ut.getUser().equals(u) && "1".equals(ut.getStatus())){
					result = true;
					break;
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
			List<UserTag> userTags = userTagRepository.findByTagIdAndStatus(tag.getId(), "1", pageUserTag);
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
	
	public List<Map<String, String>> searchUsers(String name) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		if (name == null || "".equals(name)) {
			throw new APIException(400, "bad request");
		}
		List<User> users = userRepository.findByNameContaining(name);
		for(User u : users) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", u.getId());
			map.put("nickName", u.getUsername());
			map.put("image", u.getPicture());			
			map.put("sex", u.getSex());
			map.put("birthday", u.getBirthday());
			map.put("city", u.getCity());
			map.put("userTags", getUserTagsAsString(u));
			result.add(map);
		}
		return result;
	}

	public File getImage(String filename) {
		File f = new File(imgPath + File.separator + filename);
		if(!f.exists()) {
			throw new APIException(404, "file not found");
		}
		return f;
	}
	
	public File getClientFileByVersion(String version) {
		Client client = getClientByClient(version);
		return new File(clientPath + File.separator + client.getPath());
	}

	public File getCurrentClientFile() {
		Client client = getCurrentClient();
		return new File(clientPath + File.separator + client.getPath());
	}
	
	private Client getCurrentClient() {
		List<Client> clients = clientRepository.findCurrentClient();
		if(clients == null || clients.isEmpty()) {
			throw new APIException(404, "file not found");
		}
		return clients.get(0);
	}
	
	public Client getClientByClient(String version) {
		Client client = null;
		if(!StringUtils.isEmpty(version)) {
			List<Client> clients = clientRepository.findByVersion(version);
			if (clients.isEmpty()) {
				client = getCurrentClient();
			} else {
				client = clients.get(0);
				if(client.getParent() != null) {
					client = client.getParent();
				}
			}
		}
		if(client == null) {
			throw new APIException(404, "file not found");
		}
		return client;
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
			User user = result.getUser();
			if(!user.getId().equals(userId)) {
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
			if("1".equals(status)) {
				//push notifications
				Set<UserRelation> followers = result.getFollowers();
				if(!followers.isEmpty()){
					String[] ids = new String[followers.size()];
					int i = 0;
					for(UserRelation ur: followers) {
						ids[i++] = ur.getSourceUser().getId();
					}
					jpushService.pushNotification(user.getUsername() + " is online", user.getId(), ids);
				}
			}
			return result;
		}
		throw new APIException(400, "invalid userTag");
	}

	public Map getProfile(String souceUserId, String destUserId) {
		User souceUser = userRepository.findOne(souceUserId);
		User destUser = userRepository.findOne(destUserId);
		if (souceUser == null || destUser == null) {
			throw new APIException(400, "user not exist");
		}
		List<UserRelation> relations = userRelationRepository.findBySourceIdAndTargetId(souceUserId, destUserId);
		Set<UserTag> relation = convert(relations);
		Map result = new HashMap();
		result.put("id", destUser.getId());
		result.put("nickName", destUser.getUsername());
		result.put("image", destUser.getPicture());
		result.put("sex", destUser.getSex());
		result.put("birthday", destUser.getBirthday());
		result.put("city", destUser.getCity());
		Set<UserTag> userTags = destUser.getUserTags();
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
			if(relation.contains(ut)) {
				m.put("followed", "1");	
			} else {
				m.put("followed", "0");
			}
			m.put("peopleNumbers", String.valueOf(ut.getPeopleNumbers()));
			m.put("fanNumbers", String.valueOf(ut.getFollowers().size()));
			uts.add(m);
		}
		result.put("userTags", uts);
		return result;
	}

	private Set<UserTag> convert(Collection<UserRelation> relations) {
		Set<UserTag> result = new HashSet<UserTag>();
		for(UserRelation u : relations) {
			result.add(u.getTargetUserTag());
		}
		return result;
	}

	@Transactional
	public void firendship(String userId, String targetId, String action) {
		User user = userRepository.findOne(userId);
		UserTag target = userTagRepository.findOne(targetId);
		if(user == null || target == null) {
			throw new APIException(400, "user not exist");
		}
		List<UserRelation> relations = userRelationRepository.findBySourceIdAndTargetTagId(userId, targetId);
		if("create".equals(action)) {
			if(relations.isEmpty()) {
				UserRelation ur = new UserRelation();
				ur.setSourceUser(user);
				ur.setTargetUserTag(target);
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

	public List<Map<String, String>> friends(String userId) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		Set<UserRelation> friends = user.getFollowing();
		for(UserRelation u : friends) {
			Map<String, String> map = new HashMap<String, String>();
			UserTag ut = u.getTargetUserTag();
			User taget = ut.getUser();			
			map.put("id", ut.getId());			
			map.put("name", ut.getSubject());
			map.put("tagName", ut.getTag().getName());
			map.put("userId", taget.getId());
			map.put("nickName", taget.getUsername());
			map.put("image", taget.getPicture());
			map.put("status", ut.getStatus());
			map.put("peopleNumbers", String.valueOf(ut.getPeopleNumbers()));	
			map.put("fanNumbers", String.valueOf(ut.getFollowers().size()));
			result.add(map);
		}
		return result;
	}

	public Collection<Map<String, String>> followers(String userId) {
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		Set<UserTag> userTags = user.getUserTags();
		for(UserTag ut: userTags){
			Set<UserRelation> followers = ut.getFollowers();
			for(UserRelation ur: followers) {
				User u = ur.getSourceUser();
				if(!result.containsKey(u.getId())) {
					Map<String, String> map = new HashMap<String, String>();				
					map.put("userId", u.getId());
					map.put("nickName", u.getUsername());
					map.put("image", u.getPicture());
					result.put(u.getId(), map);	
				}					
			}			
		}
		return result.values();
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

	public List<Map<String, String>> lookupUsers(String userId) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		if (userId == null || "".equals(userId)) {
			throw new APIException(400, "bad request");
		}
		String[] userIds = userId.split(",");
		List<String> ids = Arrays.asList(userIds);
		List<User> users = userRepository.findAll(ids);
		for(User u : users) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", u.getId());
			map.put("nickName", u.getUsername());
			map.put("image", u.getPicture());			
			map.put("sex", u.getSex());
			map.put("birthday", u.getBirthday());
			map.put("city", u.getCity());
			map.put("userTags", getUserTagsAsString(u));
			result.add(map);
		}
		return result;
	}
	
	public List<Map<String, String>> lookupUsersByNickname(String nickName) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		if (nickName == null || "".equals(nickName)) {
			throw new APIException(400, "bad request");
		}
		String[] nickNames = nickName.split(",");
		final List<String> names = Arrays.asList(nickNames);
		List<User> users = userRepository.findAll(new Specification<User>(){

			@Override
			public Predicate toPredicate(Root<User> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				Path<Object> nickName = root.get("username");
				return nickName.in(names);
			}
			
		});
		for(User u : users) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", u.getId());
			map.put("nickName", u.getUsername());
			map.put("image", u.getPicture());			
			map.put("sex", u.getSex());
			map.put("birthday", u.getBirthday());
			map.put("city", u.getCity());
			map.put("userTags", getUserTagsAsString(u));
			result.add(map);
		}
		return result;
	}
	
	public List<Map<String, String>> lookupUsersByTagName(String userId, String tagName) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		if (tagName == null || "".equals(tagName)) {
			throw new APIException(400, "bad request");
		}
		User user = userRepository.findOne(userId);
		Set<UserTag> relation = convert(user.getFollowing());
		List<Tag> tags = tagRepository.findByName(tagName);		
		if(tags != null) {
			for(Tag t : tags) {
				Set<UserTag> userTags = t.getUserTags();
				for(UserTag ut : userTags) {
					User u = ut.getUser();					
					Map<String, String> m = new HashMap<String, String>();
					m.put("id", ut.getId());
					m.put("name", ut.getSubject());
					m.put("tagName", t.getName());
					m.put("userId", u.getId());
					m.put("nickName", u.getUsername());
					m.put("image", u.getPicture());
					m.put("status", ut.getStatus());
					if (relation.contains(ut)) {
						m.put("followed", "1");
					} else {
						m.put("followed", "0");
					}
					result.add(m);
				}
			}
		}

		return result;
	}

	private String getUserTagsAsString(User u) {
		List<String> tags = new ArrayList<String>();
		Set<UserTag> userTags = u.getUserTags();
		for(UserTag ut: userTags) {
			tags.add(ut.getTag().getName());
		}
		
		return StringUtils.join(tags, ",");
	}

	public Map getRoom(String roomId) {
		UserTag ut = userTagRepository.findOne(roomId);
		if (ut == null) {
			throw new APIException(400, "room not exist");
		}
		Map result = new HashMap();
		User user = ut.getUser();
		result.put("id", ut.getId());			
		result.put("name", ut.getSubject());
		result.put("tagName", ut.getTag().getName());
		result.put("userId", ut.getUser().getId());
		result.put("nickName", ut.getUser().getUsername());
		result.put("image", ut.getUser().getPicture());
		result.put("status", ut.getStatus());
		Set<UserTag> userTags = user.getUserTags();
		List<String> uts = new ArrayList<String>();
		Set<UserTagSubject> userTagSubjects = ut.getUserTagSubjects();
		result.put("historySubjects", userTagSubjects);
		return result;
	}

	@Transactional
	public void updatePassword(String userId, String oldPassword,
			String password) {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new APIException(400, "user not exist");
		}
		if(oldPassword == null || password == null) {
			throw new APIException(400, "password required");
		}
		if(!user.getPassword().equals(oldPassword)) {
			throw new APIException(400, "old password does not match");
		}
	    user.setPassword(SecurityUtil.encrypt(password));
		userRepository.saveAndFlush(user);
	}

	@Transactional
	public Issue createIssue(Issue i) {
		if (i.getTitle() == null || i.getDescription() == null) {
			throw new APIException(400, "bad request");
		}		
		i.setCreateTime(new Date());
		return issueRepository.save(i);
	}

}
