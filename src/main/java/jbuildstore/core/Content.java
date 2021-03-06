// Copyright 2021 David James Pearce
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package jbuildstore.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides various interfaces for mapping the structured content held in memory
 * to unstructured content held on disk. For example, we can define sources of
 * structured content from directories or compressed archives.
 *
 * @author David J. Pearce
 *
 */
public interface Content {

	/**
	 * Get the content type associated with this piece of content.
	 *
	 * @return
	 */
	public Content.Type<?> contentType();

	/**
	 * Identifies a given piece of content in a store of some kind, and provides an
	 * API for reading it.
	 *
	 * @author David J. Pearce
	 *
	 * @param <K>
	 */
	public interface Entry<S> {
		/**
		 * Get the identifying key for this particular piece of content.
		 *
		 * @return
		 */
		public Key<S,?> getKey();

		/**
		 * Read this particular piece of content.
		 *
		 * @param <T>
		 * @param kind
		 * @return
		 */
		public Content get();
	}

	/**
	 * Provides an abstract mechanism for reading and writing file in
	 * a given format. Whiley source files (*.whiley) are one example, whilst JVM
	 * class files (*.class) are another.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public interface Type<T extends Content> {
		/**
		 * Physically read the raw bytes from a given input stream and convert into the
		 * format described by this content type.
		 *
		 * @param input    Input stream representing in the format described by this
		 *                 content type.
		 * @param registry Content registry to be used for creating content within the
		 *                 given type.
		 * @return
		 */
		public T read(InputStream input) throws IOException;

		/**
		 * Convert an object in the format described by this content type into
		 * an appropriate byte stream and write it to an output stream
		 *
		 * @param output
		 *            --- stream which this value is to be written to.
		 * @param value
		 *            --- value to be converted into bytes.
		 */
		public void write(OutputStream output, T value) throws IOException;

		/**
		 * Return an appropriate suffix for this content type. This is used to identify
		 * instances stored on disk (for example).
		 *
		 * @return
		 */
		public String suffix();
	}

	/**
	 * Provides a general mechanism for reading content from a given source.
	 *
	 * @author David J. Pearce
	 *
	 */
	public interface Source<K> {
		/**
		 * Get a given piece of content from this source.
		 *
		 * @param <T>
		 * @param kind
		 * @param key
		 * @return
		 */
		public <T extends Content> T get(Key<K,T> key) throws IOException;

		/**
		 * Get a given piece of content from this source.
		 *
		 * @param <T>
		 * @param kind
		 * @param p
		 * @return
		 */
		public <T extends Content> List<T> getAll(Predicate<Key<K,?>> query) throws IOException;

		/**
		 * Find all content matching a given filter.
		 *
		 * @param <S>
		 * @param kind
		 * @param f
		 * @return
		 */
		public <T extends Content> List<Key<K, T>> match(Predicate<Key<K,?>> query);
	}

	/**
	 * Provides a general mechanism for writing content into a given source.
	 *
	 * @author David J. Pearce
	 *
	 */
	public interface Sink<S> {
		/**
		 * Write a given piece of content into this sink.
		 *
		 * @param <T>
		 * @param kind
		 * @param key
		 * @param value
		 */
		public <T extends Content> void put(Key<S,T> key, T value);

		/**
		 * Remove a given piece of content from this sink.
		 * @param key
		 */
		public void remove(Key<S, ?> key);
	}

	/**
	 * A content store represents an interface to an underlying medium (e.g. the file
	 * system). As such it provides both read and write access, along with the
	 * ability for synchronisation.
	 *
	 * @author David J. Pearce
	 *
	 */
	public interface Store<K> extends Source<K>, Sink<K> {
		/**
		 * Synchronise this root against the underlying medium. This does two things. It
		 * flushes writes and invalidates items which have changed on disk. Invalidate
		 * items will then be reloaded on demand when next requested.
		 */
		public void synchronise() throws IOException;
	}
}
