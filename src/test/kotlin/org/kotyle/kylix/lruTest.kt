package org.kotyle.kylix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotyle.kylix.lru.LRUMap
import org.kotyle.kylix.lru.toLruMap
import java.util.function.Function

fun triple(n: Int) = 3 * n

class LRUTest {
    @Test
    fun checkMaxSize() {
        val maxSize = 10
        val maxKey = 20
        val nextAdditions = 3
        val newMap = (1..maxKey).map { Pair(it,it)}.toLruMap(maxSize)
        assertEquals("Map should have maxsize elements at max", maxSize, newMap.size)
        assertTrue("Map should've retained most recent insertions", newMap.all { it.key > maxKey - maxSize} && newMap.all { it.key <= maxKey})
        // force access
        (1..nextAdditions).forEach { newMap.get(maxKey-maxSize+it) }
        // add more entries
        (1..nextAdditions).forEach { (newMap as LRUMap).put(maxKey + it, maxKey + it) }
        assertTrue("Map should've retained most recent accesses", newMap.all {
            ((it.key > maxKey-maxSize) && (it.key <= maxKey-maxSize+nextAdditions)) ||
                    ((it.key > maxKey-maxSize+(2*nextAdditions)) && (it.key <= maxKey + nextAdditions))})

    }

    @Test
    fun checkDefaultingLRUMap() {
        val maxSize = 10
        val maxKey = 20
        val nextAdditions = 3
        val map =  LRUMap<Int,Int>(maxSize, Function(::triple))
        (1..maxKey).forEach { map.get(it)}
        assertEquals("Map should have maxsize elements at max", maxSize, map.size)
        assertTrue("Map should've retained most recent insertions", map.all { it.key > maxKey - maxSize} && map.all { it.key <= maxKey})
        // force access
        (1..nextAdditions).forEach { map.get(maxKey-maxSize+it) }
        // add more entries
        (1..nextAdditions).forEach { map.get(maxKey + it) }
        println(map)
        assertTrue("Map should've retained most recent accesses", map.all {
            ((it.key > maxKey-maxSize) && (it.key <= maxKey-maxSize+nextAdditions)) ||
                    ((it.key > maxKey-maxSize+(2*nextAdditions)) && (it.key <= maxKey + nextAdditions))})

    }
}