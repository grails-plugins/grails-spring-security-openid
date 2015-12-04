/* Copyright 2015 the original author or authors.
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
package grails.plugin.springsecurity.openid;

import java.util.Collections;
import java.util.List;

import org.springframework.security.openid.AxFetchListFactory;
import org.springframework.security.openid.OpenIDAttribute;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsAxFetchListFactory implements AxFetchListFactory {

	private final List<OpenIDAttribute> fetchAttrs;

	public GrailsAxFetchListFactory(List<OpenIDAttribute> attributes) {
		fetchAttrs = Collections.unmodifiableList(attributes);
	}

	public List<OpenIDAttribute> createAttributeList(String identifier) {
		return fetchAttrs;
	}
}
