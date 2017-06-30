package org.kotyle.kylix.lru

import java.util.*
import java.util.function.Function

open class LRUMap<K,V>(val maxSize: Int, val fn: ((K) -> V?)): LinkedHashMap<K,V?>(16, 0.75F, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V?>?) = size > maxSize
    override fun get(key: K): V? = if (fn != null) super.computeIfAbsent(key,Function(fn)) else super.get(key)
}

fun <K,V> Collection<Pair<K,V>>.toLruMap(maxSize: Int=50, fn: ((K) -> V?), sync: Boolean = false):  Map<K,V?> =
        this.fold(LRUMap<K,V>(maxSize,fn)) { acc, elem ->
            acc.apply { put(elem.first, elem.second) }
        }.let { if (sync) Collections.synchronizedMap(it) else it}
