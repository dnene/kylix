package org.kotyle.kylix

import org.junit.Test
import org.junit.Assert.*
import org.kotyle.kylix.clist.CList
import org.kotyle.kylix.clist.asList

class CListTest {
    @Test
    fun construction() {
        assertEquals(CList.Nil,CList<Int>())
        assertEquals(listOf<Int>(1,2,3,4,5), CList<Int>(1,2,3,4,5).asList())
    }

    @Test
    fun drop() {
        assertEquals(listOf(4,5,6), CList(1,2,3,4,5,6).drop(3).asList())
    }

    @Test
    fun example316() {
        fun CList<Int>.increment(): CList<Int> = when (this) {
            is CList.Nil -> CList.Nil
            is CList.Cons -> CList.Cons<Int>(this.head + 1, this.tail.increment())
        }

        assertEquals(CList(1,2,3,4,5), CList(0,1,2,3,4).increment())
    }

    @Test
    fun example318() {
        assertEquals(CList<String>("2","4","6"), CList<Int>(1,2,3).map { (it * 2).toString()})
    }


    @Test
    fun example319() {
        assertEquals(CList<Int>(2), CList<Int>(1,2,3).filter { it %2 == 0})
    }

    @Test
    fun example320() {
        assertEquals(CList(1,2,1,2,4,4,3,6,9), CList<Int>(1,2,3).flatMap { CList<Int>(it, 2*it,it * it) })
    }

    @Test
    fun example321() {
        assertEquals(CList<Int>(2), CList<Int>(1,2,3).flatMap<Int> { if (it %2 == 0) CList<Int>(it) else CList.Nil })
    }

}