// port-lint: source src/core_lazy.rs
package io.github.kotlinmania.lazystatic.lazycore

// Copyright 2016 lazy-static.rs Developers
//
// Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
// https://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
// https://opensource.org/licenses/MIT>, at your option. This file may not be
// copied, modified, or distributed except according to those terms.

import kotlin.concurrent.atomics.AtomicReference

private sealed class LazyState<out T> {
    object Uninitialized : LazyState<Nothing>()
    object Initializing : LazyState<Nothing>()
    class Initialized<T : Any>(val value: T) : LazyState<T>()
    class Failed(val failure: Throwable) : LazyState<Nothing>()
}

/**
 * The `spin_no_std` backend.
 *
 * In Rust this is selected via cargo's `spin_no_std` feature and is implemented on top
 * of the `spin` crate's `Once<T>`. Kotlin Multiplatform has no analogue of std vs no-std,
 * so the two backends are translated as a parallel pair: pick whichever package matches
 * the upstream feature you wanted to mirror.
 */
public class Lazy<T : Any> {

    private val state: AtomicReference<LazyState<T>> = AtomicReference(LazyState.Uninitialized)

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
        while (true) {
            when (val current = state.load()) {
                LazyState.Uninitialized -> {
                    if (state.compareAndSet(current, LazyState.Initializing)) {
                        try {
                            val value = builder()
                            state.store(LazyState.Initialized(value))
                            return value
                        } catch (failure: Throwable) {
                            state.store(LazyState.Failed(failure))
                            throw failure
                        }
                    }
                }
                LazyState.Initializing -> Unit
                is LazyState.Initialized -> return current.value
                is LazyState.Failed -> throw current.failure
            }
        }
    }
}

// Kotlin equivalent of the `__lazy_static_create!` macro:
//
//     static $NAME: lazy::Lazy<$T> = lazy::Lazy::INIT;
//
// is written at the call site as `val NAME: Lazy<T> = Lazy.init()`.
