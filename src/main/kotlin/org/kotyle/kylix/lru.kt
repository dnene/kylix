package org.kotyle.kylix.lru

import java.util.*
import java.util.function.Function

open class LRUMap<K,V>(val maxSize: Int,val fn: Function<in K, out V>?=null): LinkedHashMap<K,V>(16, 0.75F, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?) = size > maxSize
    override fun get(k: K): V? = if (fn != null) super.computeIfAbsent(k,fn) else super.get(k)
    override fun computeIfAbsent(key: K, mappingFunction: Function<in K, out V>?): V {
        return super.computeIfAbsent(key, mappingFunction)
    }
}

fun <K,V> Collection<Pair<K,V>>.toLruMap(maxSize: Int=50,fn: Function<in K, out V>?=null,sync: Boolean = false):  Map<K,V> =
    this.fold(LRUMap<K,V>(maxSize,fn)) { acc, elem ->
        acc.apply { put(elem.first, elem.second) }
    }.let { if (sync) Collections.synchronizedMap(it) else it}
