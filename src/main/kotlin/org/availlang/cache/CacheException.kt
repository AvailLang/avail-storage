package org.availlang.cache

/**
 * An abstract [RuntimeException] specific for use with the [LRUCache].
 *
 * @author Richard Arriaga
 */
sealed class CacheException: RuntimeException
{
	/**
	 * Create a [CacheException].
	 *
	 * @param e
	 *   The [cause] of this `CacheException`.
	 */
	constructor(e: RuntimeException): super(e)

	/**
	 * Create a [CacheException].
	 */
	constructor(): super()
}

/**
 * A [CacheException] that is used to wrap an exception raised during the run of
 * the [LRUCache.transformer] to calculate a value to store in the cache.
 *
 * @author Richard Arriaga
 */
class CacheInsertException constructor(e: RuntimeException): CacheException(e)
