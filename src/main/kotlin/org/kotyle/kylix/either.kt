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
**  Thanks in no small measure due to
**  http://blog.tmorris.net/posts/scalaoption-cheat-sheet/
**  https://github.com/MarioAriasC/funKTionale/blob/master/src/main/kotlin/org/funktionale/option/Option.kt
**  http://blog.originate.com/blog/2014/06/15/idiomatic-scala-your-options-do-not-match/
 */

package org.kotyle.kylix.either

import org.kotyle.kylix.helpers.EmptyIterator
import org.kotyle.kylix.helpers.SingleItemIterator
import org.kotyle.kylix.option.Option
import org.kotyle.kylix.option.Option.None
import org.kotyle.kylix.option.Option.Some

/**
 * A interface for specifying handler functions for both left and right results of an Either
 */
interface EitherHandler<L,R,T> {
    /**
     * This method is invoked if the result is a left
     *
     * @return : The computed value after the function is invoked consuming the left
     */
    fun onLeft(l: Either.Left<L, R>): T

    /**
     * This method is invoked if the result is a left
     *
     * @return : The computed value after the function is invoked consuming the right
     */
    fun onRight(r: Either.Right<L,R>): T
}

/**
 * An Either is a class which represents a result that is one amongst two possible types. Thus at any point in time
 * a value for only one of the two will be defined (and conversely will not be defined for the other type)
 *
 * These are referred to as a Left or a Right (since there is no defining characteristic)
 *
 * By convention this is used to represent either an error or successful computation. In such a situation
 * the convention is that the Left type is used to represent the error type while the Right type is the type
 * of a successful computation
 */
sealed class Either<out L, out R> protected constructor () {
    /**
     * Helper for class level factory methods
     */
    companion object {
        /**
         * Factory method to create a value of the Left type
         */
        fun <L, R> toLeft(value: L) = Left<L, R>(value)

        /**
         * Factory method to create a value of the Right type
         */
        fun <L, R> toRight(value: R) = Right<L, R>(value)
    }

    /**
     *  Returns a pair of nullable values, each of Left or Right type respectively. One of the two will
     *  always be a null
     */
    abstract fun asPair(): Pair<L?,R?>

    /**
     * Returns the projection of the Left type. The projection enables many other methods on the Left value
     * such as map, forEach, filter etc.
     */
    abstract fun left(): LeftProjection<L,R>
    /**
     * Returns the projection of the Right type. The projection enables many other methods on the Right value
     * such as map, forEach, filter etc.
     */
    abstract fun right(): RightProjection<L,R>

    /**
     * is the Left value defined?
     */
    abstract val isLeft: Boolean
    /**
     * is the Right value defined?
     */
    abstract val isRight: Boolean

    /**
     * Transform the left value to an option. If the left value is defined, it will be a Some, else it will be a None
     */
    abstract fun asLeft(): Option<L>
    /**
     * Transform the right value to an option. If the right value is defined, it will be a Some, else it will be a None
     */
    abstract fun asRight(): Option<R>

    /**
    ** Helper function to decompose this into a pair of left and right types. This results in the first element of
     * the pair being the value of the left type
     */
    operator fun component1(): Option<L> = asLeft()
    /**
     ** Helper function to decompose this into a pair of left and right types. This results in the second element of
     * the pair being the value of the right type
     */
    operator fun component2(): Option<R> = asRight()

    /**
     * Method to transform this into a result of the same type [T] irrespective of whether this is a left or a right
     * value, using transformation function pair to convert either the left or right value
     */
    abstract fun<T> fold(fnLeft: (L) -> T, fnRight: (R) ->T): T

    /**
     * Swaps the types, ie. the Left becomes Right and Right becomes Left in the new Either instance
     */
    abstract fun swap(): Either<R,L>

