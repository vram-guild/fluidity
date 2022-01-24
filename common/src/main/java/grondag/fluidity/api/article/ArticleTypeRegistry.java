/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.api.article;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.resources.ResourceLocation;

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
	 * Find an article type by name-spaced ID.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param id name-spaced identifier
	 * @return The {@code ArticleType} associated with the given ID, or {@link ArticleType#NOTHING} if not found.
	 */
	<T> ArticleType<T> get(ResourceLocation id);

	/**
	 * Like {@link #get(ResourceLocation)} but accepts a namespace:id {@code String}.
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
	<T> ResourceLocation getId(ArticleType<T> articleType);

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
	boolean contains(ResourceLocation id);

	/**
	 * Associates an {@code ArticleType} instance with a name-spaced identifier,
	 * adding it to this registry. Call once during game initialization.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param id The name-spaced identifier for the instance being added
	 * @param articleType The instance being added
	 * @return The instance that was added
	 */
	<T> ArticleType<T> add(ResourceLocation id, ArticleType<T> articleType);

	/**
	 * A version of {@link #add(ResourceLocation, ArticleType)} that accepts a namespace:id {@code String}.
	 *
	 * @param <T> {@code Class} of the game resource the {@code ArticleType} represents
	 * @param id A namespaced:id {@code String} identifying the instance being added
	 * @param articleType The instance being added
	 * @return The instance that was added
	 */
	default <T> ArticleType<T> add(String idString, ArticleType<T> articleType) {
		return add(new ResourceLocation(idString), articleType);
	}
}
