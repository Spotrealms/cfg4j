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

import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * {@link PropertiesProvider} that interprets given stream as JSON or HJSON file.
 */
public class TomlBasedPropertiesProvider extends FormatBasedPropertiesProvider {

	/**
	 * Get {@link Properties} for a given {@code inputStream} treating it as a JSON or HJSON file.
	 *
	 * @param inputStream input stream representing JSON or HJSON file
	 * @return properties representing values from {@code inputStream}
	 * @throws IllegalStateException when unable to read properties
	 */
	@Override
	public Properties getProperties(InputStream inputStream){
		requireNonNull(inputStream);

		Properties properties = new Properties();

		try{
			//Parse the InputStream to toml
			TomlParseResult toml = Toml.parse(inputStream);

			//Convert to map
			Map<String, Object> tomlAsMap = convertToMap(toml.toMap());

			//Put all into the properties object
			properties.putAll(flatten(tomlAsMap));

			//Return the properties file
			return properties;

		} catch(Exception e){
			throw new IllegalStateException("Unable to load toml configuration from provided stream", e);
		}
	}

	/**
	 * Convert given Toml document to a multi-level map.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> convertToMap(Object tomlDocument){
		//Initialize the map
		Map<String, Object> tomlMap = new LinkedHashMap<>();

		//Document is a text block
		if(!(tomlDocument instanceof Map)){
			tomlMap.put("content", tomlDocument);
			return tomlMap;
		}

		//Iterate over the raw toml data
		for(Map.Entry<Object, Object> entry : ((Map<Object, Object>) tomlDocument).entrySet()){
			//Get the current value
			Object value = entry.getValue();

			//Check if the value is a simple toml table
			if(value instanceof TomlTable){
				//Recursively call the method
				value = convertToMap(((TomlTable) value).toMap());
			}
			else if (value instanceof TomlArray){
				//Create a new collection to hold the toml array elements
				ArrayList<Map<String, Object>> collection = new ArrayList<>();

				//Loop over each element of the toml array
				for(Object element : ((TomlArray) value).toList()){
					//Derive the element by a recursive call and add it to the collection
					collection.add(convertToMap(element));
				}

				//Set the value to be the collection
				value = collection;
			}

			//Put the keypair into the toml map
			tomlMap.put(entry.getKey().toString(), value);
		}

		//Return the filled toml map
		return tomlMap;
	}
}
