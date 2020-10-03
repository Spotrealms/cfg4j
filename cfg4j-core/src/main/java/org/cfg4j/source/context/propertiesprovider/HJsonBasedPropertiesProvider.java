/*
 * Copyright 2015-2018 Norbert Potocki (norbert.potocki@nort.pl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cfg4j.source.context.propertiesprovider;

import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * {@link PropertiesProvider} that interprets given stream as HJSON file.
 */
public class HJsonBasedPropertiesProvider extends FormatBasedPropertiesProvider {

	/**
	 * Get {@link Properties} for a given {@code inputStream} treating it as an HJSON file.
	 *
	 * @param inputStream input stream representing HJSON file
	 * @return properties representing values from {@code inputStream}
	 * @throws IllegalStateException when unable to read properties
	 */
	@Override
	public Properties getProperties(InputStream inputStream) {
		requireNonNull(inputStream);

		Properties properties = new Properties();

		try {
			//Parse the InputStream to hjson
			JsonObject hjson = JsonValue.readHjson(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).asObject();

			//Convert to map
			Map<String, Object> hjsonAsMap = convertToMap(hjson);

			//Put all into the properties object
			properties.putAll(flatten(hjsonAsMap));

			//Return the properties file
			return properties;
		}
		catch (Exception e) {
			throw new IllegalStateException("Unable to load hjson configuration from provided stream", e);
		}
	}

	/**
	 * Convert given Json document to a multi-level map.
	 */
	private Map<String, Object> convertToMap(Object jsonDocument) {
		Map<String, Object> hjsonMap = new LinkedHashMap<>();

		// Document is a text block
		if(!(jsonDocument instanceof JsonObject)){
			hjsonMap.put("content", jsonDocument);
			return hjsonMap;
		}

		JsonObject obj = (JsonObject) jsonDocument;
		for(JsonObject.Member member : obj){
			String key = member.getName();
			Object value = obj.get(key);

			if(value instanceof JsonObject){
				value = convertToMap(value);
			}
			else if(value instanceof JsonArray){
				ArrayList<Map<String, Object>> collection = new ArrayList<>();

				for(Object element : ((JsonArray) value)){
					collection.add(convertToMap(stripQuotesIfJsonString(element))); //Assert that any json strings have their quotes removed
				}

				value = collection;
			}

			hjsonMap.put(key, stripQuotesIfJsonString(value)); //Assert that any json strings have their quotes removed
		}

		return hjsonMap;
	}

	/**
	 * Strips the leading and trailing quotes off
	 * a JsonString if the input is one
	 * @param allegedJsonString The object to test and strip quotes from
	 * @return The cleaned object
	 */
	private Object stripQuotesIfJsonString(Object allegedJsonString){
		//Check if the datatype of the input is a string
		if(allegedJsonString instanceof JsonValue && ((JsonValue) allegedJsonString).isString()){
			//Strip the quotes off the beginning and end and return
			return ((JsonValue) allegedJsonString).asString().replaceAll("^[\"']|[\"']$", "");
		}
		else {
			//Just pass back the object as is
			return allegedJsonString;
		}
	}
}
