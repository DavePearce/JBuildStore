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

import jbuildstore.core.Content.Type;

/**
 * Minimal requirements for a content key.
 *
 * @author David J. Pearce
 *
 * @param <T>
 */
public interface Key<S, T extends Content> {
	/**
	 * Get the identifying component of this key.
	 *
	 * @return
	 */
	public S id();

	/**
	 * Get the content type encoded by this key.
	 *
	 * @return
	 */
	public Content.Type<T> contentType();

	/**
	 * A key mapping provides a mechanism for mapping between lowlevel key
	 * representations (e.g. filenames) and instances of <code>Key</code>.
	 *
	 * @author David J. Pearce
	 *
	 * @param <K>
	 * @param <V>
	 */
	public interface Map<K, V> {
		/**
		 * Encode a given content type and key into a low-level type (e.g. a filename).
		 *
		 * @param s
		 * @return
		 */
		public V encode(Key<K, ?> key);

		/**
		 * Decode a low-level type (e.g. a filename) into a key. In the context of a
		 * filename, for example, this might return its path.
		 *
		 * @param t
		 * @return
		 */
		public Key<K, ?> decode(V t);
	}

	/**
	 * A default implementation of <code>Content.Key</code> which wraps some
	 * existing type (e.g. <code>String</code>) up with a corresponding
	 * <code>Content.Type</code>.
	 *
	 * @author David J. Pearce
	 *
	 * @param <S>
	 * @param <T>
	 */
	public static class Pair<S, T extends Content> implements Key<S, T> {
		private final S id;
		private final Content.Type<T> contentType;

		public Pair(S key, Content.Type<T> contentType) {
			if (key == null) {
				throw new IllegalArgumentException("identifier cannot be null");
			} else if (contentType == null) {
				throw new IllegalArgumentException("content type cannot be null");
			}
			this.contentType = contentType;
			this.id = key;
		}

		@Override
		public S id() {
			return id;
		}

		@Override
		public Type<T> contentType() {
			return contentType;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Key) {
				Key<?, ?> k = (Key<?, ?>) o;
				return contentType.equals(k.contentType()) && id.equals(k.id());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return contentType.hashCode() ^ id.hashCode();
		}

		@Override
		public String toString() {
			return id.toString() + ":" + contentType.toString();
		}
	}
}
