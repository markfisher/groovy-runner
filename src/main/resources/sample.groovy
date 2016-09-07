@Configuration
class Main {

	@Bean
	Handler runner() { { request ->
			["foo" : "bar", "request" : request]
		}
	}

}
