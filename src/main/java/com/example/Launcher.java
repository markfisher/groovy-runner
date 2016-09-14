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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.cli.app.SpringApplicationLauncher;
import org.springframework.boot.cli.compiler.GroovyCompiler;
import org.springframework.boot.cli.compiler.GroovyCompilerConfiguration;
import org.springframework.boot.cli.compiler.GroovyCompilerScope;
import org.springframework.boot.cli.compiler.RepositoryConfigurationFactory;
import org.springframework.boot.cli.compiler.grape.RepositoryConfiguration;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 *
 */
public class Launcher implements Handler {

	private String[] args;

	private int count;

	private String[] sources;

	private GroovyCompiler compiler;

	private RunThread runThread;

	private Object monitor = new Object();

	private ClassLoader classLoader;

	private List<Object> compiled;

	public Launcher(ClassLoader classLoader, int count, String source, String... args) {
		this.classLoader = classLoader;
		this.count = count;
		this.sources = new String[] { source };
		this.args = args;
	}

	@Override
	public Map<String, Object> handle(Map<String, Object> request) {
		ClassUtils.overrideThreadContextClassLoader(classLoader);
		launch();
		Handler handler = runThread.getHandler();
		Map<String, Object> result = handler.handle(request);
		close();
		return result;
	}

	private void launch() {
		synchronized (this.monitor) {
			try {
				if (compiled == null) {
					this.compiler = new GroovyCompiler(new LauncherConfiguration());
					compiled = new ArrayList<>(Arrays.asList(compile()));
				}
				// Run in new thread to ensure that the context classloader is setup
				this.runThread = new RunThread(compiled);
				this.runThread.start();
				this.runThread.join();
			}
			catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public void close() {
		synchronized (this.monitor) {
			if (this.runThread != null) {
				if (this.runThread.applicationContext != null) {
					this.runThread.shutdown();
				}
				this.runThread = null;
			}
		}
	}

	private String[] getArgs() {
		String[] args = new String[this.args.length + 2];
		System.arraycopy(this.args, 0, args, 0, this.args.length);
		args[this.args.length] = "--spring.config.name=scriptlet";
		args[this.args.length + 1] = "--spring.jmx.default-domain=scriptlet." + count;
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

	private class RunThread extends Thread {

		private final Object monitor = new Object();

		private final List<Object> compiledSources;

		private Object applicationContext;

		/**
		 * Create a new {@link RunThread} instance.
		 * @param compiledSources the sources to launch
		 */
		RunThread(List<Object> compiledSources) {
			super("runner-" + count);
			this.compiledSources = compiledSources;
			if (!compiledSources.isEmpty() && compiledSources.get(0) instanceof Class) {
				setContextClassLoader(
						((Class<?>) compiledSources.get(0)).getClassLoader());
			}
			setDaemon(true);
		}

		public Handler getHandler() {
			try {
				Method method = this.applicationContext.getClass()
						.getMethod("getBean", Class.class);
				Handler handler = (Handler) method.invoke(this.applicationContext,
						Handler.class);
				return handler;
			}
			catch (Exception ex) {
				return request -> Collections.singletonMap("status", "FAILED");
			}
		}

		@Override
		public void run() {
			synchronized (this.monitor) {
				try {
					this.applicationContext = new SpringApplicationLauncher(
							getContextClassLoader()).launch(
									this.compiledSources.toArray(new Object[0]),
									getArgs());
				}
				catch (Exception ex) {
				}
			}
		}

		/**
		 * Shutdown the thread, closing any previously opened application context.
		 */
		public void shutdown() {
			synchronized (this.monitor) {
				if (this.applicationContext != null) {
					try {
						Method method = this.applicationContext.getClass()
								.getMethod("close");
						method.invoke(this.applicationContext);
					}
					catch (NoSuchMethodException ex) {
						// Not an application context that we can close
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					finally {
						this.applicationContext = null;
					}
				}
			}
		}
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