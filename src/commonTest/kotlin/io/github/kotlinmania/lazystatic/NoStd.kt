// port-lint: source tests/no_std.rs
package io.github.kotlinmania.lazystatic

import io.github.kotlinmania.lazystatic.lazycore.Lazy
import kotlin.test.Test
import kotlin.test.assertEquals

// Upstream gates this file on `#![cfg(feature = "spin_no_std")]` together with
// `#![no_std]`. Kotlin Multiplatform has no std-vs-no-std split, so the upstream
// `spin_no_std` cargo feature is translated as the parallel
// [io.github.kotlinmania.lazystatic.lazycore] backend; this test exercises that
// backend in place of the standard inline one used by [Test.kt].
//
// Upstream's `pub static ref NUMBER: u32 = times_two(3);` lives at module scope.
// In Kotlin a sibling [Test.kt] in the same package already binds a top-level
// `NUMBER`, so this file scopes its own declarations inside a private object
// rather than introducing a colliding top-level name. The behavioural contract is
// the same: a single lazy cell whose builder evaluates `times_two(3)` exactly
// once.
private object NoStdStatics {
    // Documentation!
    val NUMBER: Lazy<UInt> = Lazy.init()

    fun timesTwo(n: UInt): UInt = n * 2u
}

public class NoStdTest {

    @Test
    public fun testBasic() {
        assertEquals(6u, NoStdStatics.NUMBER.get { NoStdStatics.timesTwo(3u) })
    }
}
