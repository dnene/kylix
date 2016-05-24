package org.kotyle.kylix.nullable

import org.kotyle.kylix.either.Either

sealed class NullableMapper<O, R> {
    abstract fun orElse(fn: () -> R): R
    class IsNull<O,R>: NullableMapper<O,R>() {
        override fun orElse(fn: () -> R): R = fn()
    }
    class NotNull<O,R>(val r: R) : NullableMapper<O,R>() {
        override fun orElse(fn: () -> R): R = r
    }
}

inline fun <O, R> O?.ifdefined(fn: (O) -> R): NullableMapper<O,R> =
        if (this == null) NullableMapper.IsNull<O,R>() else NullableMapper.NotNull<O,R>(fn(this))

infix fun <T> T?.or(other: T?): T? = if(this == null) other else this
infix fun <T> T?.or(other: () -> T?): T? = if(this == null) other() else this
fun <L,R> L?.toLeftOr(op: () -> Either<L, R>): Either<L, R> =
        if (this != null) Either.Left(this) else op()
fun <T> List<T?>.firstNonNull(): T? { this.forEach { if (it != null) return it }; return null }

