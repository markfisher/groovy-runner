class Main {
	@Bean
	CommandLineRunner runner() {
		{ args ->
			com.example.MessageExchange.setResponse(["foo" : "bar", "request" : com.example.MessageExchange.getRequest()])
		}
	}
}