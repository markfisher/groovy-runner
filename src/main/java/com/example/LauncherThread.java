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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.cli.command.run.RunCommand;
import org.springframework.boot.cli.compiler.GroovyCompiler;
import org.springframework.boot.cli.compiler.GroovyCompilerConfiguration;
import org.springframework.boot.cli.compiler.GroovyCompilerScope;
import org.springframework.boot.cli.compiler.RepositoryConfigurationFactory;
import org.springframework.boot.cli.compiler.grape.RepositoryConfiguration;

/**
 * @author Dave Syer
 *
 */
public class LauncherThread extends Thread {

	private String[] args;

	private Map<String, Object> result;

	private int count;

	private String[] sources;

	private GroovyCompiler compiler;

	public LauncherThread(ClassLoader classLoader, int count, String source,
			String... args) {
		super("spring-launcher-" + count);
		this.count = count;
		this.sources = new String[] {source};
		this.args = args;
		setContextClassLoader(classLoader);
		setDaemon(true);
	}

	public Map<String, Object> getResult() {
		return result;
	}

	@Override
	public void run() {
		launch();
	}

	private void launch() {
		try {
			this.compiler = new GroovyCompiler(new LauncherConfiguration());
			RunCommand command = new RunCommand();
			command.run(getArgs());
			command.stop();
			result = Collections.singletonMap("status", "DONE");
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private String[] getArgs() {
		String[] args = new String[this.args.length + 4];
		args[0] = this.sources[0];
		System.arraycopy(this.args, 0, args, 1, this.args.length);
		args[this.args.length + 1] = "--";
		args[this.args.length + 2] = "--spring.config.name=scriptlet";
		args[this.args.length + 3] = "--spring.jmx.default-domain=scriptlet." + count;
		return args;
	}

	private Object[] compile() throws IOException {
		Object[] compiledSources = this.compiler.compile(this.sources);
		if (compiledSources.length == 0) {
			throw new RuntimeException(
					"No classes found in '" + Arrays.toString(this.sources) + "'");
		}
		return compiledSources;
	}

}

class LauncherConfiguration implements GroovyCompilerConfiguration {

	@Override
	public GroovyCompilerScope getScope() {
		return GroovyCompilerScope.DEFAULT;
	}

	@Override
	public boolean isGuessImports() {
		return true;
	}

	@Override
	public boolean isGuessDependencies() {
		return true;
	}

	@Override
	public boolean isAutoconfigure() {
		return true;
	}

	@Override
	public String[] getClasspath() {
		return DEFAULT_CLASSPATH;
	}

	@Override
	public List<RepositoryConfiguration> getRepositoryConfiguration() {
		return RepositoryConfigurationFactory.createDefaultRepositoryConfiguration();
	}

}