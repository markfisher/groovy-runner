class Main {
	@Bean
	CommandLineRunner runner() { {
			args ->  println "Args: ${args}" }
	}
}