package org.kotyle.kylix.result

import org.kotyle.kylix.handlers.ResultHandler
import org.kotyle.kylix.handlers.SuccessHandler
import org.kotyle.kylix.error.Fault
import java.util.*

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

/**
 * An Result is a class which represents a result that is one amongst two possible types. Thus at any point in time
 * a value for only one of the two will be defined (and conversely will not be defined for the other type)
 *
 * These are referred to as a Fault or a Success (since there is no defining characteristic)
 *
 * By convention this is used to represent either an error or successful computation. In such a situation
 * the convention is that the Fault type is used to represent the error type while the Success type is the type
 * of a successful computation
 */

sealed class Result<S> private constructor () {
    /**
     * Helper for class level factory methods
     */
    companion object {
        /**
         * Factory method to create a value of the Fault type
         */
        fun <R> toFailure(value: Fault) = Failure<R>(value)

        /**
         * Factory method to create a value of the Success type
         */
        fun <R> toSuccess(value: R) = Success<R>(value)
    }

    /**
     *  Returns a pair of nullable values, each of Fault or Success type respectively. One of the two will
     *  always be a null
     */
    abstract fun asPair(): Pair<Fault?,S?>

    /**
     * is the Fault value defined?
     */
    abstract val isError: Boolean
    /**
     * is the Success value defined?
     */
    abstract val isSuccess: Boolean

    /**
     * Transform the error value to an option. If the error value is defined, it will be a Some, else it will be a None
     */
    abstract fun asError(): Optional<Fault>
    /**
     * Transform the success value to an option. If the success value is defined, it will be a Some, else it will be a None
     */
    abstract fun asSuccess(): Optional<S>

    /**
     * Transform the error value to a nullable type. If the error value is not defined, it will return a null
     */
    abstract fun toError(): Fault?
    /**
     * Transform the success value to a nullable type. If the success value is not defined, it will return a null
     */
    abstract fun toSuccess(): S?

    /**
     * Method to transform this into a result of the same type [T] irrespective of whether this is a error or a success
     * value, using transformation function pair to convert either the error or success value
     */
    abstract fun<T> fold(onFlub: (Fault) -> T, onSuccess: (S) ->T): T

    /**
     * fold using an either handler
     */

    fun <T> fold(handler: ResultHandler<S, T>): T =
        when(this) {
            is Failure -> handler.onFailure(this.value)
            is Success -> handler.onSuccess(this.value)
        }

    /**
     * flatMap using an handler
     */

    fun <T> flatMap(handler: SuccessHandler<S,T>): Result<T> =
            when(this) {
                is Failure -> Failure<T>(this.value)
                is Success -> handler.onSuccess(this.value)
            }

    class Success<R>(val value: R) : Result<R>() {
        override val isSuccess: Boolean = true
        override val isError: Boolean = false
        override fun asError(): Optional<Fault> = Optional.empty()
        override fun asSuccess(): Optional<R> = Optional.of(value)
        override fun toError(): Fault? = null
        override fun toSuccess(): R? = value
        override fun toString(): String = "Success($value)"
        override fun equals(other: Any?): Boolean =
                this === other || (other != null && other is Success<*> && this.hashCode() == other.hashCode()
                        && this.value == other.value)
        override fun hashCode(): Int = value!!.hashCode()
        override fun<T> fold(onFlub: (Fault) -> T, onSuccess: (R) ->T): T = onSuccess(value)
        override fun asPair(): Pair<Fault?,R?> = Pair(null,value)
    }

    class Failure<R>(val value: Fault) : Result<R>() {
        override val isSuccess: Boolean = false
        override val isError: Boolean = true
        override fun asError(): Optional<Fault> = Optional.of(value)
        override fun asSuccess(): Optional<R> = Optional.empty()
        override fun toError(): Fault? = value
        override fun toSuccess(): R? = null
        override fun toString(): String = "Fault($value)"
        override fun equals(other: Any?): Boolean =
                this === other || (other != null && other is Failure<*> && this.hashCode() == other.hashCode()
                        && this.value == other.value)
        override fun hashCode(): Int = value!!.hashCode()
        override fun<T> fold(onFlub: (Fault) -> T, onSuccess: (R) ->T): T = onFlub(value)
        override fun asPair(): Pair<Fault?,R?> = Pair(value, null)
    }
}