package com.example;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.cli.command.run.RunCommand;
import org.springframework.boot.cli.compiler.RepositoryConfigurationFactory;
import org.springframework.boot.cli.compiler.grape.AetherGrapeEngine;
import org.springframework.boot.cli.compiler.grape.AetherGrapeEngineFactory;
import org.springframework.boot.cli.compiler.grape.DependencyResolutionContext;
import org.springframework.boot.cli.compiler.grape.RepositoryConfiguration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import groovy.lang.GroovyClassLoader;

@SpringBootApplication
@RestController
public class SimpleApplication {

	@GetMapping
	public Map<String, Object> get() throws Exception {
		return translate(null);
	}

	@RequestMapping
	public Map<String, Object> handle(@RequestBody Map<String, Object> body) throws Exception {
		return translate(body);
	}

	public static void main(String[] args) {
		SpringApplication.run(SimpleApplication.class, args);
	}

	private Map<String, Object> translate(Map<String, Object> body) throws Exception {
		return new LauncherCommand().run(body, "classpath:sample.groovy");
	}
}

class LauncherCommand {

	private static AtomicInteger count = new AtomicInteger();
	private static final String DEFAULT_VERSION = "1.4.0.RELEASE";
	private URI[] baseUris;

	public synchronized Map<String, Object> run(Map<String, Object> request, String source, String... args) throws Exception {

		try {
			URLClassLoader classLoader = populateClassloader();

			String name = "com.example.LauncherThread";
			Class<?> threadClass = classLoader.loadClass(name);

			Constructor<?> constructor = threadClass.getConstructor(ClassLoader.class,
					int.class, Map.class, String.class, String[].class);
			Thread thread = (Thread) constructor.newInstance(classLoader,
					count.incrementAndGet(), request, source, args);
			thread.start();
			thread.join();
			return getField(thread, "result");
		}
		catch (Exception e) {
			e.printStackTrace();
			return Collections.singletonMap("status", "ERROR");
		}

	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getField(Object object, String name) {
		Field field = ReflectionUtils.findField(object.getClass(), name);
		ReflectionUtils.makeAccessible(field);
		return (Map<String, Object>) ReflectionUtils.getField(field, object);
	}

	private URLClassLoader populateClassloader()
			throws MalformedURLException, URISyntaxException {
		GroovyClassLoader loader = new GroovyClassLoader(
				Thread.currentThread().getContextClassLoader().getParent().getParent(),
				new CompilerConfiguration());

		String[] classpaths = { "." };
		for (String classpath : classpaths) {
			loader.addClasspath(classpath);
		}

		loader.addURL(LauncherCommand.class.getProtectionDomain().getCodeSource()
				.getLocation());
		for (URI uri : getBaseClassPath(loader)) {
			loader.addURL(uri.toURL());
		}
		return loader;
	}

	private URI[] getBaseClassPath(GroovyClassLoader loader) {

		if (this.baseUris == null) {
			synchronized (this) {
				if (this.baseUris == null) {

					DependencyResolutionContext resolutionContext = new DependencyResolutionContext();
					List<RepositoryConfiguration> repositoryConfiguration = RepositoryConfigurationFactory
							.createDefaultRepositoryConfiguration();
					repositoryConfiguration.add(0, new RepositoryConfiguration("local",
							new File("repository").toURI(), true));

					AetherGrapeEngine grapeEngine = AetherGrapeEngineFactory
							.create(loader, repositoryConfiguration, resolutionContext);

					HashMap<String, String> dependency = new HashMap<>();
					dependency.put("group", "org.springframework.boot");
					dependency.put("module", "spring-boot-cli");
					dependency.put("version", getVersion());
					this.baseUris = grapeEngine.resolve(null, dependency);

				}
			}
		}

		return this.baseUris;

	}

	private String getVersion() {
		Package pkg = RunCommand.class.getPackage();
		String version = (pkg != null ? pkg.getImplementationVersion() : DEFAULT_VERSION);
		return version != null ? version : DEFAULT_VERSION;
	}

}