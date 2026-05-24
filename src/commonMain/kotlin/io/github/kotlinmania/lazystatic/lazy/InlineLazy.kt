// port-lint: source src/inline_lazy.rs
package io.github.kotlinmania.lazystatic.lazy

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
 * The standard ("inline") backend used when the `spin_no_std` cargo feature is off.
 *
 * Holds a single value of type [T] together with a once-guard. The first call to [get]
 * runs the supplied builder and caches the result; every subsequent call returns the
 * same cached reference without ever invoking the builder again.
 */
public class Lazy<T : Any> {

    private val state: AtomicReference<LazyState<T>> = AtomicReference(LazyState.Uninitialized)

    public companion object {
        /**
         * The initial, uninitialized state of a [Lazy] cell. In upstream Rust this is
         * a `const Self`; in Kotlin the generic equivalent is a small factory.
         */
        public fun <T : Any> init(): Lazy<T> = Lazy()
    }

    /**
     * Return the cached value, computing it via [f] on first access.
     *
     * The closure runs at most once, even under concurrent calls. After the first
     * successful initialization every caller observes the same stored object.
     */
    public fun get(f: () -> T): T {
        while (true) {
            when (val current = state.load()) {
                LazyState.Uninitialized -> {
                    if (state.compareAndSet(current, LazyState.Initializing)) {
                        try {
                            val value = f()
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
// is written at the call site as `val NAME: Lazy<T> = Lazy.init()`. Macros do not
// translate; the runtime API above exposes the same constructor shape.
