package support

import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

abstract class KoinFlowTest: KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.Companion.create {
        modules(koinModule())
    }

    protected abstract fun koinModule(): Module

}