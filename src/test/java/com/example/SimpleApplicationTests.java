package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration//(loader=CustomLoader.class)
@AutoConfigureMockMvc
public class SimpleApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void before() throws Exception {
		mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
				.content("{\"one\":\"two\"}")).andExpect(status().isOk())
				.andExpect(content().json(
						"{\"foo\":\"bar\",\"request\":{\"one\":\"two\"}}"));
	}

	@Test
	@Repeat(10)
	public void several() throws Exception {
		mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
				.content("{\"one\":\"two\"}")).andExpect(status().isOk())
				.andExpect(content().json(
						"{\"foo\":\"bar\",\"request\":{\"one\":\"two\"}}"));
	}

}
