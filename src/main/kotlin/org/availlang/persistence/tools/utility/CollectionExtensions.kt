package org.availlang.persistence.tools.utility

/**
 * Transform the receiver via the supplied function and collect the results into
 * an optionally provided set. Answer the result set.
 *
 * @param T
 *   The element type of the incoming [Iterable].
 * @param R
 *   The element type of the outgoing [Set].
 * @param destination
 *   The destination [MutableSet]. Defaults to [mutableSetOf].
 * @param transform
 *   The function to map keys to values.
 * @return
 *   The resultant [MutableSet].
 */
inline fun <T, R> Iterable<T>.mapToSet (
	destination: MutableSet<R> = mutableSetOf(),
	transform: (T) -> R
) : MutableSet<R> = mapTo(destination, transform)

/**
 * Project the receiver onto an {@link EnumMap}, applying the function to each
 * enum value of the array.
 *
 * @param K
 *   The key type, an [Enum].
 * @param V
 *   The value type produced by the function.
 * @param generator
 *   The function to map keys to values.
 * @return
 *   A fully populated [EnumMap].
 */
inline fun <K : Enum<K>, V: Any> Array<K>.toEnumMap (
	generator: (K) -> V
) : EnumMap<K, V>
{
	val map = EnumMap<K, V>(this)
	this.forEach { key -> map[key] = generator(key) }
	return map
}
