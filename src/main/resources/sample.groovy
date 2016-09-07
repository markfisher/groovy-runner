import java.util.function.Function;

@Configuration
class Main {

	@Bean
	Function<Map<String, Object>, Map<String, Object>> runner() { { request ->
			["foo" : "bar", "request" : request]
		}
	}

}
