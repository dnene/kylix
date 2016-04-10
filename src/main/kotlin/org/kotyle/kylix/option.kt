/*
Copyright 2016 Dhananjay Nene

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*

 */

package org.kotyle.kylix.option

import org.kotyle.kylix.helpers.EmptyIterator
import org.kotyle.kylix.helpers.SingleItemIterator

/**
 * Option indicates a value which might be available or unavailable (present or absent)
 *
 *
 * Thanks in no small measure based on information from these and other helpful sources
 * http://blog.tmorris.net/posts/scalaoption-cheat-sheet/
 * https://github.com/MarioAriasC/funKTionale/blob/master/src/main/kotlin/org/funktionale/option/Option.kt
 * http://blog.originate.com/blog/2014/06/15/idiomatic-scala-your-options-do-not-match/
 */
sealed class Option<out T>: Collection<T> {
    companion object {
        /**
         * Construction helper.
         *
         * @return A None if the provided value is a null, else the value wrapped by a Some
         */
        operator fun<T> invoke(t: T?): Option<T> = if (t == null) None else Some(t)

        /**
         * Wrap the provided function invocation with a try block to silently consume any exceptions being raised
         * and returning a none instead.
         *
         * @return none in case of exception being raised else the return value converted to an option
         *
         * @note If the function invocation results in a null being returned, this will return a None
         */
        fun <T> doTry(f: () -> T): Option<T> {
            return try {
                f().toOption()
            } catch (e: Exception) {
                None
            }
        }
    }

    /**
     *  Is this value defined (or available or present) ?
     *
     *  @return true if this is a Some, else false (ie. if it is a None)
     */
    abstract fun isDefined(): Boolean

    /**
     * Convert this into an instance of a nullable type
     *
     * @return a null if this is a None, else a non null value
     */
    abstract fun orNull(): T?

    /**
     * Filter this value based on the provided predicate
     *
     *   if this is None -> None
     *
     *   if this is a Some and predicate evaluates to true -> this
     *
     *   if this is a Some and predicate evaluates to false -> None
     *
     */
    abstract fun filter(p: (T) -> Boolean): Option<T>

    /**
     * Filter this value based on the inverse of the provided predicate
     *
     *   if this is None -> None
     *
     *   if this is defined and predicate evaluates to true -> None
     *
     *   if this is defined and predicate evaluates to false -> this
     */
    abstract fun filterNot(p: (T) -> Boolean): Option<T>

    /**
     * Check if this value matches the provided predicate
     *
     *   if this is None -> false
     *
     *   if this is defined and predicate evaluates to true - > true
     *
     *   if this is defined and predicate evaluates to false -> false
     */
    abstract fun exists(p: (T) -> Boolean): Boolean

    /**
     * Perform the map operation over the wrapped value and return the result wrapped as an option
     *
     *   if this is a None -> None
     *
     *   if this is defined and wraps a value v -> fn(v).toOption()
     */
    abstract fun <R> map(fn: (T) -> R): Option<R>

    /**
     * Perform the map operation over this wrapped value and another optional one if both are defined
     * Will return a None if either of the two are not defined
     */
    abstract fun<P,R> map(p: Option<P>, f: (T, P) -> R): Option<R>

    /**
     * Perform the map operation over this wrapped value and two other optional values if all are defined
     * Will return a None if either or more of the three are not defined
     */
    abstract fun<P,Q,R> map(p: Option<P>, q: Option<Q>, f: (T, P, Q) -> R): Option<R>

    /**
     * Perform a flatMap operation over the wrapped value (which returns an option) and return the result flattened
     *
     *   if this is a None -> None
     *
     *   if this is defined and wraps a value v -> fn(v)
     */
    abstract fun <R> flatMap(fn: (T) -> Option<R>): Option<R>

    /**
     * Compute a value using the provided function if this is defined else default to
     * using the function which generates the default value
     */
    abstract fun <R> fold(default: () -> R, operation: (T) -> R): R

    /**
     * Compute a value using the provided function if this is defined else default to the initial value
     */
    abstract fun <R> fold(default: R, operation: (T) -> R): R

    /**
     * Inverse of is this value defined. Required for implementation as a collection
     */
    override fun isEmpty(): Boolean = ! isDefined()

    object None: Option<Nothing>() {
        override val size = 0

