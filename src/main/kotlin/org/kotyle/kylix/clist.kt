package org.kotyle.kylix.clist

sealed class CList<out T> {
    companion object {
        operator fun <T> invoke(vararg ts: T): CList<T> =
                ts.asList().foldRight(Nil) { t: T, acc: CList<T> -> Cons<T>(t, acc)}
    }
    abstract fun drop(n: Int): CList<T>
    abstract fun dropWhile(pred: (T) -> Boolean): CList<T>
    abstract fun <R> map(fn: (T) -> R): CList<R>
    abstract fun filter(fn: (T) -> Boolean): CList<T>
    abstract fun <R> flatMap(fn: (T) -> CList<R>): CList<R>

    object Nil: CList<Nothing>() {
        override fun drop(n: Int): CList<Nothing> { if (n > 0) throw IllegalStateException("drop accessed on a Nil") else return this}
        override fun dropWhile(pred: (Nothing) -> Boolean): CList<Nothing> = this
        override fun equals(other: Any?): Boolean = other is Nil
        override fun toString(): String = "Nil"
        override fun <R> map(fn: (Nothing) -> R): CList<R> = this
        override fun filter(fn: (Nothing) -> Boolean): CList<Nothing> = this
        override fun <R> flatMap(fn: (Nothing) -> CList<R>): CList<R> = this
    }
    class Cons<T>(val head: T, val tail: CList<T>): CList<T>() {
        override fun drop(n: Int) = if (n > 0) tail.drop(n-1) else this
        override fun dropWhile(pred: (T) -> Boolean): CList<T> = if (pred(head)) tail.dropWhile(pred) else this
        override fun equals(other: Any?): Boolean = (other is Cons<*> && this.head == other.head && this.tail.equals(other.tail))
        override fun toString():String {
            fun toString(me: CList<T>, b: StringBuilder): StringBuilder {
                when(me) {
                    is Nil -> { b.append(")"); return b}
                    is Cons -> { b.append(me.head); b.append(","); return toString(me.tail, b)}
                }
            }
            val b = StringBuilder("CList(")
            return toString(this, b).toString()
        }
        override fun <R> map(fn: (T) -> R): CList<R> = Cons<R>(fn(head), if (tail !is Nil) tail.map(fn) else Nil)
        override fun filter(fn: (T) -> Boolean): CList<T> = if (fn(head)) Cons(head, tail.filter(fn)) else tail.filter(fn)
        override fun <R> flatMap(fn: (T) -> CList<R>): CList<R> {
            fun merge(fn: (T) -> CList<R>, first: CList<R>, second: CList<T>): CList<R> {
                return when(first) {
                    is Cons -> Cons<R>(first.head, merge(fn, first.tail, second))
                    is Nil -> when (second) {
                        is Cons -> merge(fn, fn(second.head), second.tail)
                        is Nil -> Nil
                    }
                }
            }
            return merge(fn,fn(head),tail)
        }
    }
}

fun <T> CList<T>.asList(): List<T> {
    fun <T> CList<T>.asList(l: List<T>): List<T> =
            when (this) {
                is CList.Cons -> this.tail.asList(l + head)
                is CList.Nil -> l
            }
    return this.asList(listOf())
}

fun <T> CList<T>.append(l: CList<T>): CList<T> =
        when(this) {
            is CList.Cons -> CList.Cons<T>(this.head, tail.append(l))
            is CList.Nil -> l
        }
