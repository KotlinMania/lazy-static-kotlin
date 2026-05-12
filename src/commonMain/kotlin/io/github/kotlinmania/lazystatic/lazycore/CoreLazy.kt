// port-lint: source src/core_lazy.rs
package io.github.kotlinmania.lazystatic.lazycore

// Copyright 2016 lazy-static.rs Developers
//
// Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
// https://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
// https://opensource.org/licenses/MIT>, at your option. This file may not be
// copied, modified, or distributed except according to those terms.

import kotlin.concurrent.atomics.AtomicReference

/**
 * The `spin_no_std` backend.
 *
 * In Rust this is selected via cargo's `spin_no_std` feature and is implemented on top
 * of the `spin` crate's `Once<T>`. Kotlin Multiplatform has no analogue of std vs no-std,
 * so the two backends are translated as a parallel pair: pick whichever package matches
 * the upstream feature you wanted to mirror.
 */
public class Lazy<T : Any> {

    private val once: AtomicReference<T?> = AtomicReference(null)

    public companion object {
        /**
         * The initial, uninitialized state. Mirrors `Lazy::INIT` from upstream.
         */
        public fun <T : Any> init(): Lazy<T> = Lazy()
    }

    /**
     * Return the cached value, running [builder] exactly once on first access.
     */
    public fun get(builder: () -> T): T {
        once.load()?.let { return it }
        val candidate = builder()
        return if (once.compareAndSet(null, candidate)) {
            candidate
        } else {
            once.load()!!
        }
    }
}

// Kotlin equivalent of the `__lazy_static_create!` macro:
//
//     static $NAME: lazy::Lazy<$T> = lazy::Lazy::INIT;
//
// is written at the call site as `val NAME: Lazy<T> = Lazy.init()`.
