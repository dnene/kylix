package org.kotyle.kylix.collections

import java.util.*

fun <T> Enumeration<T>.asSequence(): Sequence<T> =
        object:Sequence<T> {
            override fun iterator(): Iterator<T> =
                    object:Iterator<T> {
                        override fun hasNext(): Boolean = this@asSequence.hasMoreElements()
                        override fun next(): T = this@asSequence.nextElement()
                    }
        }

