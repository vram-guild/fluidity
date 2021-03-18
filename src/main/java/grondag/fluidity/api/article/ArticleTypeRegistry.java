/*******************************************************************************
 * Copyright 2019, 2020 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.api.article;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.Identifier;

import grondag.fluidity.impl.article.ArticleTypeRegistryImpl;

/**
 * Registry for {@code ArticleType} instances.
 * Registration should occur once during mod initialization.
 */
@Experimental
public interface ArticleTypeRegistry {
	/**
	 * Return the registry instance.
	 *
	 * @return the registry instance
	 */
	static ArticleTypeRegistry instance() {
		return ArticleTypeRegistryImpl.INSTANCE;
	}

	/**
	 * Find an article type by name-spaced ID
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param id name-spaced identifier
	 * @return The {@code ArticleType} associated with the given ID, or {@link ArticleType#NOTHING} if not found.
	 */
	<T> ArticleType<T> get(Identifier id);

	/**
	 * Like {@link #get(Identifier)} but accepts a namespace:id {@code String}.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param idString namespace:id {@code String} identifying the article type
	 * @return The {@code ArticleType} associated with the given string, or {@link ArticleType#NOTHING} if not found.
	 */
	<T> ArticleType<T> get(String idString);

	/**
	 * Find an article type using a raw integer index obtained earlier via {@link #getRawId(ArticleType)}.
	 * As with other registries, these "raw" integer IDs can change from session to session and
	 * should not be used to save world state. They are mainly useful for packet encoding.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param index integer raw ID of the article type, obtained earlier via {@link #getRawId(ArticleType)}.
	 * @return The {@code ArticleType} associated with the given string, or {@link ArticleType#NOTHING} if not found.
	 */
	<T> ArticleType<T> get(int index);

	/**
	 * Find the name-spaced identifier associated with the given instance.
	 * These identifiers are static across game sessions and suitable for persisting game state.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param articleType The instance for which an ID will be returned
	 * @return The name-spaced identifier associated with the given instance
	 */
	<T> Identifier getId(ArticleType<T> articleType);

	/**
	 * Find the "raw" integer index associated with the given instance.
	 * This index can change across game sessions and should not be used for persistence.
	 * The main use of raw integer index is packet serialization.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param articleType The instance for which an integer index will be returned
	 * @return The integer index associated with the given instance
	 */
	<T> int getRawId(ArticleType<T> articleType);

	/**
	 * Iterate all instances currently in the registry and apply the given consumer.
	 *
	 * @param consumer Consumer to apply to all registry entries.
	 */
	void forEach(Consumer<? super ArticleType<?>> consumer);

	/**
	 * Test if the registry contains an instance associated with the given name-spaced identifier.
	 *
	 * @param id The name-spaced identifier to be tested.
	 * @return {@code true} if the registry contains an instance associated with the given ID
	 */
	boolean contains(Identifier id);

	/**
	 * Associates an {@code ArticleType} instance with a name-spaced identifier,
	 * adding it to this registry. Call once during game initialization.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param id The name-spaced identifier for the instance being added
	 * @param articleType The instance being added
	 * @return The instance that was added
	 */
	<T> ArticleType<T> add(Identifier id, ArticleType<T> articleType);

	/**
	 * A version of {@link #add(Identifier, ArticleType)} that accepts a namespace:id {@code String}.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param id A namespaced:id {@code String} identifying the instance being added
	 * @param articleType The instance being added
	 * @return The instance that was added
	 */
	default <T> ArticleType<T> add(String idString, ArticleType<T> articleType) {
		return add(new Identifier(idString), articleType);
	}
}
