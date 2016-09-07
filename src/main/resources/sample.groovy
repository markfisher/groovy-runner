class Main {
	@Bean
	CommandLineRunner runner(com.example.MessageExchange result) {
		{ args ->
			result.setResponse(["foo" : "bar"])
		}
	}
}