        override fun toString(): String = "None"
        override fun equals(other: Any?) = (other is None)
        override fun contains(element: Nothing): Boolean = false
        override fun containsAll(elements: Collection<Nothing>): Boolean = elements.isEmpty()
        override fun iterator(): Iterator<Nothing> = EmptyIterator
        override fun isDefined() = false
        override fun orNull(): Nothing? = null
        override fun filter(p: (Nothing) -> Boolean): Option<Nothing> = None
        override fun filterNot(p: (Nothing) -> Boolean): Option<Nothing> = None
        override fun exists(p: (Nothing) -> Boolean): Boolean = false
        override fun <R> map(fn: (Nothing) -> R): Option<R> = None
        override fun <R> flatMap(fn: (Nothing) -> Option<R>): Option<R> = None
        override fun <R> fold(default: () -> R, operation: (Nothing) -> R): R = default()
        override fun <R> fold(default: R, operation: (Nothing) -> R): R = default
        override fun<P,R> map(p: Option<P>, f: (Nothing, P) -> R): Option<R> = None
        override fun<P,Q,R> map(p: Option<P>, q: Option<Q>, f: (Nothing, P, Q) -> R): Option<R> = None
    }
    class Some<T>(val t: T) : Option<T>() {
        init {
            if (t == null) throw IllegalArgumentException("null value passed to constructor of Some")
        }

        override fun toString(): String = "Some(${t})"
        override val size = 1
        override fun equals(other: Any?) = (other is Some<*> && t!!.equals(other.t))
        override fun contains(element: T): Boolean = t == element
        override fun containsAll(elements: Collection<T>): Boolean = !elements.any { it != t }
        override fun iterator(): Iterator<T> = SingleItemIterator(t)
        override fun isDefined() = true
        override fun orNull(): T? = t
        override fun filter(p: (T) -> Boolean): Option<T> = if (p(t)) this else None
        override fun filterNot(p: (T) -> Boolean): Option<T> = if (p(t)) None else this
        override fun exists(p: (T) -> Boolean): Boolean = p(t)
        override fun <R> map(fn: (T) -> R): Option<R> = Some(fn(t))
        override fun <R> flatMap(fn: (T) -> Option<R>): Option<R> {
            val result = fn(t)
            return when(result) {
                is Some -> Some(result.t)
                is None -> None
            }
        }
        override fun <R> fold(default: () -> R, operation: (T) -> R): R = operation(t)
        override fun <R> fold(default: R, operation: (T) -> R): R = operation(t)
        override fun<P,R> map(p: Option<P>, f: (T, P) -> R): Option<R> = flatMap { t -> p.map { pp -> f(t,pp)} }
        override fun<P,Q,R> map(p: Option<P>, q: Option<Q>, f: (T, P, Q) -> R): Option<R> =
                flatMap { t -> p.flatMap { pp -> q.map { qq -> f(t,pp,qq)}}}
    }
}

/**
 * Get the intrinsic value wrapped by this instance if defined, or the alternative as supplied
 */
fun<T> Option<T>.getOrElse(t: T): T = if (isDefined()) (this as Option.Some<T>).t else t
/**
 * Get the intrinsic value wrapped by this instance if defined, or the result of evaluation of the alternative function
 */
fun<T> Option<T>.getOrElse(f: () -> T): T = if (isDefined()) (this as Option.Some<T>).t else f()

/**
 * Return this instance if defined, or alternatively the default specified
 */
fun<T> Option<T>.orElse(t: Option<T>): Option<T> = if (isDefined()) this else t
/**
 * Return this instance if defined, or the result of the evaluation of the alternative function provided
 */
fun<T> Option<T>.orElse(f: () -> Option<T>): Option<T> = if (isDefined()) this else f()

/**
 * Return this instance if defined, or alternatively the default specified
 */
infix fun<T> Option<T>.or(t: Option<T>): Option<T> = if (isDefined()) this else t
/**
 * Return this instance if defined, or the result of the evaluation of the alternative function provided
 */
infix fun<T> Option<T>.or(f: () -> Option<T>): Option<T> = if (isDefined()) this else f()

/**
* Helper function to convert any nullable type into an option
 */
fun<T> T?.toOption(): Option<T> = if (this == null) Option.None else Option(this)

/* Very nice optional get on Map courtesy
 * https://github.com/MarioAriasC/funKTionale/blob/master/src/main/kotlin/org/funktionale/option/Option.kt */

interface Getter<K, V> {
    val getter: (K) -> V
    operator fun get(key: K): V = getter(key)
}

class GetterImpl<K, V>(override val getter: (K) -> V) : Getter<K, V>

val<K,V> Map<K,V>.optional: Getter<K, Option<V>>
    get () = GetterImpl { k -> this[k].toOption()}
