package hnau.common.gen.sealup.test

import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.common.gen.sealup.test.State1
import kotlinx.serialization.Serializable

@Serializable
data class State1(
    val count: Int,
) {

    constructor(): this(count = 1)

    fun foo1() {}

    fun String.foo2(): String = count.toString()

    fun <T, S> foo3(iE: T, sF: S): T where T: Number, T: Comparable<T> = iE

    fun foo4(iG: Int, sH: String) {}

    val bar1: Any
        get() = count

    var <T> T.bar2: Any where T: Number
        get() = count
        set(value) {}
}


@Retention(AnnotationRetention.SOURCE)
annotation class TestAnnotation(
    val value: String,
)

@Serializable
data class State2(
    val flag: Boolean,
) {

    constructor(): this(flag = true)

    constructor(int: Int): this(flag = int != 0)

    fun foo1() {}

    fun String.foo2(): String = flag.toString()

    fun <T, S> foo3(iE: T, sF: S): T where T: Number, T: Comparable<T> = iE

    fun foo4(iC: Int, sD: String) {}

    val bar1: Any
        get() = flag

    var <T> T.bar2: Any where T: Number
        get() = flag
        set(value) {}
}

data object App {

    @SealUp(
        variants = [
            Variant(
                type = State1::class,
                wrapperClassName = "State1W",
                identifier = "s1",
                serialName = "state_1",
                wrappedValuePropertyName = "wrappedValueState1"
            ),
            Variant(
                type = State2::class,
                wrapperClassName = "State2Wrap",
                identifier = "sss1",
                serialName = "state_second",
                wrappedValuePropertyName = "wrappedValueState2"
            ),
        ],
        serializable = true,
        ordinal = true,
        name = true,
        fold = true,
        factoryMethods = true,
        sealedInterfaceName = "AppState",
    )
    internal interface State {

        fun foo1()

        @TestAnnotation("abc")
        fun String.foo2(): String

        fun <T, S> foo3(iE: T, sF: S): T where T: Number, T: Comparable<T>

        fun foo4(i: Int, s: String)

        val bar1: Any

        var <T> T.bar2: Any where T: Number

        companion object
    }
}

fun main() {

}

