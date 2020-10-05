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

import static java.util.Objects.requireNonNull;

/**
 * Selects {@link PropertiesProvider} to use based on a file extension.
 */
public class PropertiesProviderSelector {

	private final PropertiesProvider yamlProvider;
	private final PropertiesProvider jsonProvider;
	private final PropertiesProvider hjsonProvider;
	private final PropertiesProvider tomlProvider;
	private final PropertiesProvider propertiesProvider;

	/**
	 * Construct selector.
	 *
	 * @param propertiesProvider provider used for parsing properties files
	 * @param yamlProvider       provider used for parsing Yaml files
	 * @param jsonProvider       provider used for parsing JSON files
	 * @param hjsonProvider      provider used for parsing HJSON files
	 * @param tomlProvider       provider used for parsing TOML and INI files
	 */
	public PropertiesProviderSelector(PropertiesProvider propertiesProvider, PropertiesProvider yamlProvider, PropertiesProvider jsonProvider, PropertiesProvider hjsonProvider, PropertiesProvider tomlProvider) {
		this.propertiesProvider = requireNonNull(propertiesProvider);
		this.yamlProvider = requireNonNull(yamlProvider);
		this.jsonProvider = requireNonNull(jsonProvider);
		this.hjsonProvider = requireNonNull(hjsonProvider);
		this.tomlProvider = requireNonNull(tomlProvider);
	}

	/**
	 * Selects {@link PropertiesProvider} to use based on a file extension. For *.yaml files
	 * returns {@code yamlProvider}. For any other extension returns {@code propertiesProvider}.
	 *
	 * @param filename configuration file name
	 * @return provider for the give file type
	 */
	public PropertiesProvider getProvider(String filename) {
		//Transform to lowercase
		filename = filename.toLowerCase();

		//Yaml-based config
		if(filename.endsWith(".yaml") || filename.endsWith(".yml")) return yamlProvider;

		//Json-based config
		else if(filename.endsWith(".json")) return jsonProvider;

		//Hjson-based config
		else if(filename.endsWith(".hjson")) return hjsonProvider;

		//Toml-based config
		else if(filename.endsWith(".toml") || filename.endsWith(".tml")) return tomlProvider;

		//Return a properties based config by default
		else return propertiesProvider;
	}
}
