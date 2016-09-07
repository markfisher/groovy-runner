import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.example.MessageExchange;

@Configuration
class Main {

	@Bean
	Function<Map<String, Object>, Map<String, Object>> runner() { { request ->
			["foo" : "bar", "request" : request]
		}
	}

}

@Component
class Listener {

	private Function<Map<String, Object>, Map<String, Object>> handler;

	@Autowired(required = false)
	public void setHandler(Function<Map<String, Object>, Map<String, Object>> handler) {
		this.handler = handler;
	}

	@EventListener
	public void handle(ApplicationReadyEvent event) {
		if (handler != null) {
			MessageExchange.setResponse(handler.apply(MessageExchange.getRequest()));
		}
	}

}
