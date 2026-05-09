// port-lint: source tests/test.rs
package io.github.kotlinmania.lazystatic

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

// Documentation!
public val NUMBER: LazyStaticRef<UInt> = lazyStatic { timesTwo(3u) }

private val ARRAY_BOXES: LazyStaticRef<List<UInt>> =
    lazyStatic { listOf(1u, 2u, 3u) }

// More documentation!
// Upstream attaches `#[derive(Copy, Clone, Debug)]` to the macro-generated wrapper
// type; the Kotlin port has no per-static wrapper, so the corresponding behavioural
// expectation is just that repeated reads return the same cached instance.
public val STRING: LazyStaticRef<String> = lazyStatic { "hello" }

private val HASHMAP: LazyStaticRef<Map<UInt, String>> = lazyStatic {
    val m = mutableMapOf<UInt, String>()
    m[0u] = "abc"
    m[1u] = "def"
    m[2u] = "ghi"
    m
}

// This should not compile if the unsafe is removed.
// Upstream `std::mem::transmute::<i32, u32>(-1)` reinterprets the all-ones bit pattern
// of -1i32 as the maximum u32 value.
private val UNSAFE: LazyStaticRef<UInt> = lazyStatic {
    (-1).toUInt()
}

private val S1: LazyStaticRef<String> = lazyStatic { "a" }
private val S2: LazyStaticRef<String> = lazyStatic { "b" }
private val S3: LazyStaticRef<String> = lazyStatic { listOf(S1.value, S2.value).joinToString("") }

// Upstream's `#[allow(non_upper_case_globals)] pub static ref string` exists to verify
// the macro accepts a lower-case identifier. Kotlin top-level vals have no equivalent
// upper-case convention, so the lower-case spelling is the natural form.
public val string: LazyStaticRef<String> = lazyStatic { "hello" }

private fun timesTwo(n: UInt): UInt = n * 2u

public class LazyStaticTest {

    @Test
    public fun s3() {
        assertEquals("ab", S3.value)
    }

    @Test
    public fun testBasic() {
        assertEquals("hello", STRING.value)
        assertEquals(6u, NUMBER.value)
        assertNotNull(HASHMAP.value[1u])
        assertNull(HASHMAP.value[3u])
        assertEquals(listOf(1u, 2u, 3u), ARRAY_BOXES.value)
        assertEquals(UInt.MAX_VALUE, UNSAFE.value)
    }

    @Test
    public fun testRepeat() {
        assertEquals(6u, NUMBER.value)
        assertEquals(6u, NUMBER.value)
        assertEquals(6u, NUMBER.value)
    }

    @Test
    public fun testMeta() {
        // Upstream verifies the macro-generated type implements `Copy`, `Clone`, and
        // `Debug`. The Kotlin port models a lazy static as a single [LazyStaticRef]
        // rather than a per-static type, so the analogous behavioural check is that
        // repeated reads observe the same cached object.
        val first = STRING.value
        val second = STRING.value
        assertSame(first, second)
    }

    @Test
    public fun testVisibility() {
        assertEquals(0u, Visibility.FOO.value)
        assertEquals(37u, Visibility.Inner.BAG.value)
    }

    @Test
    public fun subTest() {
        assertEquals(0u, Visibility.FOO.value)
        assertEquals(98u, Visibility.BAR.value)
        assertEquals(42u, Visibility.Inner.BAZ.value)
        assertEquals(37u, Visibility.Inner.BAG.value)
    }

    @Test
    public fun itemNameShadowing() {
        assertEquals(X, ITEM_NAME_TEST.value)
    }

    @Test
    public fun preInit() {
        assertFalse(PRE_INIT_FLAG.load())
        initialize(PRE_INIT)
        assertTrue(PRE_INIT_FLAG.load())
    }

    @Test
    public fun lifetimeName() {
        // Upstream stores a function pointer with an explicit `for<'a>` lifetime quantifier.
        // Kotlin function types have no lifetime parameter; the analogous check is just
        // that the value reads back as a callable.
        val fn = LIFETIME_NAME.value
        fn(0u)
    }
}

private object Visibility {
    public val FOO: LazyStaticRef<UInt> = lazyStatic { 0u }
    public val BAR: LazyStaticRef<UInt> = lazyStatic { 98u }

    public object Inner {
        public val BAZ: LazyStaticRef<UInt> = lazyStatic { 42u }
        public val BAG: LazyStaticRef<UInt> = lazyStatic { 37u }
    }
}

// This should not cause a warning about a missing Copy implementation.
public val VAR: LazyStaticRef<Int> = lazyStatic { 0 }

private object X {
    override fun toString(): String = "X"
}
private class Once(val x: X)
private val ONCE_INIT: Once = Once(X)
private val DATA: X = X
private val ONCE: X = X
private fun requireSync(): X = X
private fun transmute(): X = X
private fun staticRefInitialize(): X = X
private fun test(items: List<X>): X {
    assertTrue(items.isNotEmpty())
    return X
}

// All these names should not be shadowed.
private val ITEM_NAME_TEST: LazyStaticRef<X> = lazyStatic {
    test(
        listOf(
            X, Once(X).x, ONCE_INIT.x, DATA, ONCE,
            requireSync(), transmute(),
            // Except this, which would sadly be shadowed by internals upstream;
            // Kotlin closures do not capture local helper names so the call would be
            // valid here, but the upstream comment is preserved as content:
            // staticRefInitialize()
        ),
    )
}

private val PRE_INIT_FLAG: AtomicBoolean = AtomicBoolean(false)

private val PRE_INIT: LazyStaticRef<Unit> = lazyStatic {
    PRE_INIT_FLAG.store(true)
}

private val LIFETIME_NAME: LazyStaticRef<(UByte) -> Unit> = lazyStatic {
    val f: (UByte) -> Unit = { _ -> }
    f
}
