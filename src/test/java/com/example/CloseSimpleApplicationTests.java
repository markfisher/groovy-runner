package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration // (loader=CustomLoader.class)
@AutoConfigureMockMvc
public class CloseSimpleApplicationTests {

	@Autowired
	private SimpleApplication launchers;

	@Test
	public void before() throws Exception {
		launchers.close();
	}

}
