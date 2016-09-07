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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
	
/**
 * @author Dave Syer
 *
 */
public class MessageExchange {

	static private AtomicInteger count = new AtomicInteger();

	static private Map<String, Object> request = new LinkedHashMap<>();

	static private Map<String, Object> response = new LinkedHashMap<>();

	static public Map<String, Object> getRequest() {
		return request;
	}

	static public void setRequest(Map<String, Object> request) {
		MessageExchange.request = request;
	}

	static public Map<String, Object> getResult() {
		return response;
	}

	static public void setResponse(Map<String, Object> result) {
		MessageExchange.response.putAll(result);
		response.put("count", count.incrementAndGet());
	}

}
