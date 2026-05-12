# lazy-static-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Flazy--static--kotlin-blue.svg)](https://github.com/KotlinMania/lazy-static-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/lazy-static-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/lazy-static-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/lazy-static-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/lazy-static-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`rust-lang-nursery/lazy-static.rs`](https://github.com/rust-lang-nursery/lazy-static.rs).

**Original Project:** This port is based on [`rust-lang-nursery/lazy-static.rs`](https://github.com/rust-lang-nursery/lazy-static.rs). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `rust-lang-nursery/lazy-static.rs`

> The text below is reproduced and lightly edited from [`https://github.com/rust-lang-nursery/lazy-static.rs`](https://github.com/rust-lang-nursery/lazy-static.rs). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

lazy-static.rs
==============

A macro for declaring lazily evaluated statics in Rust.

Using this macro, it is possible to have `static`s that require code to be
executed at runtime in order to be initialized.
This includes anything requiring heap allocations, like vectors or hash maps,
as well as anything that requires non-const function calls to be computed.

[![Rust](https://github.com/rust-lang-nursery/lazy-static.rs/actions/workflows/rust.yml/badge.svg)](https://github.com/rust-lang-nursery/lazy-static.rs/actions/workflows/rust.yml)
[![Latest version](https://img.shields.io/crates/v/lazy_static.svg)](https://crates.io/crates/lazy_static)
[![Documentation](https://docs.rs/lazy_static/badge.svg)](https://docs.rs/lazy_static)
[![License](https://img.shields.io/crates/l/lazy_static.svg)](https://github.com/rust-lang-nursery/lazy-static.rs#license)

## Minimum supported `rustc`

`1.40.0+`

This version is explicitly tested in CI and may only be bumped in new minor versions. Any changes to the supported minimum version will be called out in the release notes.


## Getting Started

[lazy-static.rs is available on crates.io](https://crates.io/crates/lazy_static).
It is recommended to look there for the newest released version, as well as links to the newest builds of the docs.

At the point of the last update of this README, the latest published version could be used like this:

Add the following dependency to your Cargo manifest...

```toml
[dependencies]
lazy_static = "1.5.0"
```

...and see the [docs](https://docs.rs/lazy_static) for how to use it.

# Example

```rust
use lazy_static::lazy_static;
use std::collections::HashMap;

lazy_static! {
    static ref HASHMAP: HashMap<u32, &'static str> = {
        let mut m = HashMap::new();
        m.insert(0, "foo");
        m.insert(1, "bar");
        m.insert(2, "baz");
        m
    };
}

fn main() {
    // First access to `HASHMAP` initializes it
    println!("The entry for `0` is \"{}\".", HASHMAP.get(&0).unwrap());

    // Any further access to `HASHMAP` just returns the computed value
    println!("The entry for `1` is \"{}\".", HASHMAP.get(&1).unwrap());
}
```

# Standard library

It is now possible to easily replicate this crate's functionality in Rust's standard library with [`std::sync::LazyLock`](https://doc.rust-lang.org/std/sync/struct.LazyLock.html). The example above could also be written as:

```rust
use std::collections::HashMap;
use std::sync::LazyLock;

static HASHMAP: LazyLock<HashMap<u32, &str>> = LazyLock::new(|| {
    let mut m = HashMap::new();
    m.insert(0, "foo");
    m.insert(1, "bar");
    m.insert(2, "baz");
    m
});

fn main() {
    // First access to `HASHMAP` initializes it
    println!("The entry for `0` is \"{}\".", HASHMAP.get(&0).unwrap());

    // Any further access to `HASHMAP` just returns the computed value
    println!("The entry for `1` is \"{}\".", HASHMAP.get(&1).unwrap());
}
```

## License

Licensed under either of

 * Apache License, Version 2.0, ([LICENSE-APACHE](https://github.com/rust-lang-nursery/lazy-static.rs/blob/HEAD/LICENSE-APACHE) or https://www.apache.org/licenses/LICENSE-2.0)
 * MIT license ([LICENSE-MIT](https://github.com/rust-lang-nursery/lazy-static.rs/blob/HEAD/LICENSE-MIT) or https://opensource.org/licenses/MIT)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be dual licensed as above, without any
additional terms or conditions.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:lazy-static-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`rust-lang-nursery/lazy-static.rs`](https://github.com/rust-lang-nursery/lazy-static.rs). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the lazy-static.rs authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`rust-lang-nursery/lazy-static.rs`](https://github.com/rust-lang-nursery/lazy-static.rs) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
