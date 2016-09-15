/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.boot.cli.command.run.RunCommand;
import org.springframework.boot.cli.compiler.RepositoryConfigurationFactory;
import org.springframework.boot.cli.compiler.grape.AetherGrapeEngine;
import org.springframework.boot.cli.compiler.grape.AetherGrapeEngineFactory;
import org.springframework.boot.cli.compiler.grape.DependencyResolutionContext;
import org.springframework.boot.cli.compiler.grape.RepositoryConfiguration;
import org.springframework.util.ReflectionUtils;

import groovy.lang.GroovyClassLoader;

/**
 * @author Dave Syer
 *
 */
public class LauncherFactory {

	private static AtomicInteger count = new AtomicInteger();
	private static final String DEFAULT_VERSION = "1.4.0.RELEASE";
	private URI[] baseUris;
	private URLClassLoader classLoader;
	private Object launcher;

	public LauncherFactory() {
		try {
			classLoader = populateClassloader();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public Object getLauncher(String source, String[] args) {
		try {
			if (launcher == null) {
				String name = "com.example.Launcher";
				Class<?> threadClass = classLoader.loadClass(name);
				Constructor<?> constructor = threadClass.getConstructor(ClassLoader.class,
						int.class, String.class, String[].class);
				Object target = constructor.newInstance(classLoader,
						count.incrementAndGet(), source, args);
				launcher = target;
			}
			return launcher;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}

	}

	private URLClassLoader populateClassloader()
			throws MalformedURLException, URISyntaxException {
		GroovyClassLoader loader = new GroovyClassLoader(
				Thread.currentThread().getContextClassLoader().getParent(),
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
