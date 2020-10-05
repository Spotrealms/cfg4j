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
package org.cfg4j.provider;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Provides access to configuration on a single property level, aggregated and through binding in a format agnostic way.
 */
public interface ConfigurationProvider {

	/**
	 * Get full set of configuration represented as {@link Properties}.
	 *
	 * @return full configuration set
	 * @throws IllegalStateException when provider is unable to fetch configuration
	 */
	Properties allConfigurationAsProperties();

	/**
	 * Get a configuration property of a given basic {@code type}. Sample call could look like:
	 * <pre>
	 *   boolean myBooleanProperty = configurationProvider.getProperty("my.property", boolean.class);
	 * </pre>
	 *
	 * @param <T>  property type. Supported basic types: {@link BigDecimal}, {@link BigInteger}, {@link Boolean}, {@link Byte},
	 *             {@link Character}, {@link Class}, {@link Double}, {@link Enum}, {@link File}, {@link Float}, {@link Integer},
	 *             {@link Long}, {@link Number}, {@link Short}, {@link String}, {@link URL}, {@link URI} and arrays.
	 *             For {@link Collection} support see method {@link #getProperty(String, GenericTypeInterface)})
	 * @param key  configuration key
	 * @param type {@link Class} for {@code <T>}
	 * @return configuration value
	 * @throws NoSuchElementException   when the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException when property can't be converted to {@code type}
	 * @throws IllegalStateException    when provider is unable to fetch configuration value for the given {@code key}
	 */
	<T> T getProperty(String key, Class<T> type);

	/**
	 * Get a configuration property of a generic type {@code T}. Sample call could look like:
	 * <pre>
	 *   List&lt;String&gt; myListProperty = configurationProvider.getProperty("my.list", new GenericType&lt;List&lt;String&gt;&gt;() { });
	 * </pre>
	 *
	 * @param <T>         property type. Supported collections (and most of their standard implementations): {@link Collection},
	 *                    {@link List}, {@link Set}, {@link SortedSet}, {@link Map}, {@link SortedMap}
	 * @param key         configuration key
	 * @param genericType {@link GenericTypeInterface} wrapper for {@code <T>}
	 * @return configuration value
	 * @throws NoSuchElementException   when the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException when property can't be converted to {@code type}
	 * @throws IllegalStateException    when provider is unable to fetch configuration value for the given {@code key}
	 */
	<T> T getProperty(String key, GenericTypeInterface genericType);

	/**
	 * Create an instance of a given {@code type} that will be bound to this provider. Each time configuration changes the
	 * bound object will be updated with the new values. Use {@code prefix} to specify the relative path to configuration
	 * values. Please note that each method of returned object can throw runtime exceptions. For details see javadoc for
	 * {@link BindInvocationHandler#invoke(Object, Method, Object[])}.
	 *
	 * @param <T>    interface describing configuration object to bind
	 * @param prefix relative path to configuration values (e.g. "myContext" will map settings "myContext.someSetting",
	 *               "myContext.someOtherSetting")
	 * @param type   {@link Class} for {@code <T>}
	 * @return configuration object bound to this {@link ConfigurationProvider}
	 * @throws NoSuchElementException   when the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException when property can't be converted to {@code type}
	 * @throws IllegalStateException    when provider is unable to fetch configuration value for the given {@code key}
	 */
	<T> T bind(String prefix, Class<T> type);

	//// GENERIC EXISTENTIAL CHECKS ////
	/**
	 * Checks if a given key exists in the config
	 *
	 * @param key The desired key in the config
	 * @return Whether the key exists
	 * @throws IllegalStateException    When provider is unable to fetch configuration
	 */
	default boolean has(String key){
		//Attempt to get the value for the given key
		try {
			//Get the value for the given key as an object
			getProperty(key, Object.class);

			//No failures, so return true
			return true;
		}
		catch(NoSuchElementException nsee){
			//Return false as an error occurred while trying to cast
			return false;
		}
	}

	/*
	 * Checks if a given key has a value that is non-null
	 *
	 * @param key The desired key in the config
	 * @return Whether the key has a non-null value
	 * @throws NoSuchElementException   When the provided {@code key} doesn't exist
	 * @throws IllegalStateException    When provider is unable to fetch configuration

	boolean hasValue(String key);
	 */