    class Right<L, R>(val value: R) : Either<L, R>() {
        override val isRight: Boolean = true
        override val isLeft: Boolean = false
        override fun asLeft(): Option<L> = None
        override fun asRight(): Option<R> = Some(value)
        override fun toString(): String = "Right($value)"
        override fun equals(other: Any?): Boolean =
                this === other || (other != null && other is Right<*,*> && this.hashCode() == other.hashCode()
                        && this.value == other.value)
        override fun hashCode(): Int = value!!.hashCode()
        override fun<T> fold(fnLeft: (L) -> T, fnRight: (R) ->T): T = fnRight(value)
        override fun left(): LeftProjection<L,R> = LeftProjection.LeftProjectionOfRight(value)
        override fun right(): RightProjection<L,R> = RightProjection.RightProjectionOfRight(value)
        override fun swap(): Either<R,L> = Left<R,L>(value)
        override fun asPair(): Pair<L?,R?> = Pair(null,value)
    }

    class Left<L, R>(val value: L) : Either<L, R>() {
        override val isRight: Boolean = false
        override val isLeft: Boolean = true
        override fun asLeft(): Option<L> = Some(value)
        override fun asRight(): Option<R> = None
        override fun toString(): String = "Left($value)"
        override fun equals(other: Any?): Boolean =
                this === other || (other != null && other is Left<*,*> && this.hashCode() == other.hashCode()
                        && this.value == other.value)
        override fun hashCode(): Int = value!!.hashCode()
        override fun<T> fold(fnLeft: (L) -> T, fnRight: (R) ->T): T = fnLeft(value)
        override fun left(): LeftProjection<L,R> = LeftProjection.LeftProjectionOfLeft(value)
        override fun right(): RightProjection<L,R> = RightProjection.RightProjectionOfLeft(value)
        override fun swap(): Either<R,L> = Right<R,L>(value)
        override fun asPair(): Pair<L?,R?> = Pair(value, null)
    }

    /**
     * Interface primarily for declaring a common function across both types of projections
     */
    interface Projection<out T>: Collection<T> {
        fun exists(pred: (T) -> Boolean): Boolean
    }

    /**
     * Left projection of the Either type
     */
    @Suppress("REDUNDANT_MODIFIER")
    sealed abstract class LeftProjection<out L, out R>: Projection<L> {
        /**
         * Allows a transformation if left type [L] is defined to another [T] using a map function
         */
        fun<T> map(f: (L) -> T): Either<T,R> = flatMap {it: L -> Left<T,R>(f(it))}

        /**
         * filter this instance to optional one depending on the result of a predicate being applied to the left value
         */
        abstract fun filter(pred: (L) -> Boolean): Option<Either<L,R>>

        /**
         * Extract the left value as an option
         */
        abstract fun toOption(): Option<L>

        class LeftProjectionOfLeft<L, R>(val value: L): LeftProjection<L,R>() {
            override val size: Int = 1
            override fun contains(element: L): Boolean = (value == element)
            override fun containsAll(elements: Collection<L>): Boolean = (elements.all { it == value })
            override fun isEmpty(): Boolean = false
            override fun iterator(): Iterator<L> = SingleItemIterator(value)
            override fun exists(pred: (L) -> Boolean): Boolean = pred(value)
            override fun filter(pred: (L) -> Boolean): Option<Either<L,R>> = if (pred(value)) Some(Left<L,R>(value)) else None
            override fun toOption(): Option<L> = Some(value)
        }
        class LeftProjectionOfRight<L, R>(val value: R): LeftProjection<L,R>() {
            override val size: Int = 0
            override fun contains(element: L): Boolean = false
            override fun containsAll(elements: Collection<L>): Boolean = elements.isEmpty()
            override fun isEmpty(): Boolean = true
            override fun iterator(): Iterator<L> = EmptyIterator
            override fun exists(pred: (L) -> Boolean): Boolean = false
            override fun filter(pred: (L) -> Boolean): Option<Either<L,R>> = None
            override fun toOption(): Option<L> = None
        }
    }
    @Suppress("REDUNDANT_MODIFIER")
    sealed abstract class RightProjection<out L, out R>: Projection<R> {
        /**
         * Allows a transformation if right type [R] is defined to another [T] using a map function
         */
        fun<T> map(f: (R) -> T): Either<L, T> = flatMap { it: R -> Right<L, T>(f(it)) }

