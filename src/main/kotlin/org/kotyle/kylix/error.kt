package org.kotyle.kylix.error

import org.kotyle.kylix.either.Either

data class Fault(val message: String,
                 val args: Map<String, Any?>? = null,
                 val cause: Throwable? = null,
                 val subErrors: List<Fault>? = null) {
    fun isCollection() = (subErrors != null) && (subErrors.size > 0)
    fun annotate(vararg pairs: Pair<String,Any?>): Fault =
            (args?.let { it + pairs } ?: pairs.toMap()).let {
                Fault(message,it,cause,subErrors?.map { it.annotate(*pairs)})
            }
}

fun <T: Any?> Fault.toLeft(): Either<Fault,T> =Either.Left<Fault,T>(this)
fun <T: Any?> List<Fault>.toLeft(): Either<List<Fault>,T> =Either.Left<List<Fault>,T>(this)
fun <T: Any?> T.toRight(): Either<Fault, T> = Either.Right<Fault,T>(this)
fun <T: Any?> T.toRightOfFaultList(): Either<List<Fault>, T> = Either.Right<List<Fault>,T>(this)
fun List<Fault>.toFault(message: String="err-multiple-errors-observed", args: Map<String,Any?>? = null) =
        if (size ==1) this[0] else Fault(message,args = args, subErrors = this)

