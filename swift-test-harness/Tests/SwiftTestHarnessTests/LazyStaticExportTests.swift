#if canImport(Testing)
import Testing
import LazyStatic

@Suite("LazyStatic Export Tests")
struct LazyStaticExportTests {
    @Test("Swift module loads and imports cleanly")
    func swiftModuleLoads() {
        #expect(Bool(true))
    }
}
#else
import XCTest
import LazyStatic

final class LazyStaticExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "LazyStatic swift module imported cleanly")
    }
}
#endif

