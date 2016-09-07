import com.example.Handler

@Configuration
class Main {

	@Bean
	Handler runner() { { request ->
			["foo" : "bar", "request" : request]
		}
	}

}
