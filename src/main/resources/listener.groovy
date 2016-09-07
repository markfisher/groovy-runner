import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

import com.example.MessageExchange

@Component
class Listener {

	private Handler handler;

	@Autowired(required = false)
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@EventListener
	public void handle(ApplicationReadyEvent event) {
		if (handler != null) {
			MessageExchange.setResponse(handler.handle(MessageExchange.getRequest()));
		}
	}

}