        /**
         * filter this instance to optional one depending on the result of a predicate being applied to the right value
         */
        abstract fun filter(pred: (R) -> Boolean): Option<Either<L,R>>

        /**
         * Extract the right value as an option
         */
        abstract fun toOption(): Option<R>


        class RightProjectionOfLeft<L, R>(val value: L) : RightProjection<L, R>() {
            override val size: Int = 0
            override fun contains(element: R): Boolean = false
            override fun containsAll(elements: Collection<R>): Boolean = elements.isEmpty()
            override fun isEmpty(): Boolean = true
            override fun iterator(): Iterator<R> = EmptyIterator
            override fun exists(pred: (R) -> Boolean): Boolean = false
            override fun filter(pred: (R) -> Boolean): Option<Either<L,R>> = None
            override fun toOption(): Option<R> = None
        }

        class RightProjectionOfRight<L, R>(val value: R) : RightProjection<L, R>() {
            override val size: Int = 1
            override fun contains(element: R): Boolean = (value == element)
            override fun containsAll(elements: Collection<R>): Boolean = (elements.all { it == value })
            override fun isEmpty(): Boolean = false
            override fun iterator(): Iterator<R> = SingleItemIterator(value)
            override fun exists(pred: (R) -> Boolean): Boolean = pred(value)
            override fun filter(pred: (R) -> Boolean): Option<Either<L,R>> = if (pred(value)) Some(Right<L,R>(value)) else None
            override fun toOption(): Option<R> = Some(value)
        }
    }
}

/**
 * Perform the necessary computation with the result being the right type if successfully computed, and an exception
 * being the left type in case an exception go raised
 */
fun<T> doTry(fn: () -> T): Either<Exception,T> = try { Either.Right(fn()) } catch (e: Exception) { Either.Left(e) }

/**
 * Transform this into another either of a different left type, using a binding function which transforms a value of a
 * left type into an either with a different left type
 */

fun<L,R,T> Either.LeftProjection<L, R>.flatMap(f: (L) -> Either<T,R>): Either<T,R> {
    return when (this) {
        is Either.LeftProjection.LeftProjectionOfLeft -> f(this.value)
        is Either.LeftProjection.LeftProjectionOfRight -> Either.Right(this.value)
    }
}

/**
 * Transform this into another either of a different right type, using a binding function which transforms a value of a
 * right type into an either with a different right type
 */
fun<L,R,T> Either.RightProjection<L, R>.flatMap(f: (R) -> Either<L,T>): Either<L,T> {
    return when (this) {
        is Either.RightProjection.RightProjectionOfLeft -> Either.Left(this.value)
        is Either.RightProjection.RightProjectionOfRight -> f(this.value)
    }
}

class EitherFolder<L,R,T>(val e: Either<L,R>, val fnLeft: (L) -> T) {
    fun andRight(fnRight: (R) ->T): T = e.fold(fnLeft,fnRight)
}

fun<L,R,T> Either<L,R>.foldOverLeft(fnLeft: (L) -> T): EitherFolder<L,R,T> = EitherFolder(this, fnLeft)


/**
 * A right biased flatMap
 */

fun <L, R, T> Either<L,R>.flatMap(f: (R) -> Either<L,T>): Either<L,T> =
        when(this) {
            is Either.Left -> Either.Left<L,T>(this.value)
            is Either.Right -> f(this.value)
        }

/**
 * A right biased flatMap
 */

fun <L, R> Either<L,R>.flatMapWithOption(f: (R) -> L?): Either<L,R> =
        when(this) {
            is Either.Left -> Either.Left<L,R>(this.value)
            is Either.Right -> f(this.value)?.let { Either.Left<L,R>(it) } ?: this
        }


/**
 * Map this left value and another instance of an Either (with the same right type, but a different left type),
 * into another Either instance with yet another type as its Left type
 */
