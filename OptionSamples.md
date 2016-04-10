# Option usage

The code below illustrates various usages of `Option` documented as a Test Case

```
import org.junit.Test
import org.kotyle.kylix.option.*
import org.kotyle.kylix.option.Option.None
import org.kotyle.kylix.option.Option.Some


class OptionSamples {
    /**
     * How to create an option. There are two primary ways to do it, both same as each other
     *
     * There's no particular difference between the two, its just a matter of taste
     */
    @Test
    fun create() {
        /**
         * A null becomes an None when converted to an option
         */
        assert(None == Option(null))
        assert(None == null.toOption())

        /**
         * Any non null value is wrapped by Some when converted to an option
         */
        assert(Some(99) == Option(99))
        assert(Some(99) == 99.toOption())

        /**
         * You could of course use the Some() constructor as well
         */
        assert(99.toOption() == Some(99))

        /**
         * But be careful, a None when converted to an option becomes a Some(None)
         */
        assert(Some(None) == None.toOption())
    }

    /**
     * An option can always be converted back to a nullable type
     */
    @Test
    fun convertToNullableType() {
        assert(null == None.orNull())
        assert(99 == Some(99).orNull())
    }

    @Test
    fun definedOrEmpty() {
        /**
         * A None is always empty and not defined
         */
        assert(false == None.isDefined())
        assert(true == None.isEmpty())

        /**
         * A Some is always non empty but defined
         */
        assert(true == Some(99).isDefined())
        assert(false == Some(99).isEmpty())
    }

    /**
     * A filter applies a predicate on an option returning itself on a match or a None otherwise
     * A filterNot applies a predicate on an option returning a None on a match or itself otherwise
     *
     * In either case if the option is a None, a None is returned
     */

    @Test
    fun filterOrFilterNot() {
        assert(None     == None.filter { true })
        assert(None     == None.filter { false })
        assert(None     == Some(99).filter { it % 2 == 0 })
        assert(Some(99) == Some(99).filter { it % 2 != 0 })

        assert(None     == None.filterNot { true })
        assert(None     == None.filterNot { false })
        assert(Some(99) == Some(99).filterNot { it % 2 == 0 })
        assert(None     == Some(99).filterNot { it % 2 != 0 })
    }

    /**
     * An exists will always return a false if called on a None.
     * On a Some, it will evaluate the predicate and return a result of that evaluation
     */
    @Test
    fun exists() {
        assert(false == None.exists { true })
        assert(false == None.exists { false })
        assert(false == Some(99).exists { it % 2 == 0 })
        assert(true  == Some(99).exists { it % 2 != 0 })
    }

    /**
     * A map will always return a None if called on a None
     * On a some, it will perform a transformation and return the result as an option
     */
    @Test
    fun map() {
        assert(None      == None.map { it: Int -> it * 2 })
        assert(Some(198) == Some(99).map { it * 2 })
        /* Note - if your function is returning an option as below, you might be better off calling a flatMap */
        assert(Some(None) == Some(99).map { None })
        assert(Some(Some(55)) == Some(99).map { Some(55) })
    }

    /**
     * A flatMap will flatten a twice nested Option into a single level
     *   is particularly useful when you want to map over an option with a function that returns an option
     */
    @Test
    fun flatMap() {
        assert(None == Some(99).flatMap { None })
        assert(Some(55) == Some(99).flatMap { Some(55) })
        assert(None == None.flatMap { Some(55) })
    }

    /**
     * A fold allows a default value or a function returning a default value to be used in place of a function
     * performing a transformation for situations where the option is undefined
     */
    @Test
    fun fold() {
        assert("99" == Some(99).fold("55"){ it.toString()} )
        assert("55" == None.fold("55"){ it.toString()} )
    }

    /**
     * getOrElse allows you to extract the underlying value from the Option if defined, or default to the provided value
     */
    @Test
    fun getOrElse() {
        assert(55 == None.getOrElse(55))
        assert(99 == Some(99).getOrElse(55))
    }

    /**
     * orElse uses this option if defined or evaluates another value alternatively
     *
     * or is almost the same except that or is an infix function so has a better syntactic sugar quotient
     */
    @Test
    fun orElse() {
        assert(Some(55) == None.orElse(Some(55)))
        assert(Some(99) == Some(99).orElse { Some(55)})

        assert(Some(55) == None or Some(55))
        assert(Some(99) == Some(99) or { Some(55)})
    }

    /**
     * The optional projection of a map allows you to perform a get on a Map which returns an Option,
     * ie. Some if the key exists in the Map or None if the key does not
     */
    @Test
    fun mapOptionProjection() {
        val map = mapOf(1 to "one", 2 to "two", 3 to "three")
        assert(Some("one") == map.optional[1])
        assert(None == map.optional[99])
    }

    /**
     * The Option class is also a collection. If empty it is a collection with zero items, else it is a collection
     * with exactly 1 item. All functions available on a collection are also available on Option instances
     */
    @Test
    fun asACollection() {
        var forEachCalled = 0
        var forEachValue: Int? = null
        Some(5).forEach {
            forEachCalled += 1
            forEachValue = it
        }
        assert(forEachCalled == 1 && forEachValue == 5)

        forEachCalled = 0
        forEachValue = null
        None.forEach {
            @Suppress("UNREACHABLE_CODE")forEachCalled += 1
            @Suppress("UNREACHABLE_CODE")forEachValue = it
        }
        assert(forEachCalled == 0 && forEachValue == null)
    }
}
```


