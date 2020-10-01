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

import org.hjson.JsonValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * {@link PropertiesProvider} that interprets given stream as JSON or HJSON file.
 */
public class JsonBasedPropertiesProvider extends FormatBasedPropertiesProvider {

	/**
	 * Get {@link Properties} for a given {@code inputStream} treating it as a JSON or HJSON file.
	 *
	 * @param inputStream input stream representing JSON or HJSON file
	 * @return properties representing values from {@code inputStream}
	 * @throws IllegalStateException when unable to read properties
	 */
	@Override
	public Properties getProperties(InputStream inputStream) {
		requireNonNull(inputStream);

		Properties properties = new Properties();

		try {
			//Extract all lines
			String hjsonText = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

			//Convert to JSON
			String jsonString = JsonValue.readHjson(hjsonText).toString();

			//Convert back to an InputStream
			InputStream jsonStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

			JSONTokener tokener = new JSONTokener(jsonStream);
			if (tokener.end()) {
				return properties;
			}
			if (tokener.nextClean() == '"') {
				tokener.back();
				properties.put("content", tokener.nextValue().toString());
			} else {
				tokener.back();
				JSONObject obj = new JSONObject(tokener);

				Map<String, Object> jsonAsMap = convertToMap(obj);
				properties.putAll(flatten(jsonAsMap));
			}

			return properties;

		} catch (Exception e) {
			throw new IllegalStateException("Unable to load json configuration from provided stream", e);
		}
	}

	/**
	 * Convert given Json document to a multi-level map.
	 */
	private Map<String, Object> convertToMap(Object jsonDocument) {
		Map<String, Object> jsonMap = new LinkedHashMap<>();

		// Document is a text block
		if (!(jsonDocument instanceof JSONObject)) {
			jsonMap.put("content", jsonDocument);
			return jsonMap;
		}

		JSONObject obj = (JSONObject) jsonDocument;
		for (String key : obj.keySet()) {
			Object value = obj.get(key);

			if (value instanceof JSONObject) {
				value = convertToMap(value);
			} else if (value instanceof JSONArray) {
				ArrayList<Map<String, Object>> collection = new ArrayList<>();

				for (Object element : ((JSONArray) value)) {
					collection.add(convertToMap(element));
				}

				value = collection;
			}

			jsonMap.put(key, value);
		}
		return jsonMap;
	}
}
