// port-lint: source src/lib.rs
package io.github.kotlinmania.lazystatic

// Copyright 2016 lazy-static.rs Developers
//
// Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
// https://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
// https://opensource.org/licenses/MIT>, at your option. This file may not be
// copied, modified, or distributed except according to those terms.

/**
 * Support trait for enabling a few common operations on lazy static values.
 *
 * This is implemented by each defined lazy static, and used by the free functions in
 * this library.
 */
public interface LazyStatic {
    public fun initialize()
}
