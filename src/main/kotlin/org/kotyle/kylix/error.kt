package org.kotyle.kylix.error

data class Fault(val message: String, val args: Map<String, Any?> = mapOf(), val cause: Throwable? = null, val subErrors: List<Fault>? = null)