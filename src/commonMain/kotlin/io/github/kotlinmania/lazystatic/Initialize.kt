// port-lint: source src/lib.rs
package io.github.kotlinmania.lazystatic

// Copyright 2016 lazy-static.rs Developers
//
// Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
// https://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
// https://opensource.org/licenses/MIT>, at your option. This file may not be
// copied, modified, or distributed except according to those terms.

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
