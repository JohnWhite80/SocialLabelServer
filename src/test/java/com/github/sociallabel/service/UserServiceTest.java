package com.github.sociallabel.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.sociallabel.TestConfig;
import com.github.sociallabel.entity.User;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = { TestConfig.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceTest {

	@Autowired
	private UserService userService;

	@Test
	public void testAddUser() {
		User user = new User();
		user.setUsername("1");
		user.setEmail("1@1.com");
		user.setPassword("password");
		userService.addUser(user);
	}

}
