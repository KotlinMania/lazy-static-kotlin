// port-lint: source src/lib.rs
package io.github.kotlinmania.lazystatic

// Copyright 2016 lazy-static.rs Developers
//
// Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
// https://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
// https://opensource.org/licenses/MIT>, at your option. This file may not be
// copied, modified, or distributed except according to those terms.

/*
A library for declaring lazily evaluated statics.

Using this library, it is possible to have top-level values that require code to be
executed at runtime in order to be initialized. This includes anything requiring heap
allocations, like lists or maps, as well as anything that requires function calls to
be computed.

# Syntax

Upstream Rust exposes a `lazy_static!` macro:

    lazy_static! {
        [pub] static ref NAME_1: TYPE_1 = EXPR_1;
        [pub] static ref NAME_2: TYPE_2 = EXPR_2;
        ...
        [pub] static ref NAME_N: TYPE_N = EXPR_N;
    }

Macros do not exist in Kotlin. The runtime equivalent is the [lazyStatic] builder, which
returns a [LazyStaticRef] holding the lazily computed value. Doc-comment attributes that
the upstream macro forwards onto the generated type translate to KDoc on the property
holding the [LazyStaticRef]:

    /// This is an example for using doc comment attributes
    val EXAMPLE: LazyStaticRef<Byte> = lazyStatic { 42 }

# Semantics

For a given upstream `static ref NAME: TYPE = EXPR;`, the macro generates a unique type
that implements `Deref<TYPE>` and stores it in a static with name `NAME`. (Attributes end
up attaching to this type.)

In Kotlin the equivalent is a single [LazyStaticRef] instance whose [LazyStaticRef.value]
property plays the role of `*NAME`. On first access, `EXPR` (passed in as the builder)
gets evaluated and stored internally, such that all further accesses can return a
reference to the same object. Note that this can lead to deadlocks if you have multiple
lazy statics that depend on each other in their initialization.

Apart from the lazy initialization, the resulting "static ref" values have generally the
same properties as regular top-level values:

- Any type in them needs to be safely shareable across threads on the platforms it runs on.
- The value is not torn down when the process exits.

# Example

Using the builder:

    import io.github.kotlinmania.lazystatic.lazyStatic
    import io.github.kotlinmania.lazystatic.initialize

    val HASHMAP: LazyStaticRef<Map<UInt, String>> = lazyStatic {
        mapOf(
            0u to "foo",
            1u to "bar",
            2u to "baz",
        )
    }
    val COUNT: LazyStaticRef<Int> = lazyStatic { HASHMAP.value.size }
    val NUMBER: LazyStaticRef<UInt> = lazyStatic { timesTwo(21u) }

    fun timesTwo(n: UInt): UInt = n * 2u

    fun main() {
        println("The map has ${COUNT.value} entries.")
        println("The entry for `0` is \"${HASHMAP.value[0u]}\".")
        println("A expensive calculation on a static results in: ${NUMBER.value}.")
    }

# Implementation details

The [LazyStaticRef.value] accessor uses a hidden once-cell that is guarded by an atomic
check on each access.

# Cargo features

This crate provides one cargo feature:

- `spin_no_std`: This allows using this crate in a no-std environment, by depending on
  the standalone `spin` crate.

In the Kotlin port, both backends are translated as parallel sub-packages
([io.github.kotlinmania.lazystatic.lazy] and [io.github.kotlinmania.lazystatic.lazycore])
since Kotlin Multiplatform has no equivalent of cargo features. The default builder
[lazyStatic] uses the inline backend, matching the default cargo configuration.
*/

import io.github.kotlinmania.lazystatic.lazy.Lazy

/**
 * Support trait for enabling a few common operations on lazy static values.
 *
 * This is implemented by each defined lazy static, and used by the free functions in
 * this library.
 */
public interface LazyStatic {
    public fun initialize()
}

/**
 * The runtime counterpart of an upstream `lazy_static! { static ref NAME: T = ... }`
 * declaration: a single value of type [T] computed exactly once on first access.
 *
 * Read the value through [value], or invoke [initialize] to force the computation
 * without otherwise observing the result.
 */
public class LazyStaticRef<T : Any> internal constructor(
    private val initializer: () -> T,
) : LazyStatic {

    private val cell: Lazy<T> = Lazy.init()

    /**
     * The lazily computed value. The first read runs the builder; every subsequent
     * read returns the same cached object.
     */
    public val value: T get() = cell.get(initializer)

    override fun initialize() {
        // Equivalent to upstream's `let _ = &**lazy;` — a single dereference forces
        // initialization and discards the result.
        cell.get(initializer)
    }
}

/**
 * Build a [LazyStaticRef] that defers [builder] until the first read of [LazyStaticRef.value].
 *
 * This is the runtime API the Kotlin port exposes in place of the upstream Rust
 * `lazy_static!` macro.
 */
public fun <T : Any> lazyStatic(builder: () -> T): LazyStaticRef<T> = LazyStaticRef(builder)

/**
 * Take a shared reference to a lazy static and initialize it if it has not been already.
 *
 * This can be used to control the initialization point of a lazy static.
 *
 * Example:
 *
 *     import io.github.kotlinmania.lazystatic.lazyStatic
 *     import io.github.kotlinmania.lazystatic.initialize
 *
 *     val BUFFER: LazyStaticRef<List<UByte>> = lazyStatic { (0u..255u).map { it.toUByte() } }
 *
 *     fun main() {
 *         initialize(BUFFER)
 *
 *         // ...
 *         workWithInitializedData(BUFFER.value)
 *     }
 *
 *     fun workWithInitializedData(data: List<UByte>) {}
 */
public fun <T : LazyStatic> initialize(lazy: T) {
    lazy.initialize()
}
