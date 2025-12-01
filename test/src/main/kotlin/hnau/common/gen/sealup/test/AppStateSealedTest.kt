/*
package hnau.common.gen.sealup.test

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface AppStateSealedTest : App.State {

    val ordinal: Int

    val name: String

    val variant: AppStateVariant

    @Serializable
    @SerialName("state1")
    data class State1Wrapper(
        val value: State1,
    ) : AppStateSealedTest {

        override val ordinal: Int
            get() = 0

        override val name: String
            get() = "state1"

        override val variant: AppStateVariant
            get() = AppStateVariant.State1

        override fun foo1() {
            value.foo1()
        }

        override fun foo2(): String = value.foo2()

        override fun foo3(
            i: Int,
            s: String,
        ): String = value.foo3(i, s)

        override fun foo4(
            i: Int,
            s: String,
        ) {
            value.foo4(i, s)
        }

        override var bar: Any
            get() = value.bar
            set(newValue) {
                value.bar = newValue
            }
    }

    @Serializable
    @SerialName("state2")
    data class State2Wrapper(
        val value: State2,
    ) : AppStateSealedTest {

        override val ordinal: Int
            get() = 2

        override val name: String
            get() = "state2"

        override val variant: AppStateVariant
            get() = AppStateVariant.State2

        override fun foo1() {
            value.foo1()
        }

        override fun foo2(): String = value.foo2()

        override fun foo3(
            i: Int,
            s: String,
        ): String = value.foo3(i, s)

        override fun foo4(
            i: Int,
            s: String,
        ) {
            value.foo4(i, s)
        }

        override var bar: Any
            get() = value.bar
            set(newValue) {
                value.bar = newValue
            }
    }
}

internal inline fun <R> AppStateSealedTest.fold(
    ifState1: (State1) -> R,
    ifState2: (State2) -> R,
): R = when (this) {
    is AppStateSealedTest.State1Wrapper -> ifState1(value)
    is AppStateSealedTest.State2Wrapper -> ifState2(value)
}

internal fun App.State.Companion.state1(
    count: Int,
): AppStateSealedTest.State1Wrapper = AppStateSealedTest.State1Wrapper(
    value = State1(
        count = count,
    ),
)

internal fun App.State.Companion.state2(
    flag: Boolean,
): AppStateSealedTest.State2Wrapper = AppStateSealedTest.State2Wrapper(
    value = State2(
        flag = flag,
    ),
)

enum class AppStateVariant {

    State1,
    State2,
}*/