	/**
	 * Checks if a given key's value is castable to a given type
	 *
	 * @param key The desired key in the config
	 * @param type {@link Class} for {@code <T>}
	 * @param <T> The type to check for
	 * @return Whether the key's value is castable to a given type
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default <T> boolean is(String key, Class<T> type){
		//Attempt to get the value for the given key
		try {
			//Get the value for the given key as the given type
			getProperty(key, type);

			//No failures, so return true
			return true;
		}
		catch(IllegalArgumentException iae){
			//Return false as an error occurred while trying to cast
			return false;
		}
	}

	/**
	 * Checks if a given key's value is castable to an array
	 *
	 * @param key The desired key in the config
	 * @return Whether the key's value is castable to an array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isArray(String key){
		//Attempt to get the value for the given key
		try {
			//Get the value for the given key as an array of objects
			getProperty(key, Object[].class);

			//No failures, so return true
			return true;
		}
		catch(IllegalArgumentException iae){
			//Return false as an error occurred while trying to cast
			return false;
		}
	}

	/**
	 * Checks if a given key's value is castable to an array of the given type
	 *
	 * @param key The desired key in the config
	 * @param type {@link Class} for {@code <T>}
	 * @param <T> The type of array to check for
	 * @return Whether the key's value is castable to an array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default <T> boolean isArrayOf(String key, Class<T> type){
		//Check if the value is an array and the key maps to a value of the given type and return the result
		return isArray(key) && is(key, type);
	}

	/**
	 * Checks if a given key's value is castable to a generic collection
	 * (eg: LinkedList, ArrayList, etc)
	 *
	 * @param key The desired key in the config
	 * @return Whether the key's value is castable to a generic collection
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isCollection(String key){
		//Attempt to get the value for the given key
		try {
			//Get the value for the given key as a collection of objects
			getProperty(key, new GenericType<Collection<Object>>() { });

			//No failures, so return true
			return true;
		}
		catch(IllegalArgumentException iae){
			//Return false as an error occurred while trying to cast
			return false;
		}
	}

	/**
	 * Checks if a given key's value is castable to a collection of the given type
	 * (eg: LinkedList, ArrayList, etc)
	 *
	 * @param key The desired key in the config
	 * @param type {@link Class} for {@code <T>}
	 * @param <T> The type of collection to check for
	 * @return Whether the key's value is castable to a generic collection
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default <T> boolean isCollectionOf(String key, Class<T> type){
		//Check if the value is a collection and the key maps to a value of the given type and return the result
		return isCollection(key) && is(key, type);
	}

	//// EXPLICIT TYPE GETTERS & CHECKERS ////
	// PRIMITIVE TYPES //
	/**
	 * Gets a config value from its key as a primitive boolean
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a boolean
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a boolean
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean getBool(String key){ return getProperty(key, boolean.class); }

	/**
	 * Checks if a config value is a primitive boolean
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a boolean
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isBool(String key){ return is(key, boolean.class); }

	/**
	 * Gets a config value from its key as a primitive byte
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a byte
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a byte
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default byte getByte(String key){ return getProperty(key, byte.class); }

	/**
	 * Checks if a config value is a primitive byte
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a byte
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isByte(String key){ return is(key, byte.class); }

	/**
	 * Gets a config value from its key as a primitive char
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a char
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a char
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default char getChar(String key){ return getProperty(key, char.class); }

	/**
	 * Checks if a config value is a primitive char
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a char
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isChar(String key){ return is(key, char.class); }

	/**
	 * Gets a config value from its key as a primitive double
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a double
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a double
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default double getDouble(String key){ return getProperty(key, double.class); }

	/**
	 * Checks if a config value is a primitive double
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a double
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isDouble(String key){ return is(key, double.class); }

	/**
	 * Gets a config value from its key as a primitive float
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a float
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a float
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default float getFloat(String key){ return getProperty(key, float.class); }

	/**
	 * Checks if a config value is a primitive float
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a float
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isFloat(String key){ return is(key, float.class); }

	/**
	 * Gets a config value from its key as a primitive integer
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as an integer
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to an integer
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default int getInt(String key){ return getProperty(key, int.class); }

	/**
	 * Checks if a config value is a primitive integer
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is an integer
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isInt(String key){ return is(key, int.class); }

	/**
	 * Gets a config value from its key as a primitive long
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a long
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a long
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default long getLong(String key){ return getProperty(key, long.class); }

	/**
	 * Checks if a config value is a primitive long
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a long
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isLong(String key){ return is(key, long.class); }

	/**
	 * Gets a config value from its key as a primitive short
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a short
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a short
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default short getShort(String key){ return getProperty(key, short.class); }

	/**
	 * Checks if a config value is a primitive short
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a short
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isShort(String key){ return is(key, short.class); }

	// PRIMITIVE ARRAYS //
	/**
	 * Gets a config value from its key as a primitive boolean array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a boolean array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a primitive boolean array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean[] getBoolArr(String key){ return getProperty(key, boolean[].class); }

	/**
	 * Checks if a config value is a primitive boolean array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a boolean array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isBoolArr(String key){ return is(key, boolean[].class); }

	/**
	 * Gets a config value from its key as a primitive byte array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a byte array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a primitive byte array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default byte[] getByteArr(String key){ return getProperty(key, byte[].class); }

	/**
	 * Checks if a config value is a primitive byte array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a byte array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isByteArr(String key){ return is(key, byte[].class); }

	/**
	 * Gets a config value from its key as a primitive char array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a char array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a primitive char array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default char[] getCharArr(String key){ return getProperty(key, char[].class); }

	/**
	 * Checks if a config value is a primitive char array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a char array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isCharArr(String key){ return is(key, char[].class); }

	/**
	 * Gets a config value from its key as a primitive double array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a double array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a primitive double array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default double[] getDoubleArr(String key){ return getProperty(key, double[].class); }

	/**
	 * Checks if a config value is a primitive double array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a double array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isDoubleArr(String key){ return is(key, double[].class); }

	/**
	 * Gets a config value from its key as a primitive float array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a float array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a primitive float array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default float[] getFloatArr(String key){ return getProperty(key, float[].class); }

	/**
	 * Checks if a config value is a primitive float array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a float array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isFloatArr(String key){ return is(key, float[].class); }

	/**
	 * Gets a config value from its key as a primitive integer array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as an integer array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a primitive integer array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default int[] getIntArr(String key){ return getProperty(key, int[].class); }

	/**
	 * Checks if a config value is a primitive integer array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is an integer array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isIntArr(String key){ return is(key, int[].class); }

	/**
	 * Gets a config value from its key as a primitive long array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a long array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a primitive long array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default long[] getLongArr(String key){ return getProperty(key, long[].class); }

	/**
	 * Checks if a config value is a primitive long array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a long array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isLongArr(String key){ return is(key, long[].class); }

	/**
	 * Gets a config value from its key as a primitive short array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a short array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a primitive short array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default short[] getShortArr(String key){ return getProperty(key, short[].class); }

	/**
	 * Checks if a config value is a primitive short array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a short array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isShortArr(String key){ return is(key, short[].class); }

	// WRAPPED ARRAYS //
	/**
	 * Gets a config value from its key as a boolean array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a boolean array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a boolean array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Boolean[] getWBoolArr(String key){ return getProperty(key, Boolean[].class); }

	/**
	 * Checks if a config value is a boolean array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a boolean array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isWBoolArr(String key){ return is(key, Boolean[].class); }

	/**
	 * Gets a config value from its key as a byte array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a byte array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a byte array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Byte[] getWByteArr(String key){ return getProperty(key, Byte[].class); }

	/**
	 * Checks if a config value is a byte array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a byte array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isWByteArr(String key){ return is(key, Byte[].class); }

	/**
	 * Gets a config value from its key as a char array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a char array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a char array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Character[] getWCharArr(String key){ return getProperty(key, Character[].class); }

	/**
	 * Checks if a config value is a char array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a char array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isWCharArr(String key){ return is(key, Character[].class); }

	/**
	 * Gets a config value from its key as a double array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a double array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a double array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Double[] getWDoubleArr(String key){ return getProperty(key, Double[].class); }

	/**
	 * Checks if a config value is a double array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a double array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isWDoubleArr(String key){ return is(key, Double[].class); }

	/**
	 * Gets a config value from its key as a float array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a float array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a float array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Float[] getWFloatArr(String key){ return getProperty(key, Float[].class); }

	/**
	 * Checks if a config value is a float array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a float array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isWFloatArr(String key){ return is(key, Float[].class); }

	/**
	 * Gets a config value from its key as an integer array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as an integer array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to an integer array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Integer[] getWIntArr(String key){ return getProperty(key, Integer[].class); }

	/**
	 * Checks if a config value is an integer array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is an integer array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isWIntArr(String key){ return is(key, Integer[].class); }

	/**
	 * Gets a config value from its key as a long array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a long array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a long array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Long[] getWLongArr(String key){ return getProperty(key, Long[].class); }

	/**
	 * Checks if a config value is a long array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a long array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isWLongArr(String key){ return is(key, Long[].class); }

	/**
	 * Gets a config value from its key as a short array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a short array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a short array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Short[] getWShortArr(String key){ return getProperty(key, Short[].class); }

	/**
	 * Checks if a config value is a short array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a short array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isWShortArr(String key){ return is(key, Short[].class); }

	// BIGDECIMAL, BIGINTEGER, AND NUMBER //
	/**
	 * Gets a config value from its key as a BigDecimal
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a BigDecimal
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a BigDecimal
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default BigDecimal getBigDecimal(String key){ return getProperty(key, BigDecimal.class); }

	/**
	 * Checks if a config value is a BigDecimal
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a BigDecimal
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isBigDecimal(String key){ return is(key, BigDecimal.class); }

	/**
	 * Gets a config value from its key as a BigDecimal array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a BigDecimal array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a BigDecimal array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default BigDecimal[] getBigDecimalArr(String key){ return getProperty(key, BigDecimal[].class); }

	/**
	 * Checks if a config value is a BigDecimal array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a BigDecimal array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isBigDecimalArr(String key){ return is(key, BigDecimal[].class); }

	/**
	 * Gets a config value from its key as a BigInteger
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a BigInteger
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a BigInteger
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default BigInteger getBigInteger(String key){ return getProperty(key, BigInteger.class); }

	/**
	 * Checks if a config value is a BigInteger
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a BigInteger
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isBigInteger(String key){ return is(key, BigInteger.class); }

	/**
	 * Gets a config value from its key as a BigInteger array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a BigInteger array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a BigInteger array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default BigInteger[] getBigIntegerArr(String key){ return getProperty(key, BigInteger[].class); }

	/**
	 * Checks if a config value is a BigInteger array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a BigInteger array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isBigIntegerArr(String key){ return is(key, BigInteger[].class); }

	/**
	 * Gets a config value from its key as a generic number
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a generic number
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a generic number
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Number getNumber(String key){ return getProperty(key, Number.class); }

	/**
	 * Checks if a config value is a generic number
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a generic number
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isNumber(String key){ return is(key, Number.class); }

	/**
	 * Gets a config value from its key as a generic number array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a generic number array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a generic number array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Number[] getNumberArr(String key){ return getProperty(key, Number[].class); }

	/**
	 * Checks if a config value is a generic number array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a generic number array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isNumberArr(String key){ return is(key, Number[].class); }

	// STRING //
	/**
	 * Gets a config value from its key as a String
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a String
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a String
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default String getString(String key){ return getProperty(key, String.class); }

	/**
	 * Checks if a config value is a String
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a String
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isString(String key){ return is(key, String.class); }

	/**
	 * Gets a config value from its key as a String array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a String array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a String array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default String[] getStringArr(String key){ return getProperty(key, String[].class); }

	/**
	 * Checks if a config value is a String array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a String array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isStringArr(String key){ return is(key, String[].class); }

	// URI AND URL //
	/**
	 * Gets a config value from its key as a URI
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a URI
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a URI
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default URI getURI(String key){ return getProperty(key, URI.class); }

	/**
	 * Checks if a config value is a URI
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a URI
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isURI(String key){ return is(key, URI.class); }

	/**
	 * Gets a config value from its key as a URI array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a URI array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a URI array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default URI[] getURIArr(String key){ return getProperty(key, URI[].class); }

	/**
	 * Checks if a config value is a URI array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a URI array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isURIArr(String key){ return is(key, URI[].class); }

	/**
	 * Gets a config value from its key as a URL
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a URL
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a URL
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default URL getURL(String key){ return getProperty(key, URL.class); }

	/**
	 * Checks if a config value is a URL
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a URL
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isURL(String key){ return is(key, URL.class); }

	/**
	 * Gets a config value from its key as a URL array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a URL array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a URL array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default URL[] getURLArr(String key){ return getProperty(key, URL[].class); }

	/**
	 * Checks if a config value is a URL array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a URL array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isURLArr(String key){ return is(key, URL[].class); }

	// FILE AND PATH //
	/**
	 * Gets a config value from its key as a File
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a File
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a File
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default File getFile(String key){ return getProperty(key, File.class); }

	/**
	 * Checks if a config value is a File
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a File
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isFile(String key){ return is(key, File.class); }

	/**
	 * Gets a config value from its key as a File array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a File array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a File array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default File[] getFileArr(String key){ return getProperty(key, File[].class); }

	/**
	 * Checks if a config value is a File array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a File array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isFileArr(String key){ return is(key, File[].class); }

	/**
	 * Gets a config value from its key as a Path
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a Path
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a Path
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Path getPath(String key){ return getProperty(key, Path.class); }

	/**
	 * Checks if a config value is a Path
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a Path
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isPath(String key){ return is(key, Path.class); }

	/**
	 * Gets a config value from its key as a Path array
	 *
	 * @param key The desired key in the config
	 * @return The value associated with the given key as a Path array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalArgumentException When property can't be converted to a Path array
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default Path[] getPathArr(String key){ return getProperty(key, Path[].class); }

	/**
	 * Checks if a config value is a Path array
	 *
	 * @param key The desired key in the config
	 * @return Whether the config value is a Path array
	 * @throws NoSuchElementException   When the provided {@code key} doesn't have a corresponding config value
	 * @throws IllegalStateException    When provider is unable to fetch configuration value for the given {@code key}
	 */
	default boolean isPathArr(String key){ return is(key, Path[].class); }
}
