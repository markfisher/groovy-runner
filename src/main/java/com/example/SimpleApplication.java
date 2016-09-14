package com.example;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SimpleApplication {

	private LauncherCommand launcher;

	@GetMapping
	public Map<String, Object> get() throws Exception {
		return translate(null);
	}

	@RequestMapping
	public Map<String, Object> handle(@RequestBody Map<String, Object> body)
			throws Exception {
		return translate(body);
	}

	public static void main(String[] args) {
		SpringApplication.run(SimpleApplication.class, args);
	}

	private Map<String, Object> translate(Map<String, Object> body) throws Exception {
		if (launcher == null) {
			launcher = new LauncherCommand();
		}
		return launcher.run(body, "classpath:sample.groovy");
	}
}

class LauncherCommand {
	
	private LauncherFactory threadFactory = new LauncherFactory();
	private ExecutorService executor;
	
	public LauncherCommand() {
		executor = Executors.newCachedThreadPool();
	}

	public synchronized Map<String, Object> run(Map<String, Object> request,
			String source, String... args) throws Exception {		
		Future<Map<String, Object>> task = executor.submit(threadFactory.getTask(request, source, args));
		return task.get();
	}

}