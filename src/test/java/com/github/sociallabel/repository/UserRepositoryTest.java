package com.github.sociallabel.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.sociallabel.TestConfig;
import com.github.sociallabel.entity.Tag;
import com.github.sociallabel.entity.User;
import com.github.sociallabel.entity.UserTag;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = { TestConfig.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class UserRepositoryTest {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private UserTagRepository userTagRepository;

	@Test
	@Transactional
	public void test() {
		User user = new User();
		user.setUsername("1");
		user.setEmail("1@1.com");
		user.setPassword("password");

		Tag tag = new Tag();
		tag.setName("tag");

		UserTag userTag = new UserTag();
		userTag.setTag(tag);
		userTag.setUser(user);

		tagRepository.save(tag);

		userRepository.save(user);

		user.getUserTags().add(userTag);
		tag.getUserTags().add(userTag);

		userTagRepository.save(userTag);

		Tag tagOne = tagRepository.findOne(tag.getId());

		assertNotSame(tagOne.getUserTags().size(), 0);

		User userOne = userRepository.findOne(user.getId());

		assertNotSame(userOne.getUserTags().size(), 0);

		UserTag one = userTagRepository.findOne(userTag.getId());
		assertNotNull(one.getTag());
		assertNotNull(one.getUser());

	}

}