fun<L, R, P, Q> Either.LeftProjection<L,R>.map(p: Either<P,R>, f:(L,P) -> Q): Either<Q,R> =
    flatMap { l -> p.left().map { pp -> f(l, pp)}}

/**
 * Map this right value and another instance of an Either (with the same left type, but a different right type),
 * into another Either instance with yet another type as its Right type
 */
fun<L, R, P, Q> Either.RightProjection<L,R>.map(p: Either<L, P>, f:(P,R) -> Q): Either<L,Q> =
    flatMap { r -> p.right().map { pp -> f(pp, r)}}

/**
 * A right biased map on a Left Instance
 *
 * Note that if both instances are left, the value of this instance overrides that of p
 */
fun<L, R, P, Q> Either.Left<L,R>.map(p: Either<L, P>, f:(P,R) -> Q): Either<L,Q> = Either.Left<L,Q>(this.value)

/**
 * A right biased map on a Right Instance
 */

fun<L, R, P, Q> Either<L,R>.map(p: Either<L, P>, f:(P,R) -> Q): Either<L,Q> =
     when(this) {
        is Either.Left -> Either.Left<L,Q>(this.value)
        is Either.Right -> when(p) {
            is Either.Left -> Either.Left<L,Q>(p.value)
            is Either.Right -> Either.Right(f(p.value, this.value))
        }
    }


/**
 * Given an either of both left and right being of the same, type, merge it into a single value of the same type
 */
fun<T> Either<T,T>.merge(): T =
    when(this) {
        is Either.Left<T, T> -> this.value
        is Either.Right<T, T> -> this.value
    }

/**
 * Extract the underlying value of the projection if defined else use a default value, using a value generator function
 */
fun<L,R> Either.LeftProjection.LeftProjectionOfLeft   <L,R>.getOrElse(@Suppress("UNUSED_PARAMETER") default: () -> L): L = value
/**
 * Extract the underlying value of the projection if defined else use a default value, using a value generator function
 */
fun<L,R> Either.LeftProjection.LeftProjectionOfRight  <L,R>.getOrElse(default: () -> L): L = default()
/**
 * Extract the underlying value of the projection if defined else use a default value, using a value generator function
 */
fun<L,R> Either.RightProjection.RightProjectionOfLeft <L,R>.getOrElse(default: () -> R): R = default()
/**
 * Extract the underlying value of the projection if defined else use a default value, using a value generator function
 */
fun<L,R> Either.RightProjection.RightProjectionOfRight<L,R>.getOrElse(@Suppress("UNUSED_PARAMETER") default: () -> R): R = value

/**
 * Convert a pair into an Either treating the Left value as being the only defined one. Right value is ignored
 */
fun<L,R> Pair<L,R>.toLeft(): Either.Left<L, R> = Either.Left(this.first)
/**
 * Convert a pair into an Either treating the Right value as being the only defined one. Left value is ignored
 */
fun<L,R> Pair<L,R>.toRight(): Either.Right<L, R> = Either.Right(this.second)

/**
 * Allow a fold operation on the Either using an implementation of the EitherHandler interface rather than
 * separate transformation functions for the left and right values
 */
fun<L,R,T> Either<L,R>.fold(handler: EitherHandler<L,R,T>): T =
    when(this) {
        is Either.Left -> handler.onLeft(this)
        is Either.Right -> handler.onRight(this)
    }

/**
 * Convert an option to a left if option is defined else compute using the supplied function
 */
fun <L,R> Option<L>.toLeftOr(fn: () -> Either<L,R>): Either<L,R> =
        when (this) {
            is Some<L> -> Either.Left<L,R>(this.t)
            is None -> fn()
        }

/**
 * Convert an option to a right if option is defined else compute using the supplied function
 */
fun <L,R> Option<R>.toRightOr(fn: () -> Either<L,R>): Either<L,R> =
        when (this) {
            is Some<R> -> Either.Right<L,R>(this.t)
            is None -> fn()
        }



