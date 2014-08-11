package com.github.sociallabel.action;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sociallabel.TestConfig;
import com.github.sociallabel.WebConfig;
import com.github.sociallabel.entity.User;
import com.github.sociallabel.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
@ContextConfiguration(classes = { TestConfig.class, WebConfig.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class APIControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper mapper;	
	
	@Autowired
    private WebApplicationContext wac;
	
	@Autowired
	private UserService userService;	
	
    @Before
    public void setup() {
    	this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mapper = new ObjectMapper();
    }
	
	@Test
	public void testRegisterUser() throws Exception {
		User user = new User();
		user.setUsername("1");
		user.setEmail("1@1.com");
		user.setPassword("password");
		String jsonUser = mapper.writeValueAsString(user);
		
		this.mockMvc.perform(post("/api/register", user).contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(jsonUser))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.userId").exists());
	}
	
	@Test
	public void testLogin() throws Exception {
		User user = new User();
		user.setUsername("2");
		user.setEmail("2@2.com");
		user.setPassword("password");
		String jsonUser = mapper.writeValueAsString(user);
		
		this.mockMvc.perform(post("/api/register").contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(jsonUser))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.userId").exists());
		
		this.mockMvc.perform(get("/api/login", user).param("email", "2@2.com").param("password", "password"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.sessionId").exists());
	}
	
	@Test
	public void testaddTag() throws Exception {
		User user = new User();
		user.setUsername("3");
		user.setEmail("3@3.com");
		user.setPassword("password");
		String jsonUser = mapper.writeValueAsString(user);
		
		this.mockMvc.perform(post("/api/register").contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(jsonUser))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.userId").exists());
		
		MvcResult loginResult = this.mockMvc.perform(get("/api/login", user).param("email", "3@3.com").param("password", "password"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.sessionId").exists()).andReturn();
		
		Map map = mapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
		String sessionId = (String) map.get("sessionId");
		
		List<Map<String, String>> tags = new ArrayList<Map<String, String>>();
		for (int i = 0; i < 3; i++) {
			Map<String, String> tag = new HashMap<String, String>();
			tag.put("name", "name" + i);
			tags.add(tag);
		}
		String jsonTags = mapper.writeValueAsString(tags);
		
		this.mockMvc.perform(post("/api/addtag/" + sessionId, tags).contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(jsonTags))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(content().string("addtag success"));		
	}
}
