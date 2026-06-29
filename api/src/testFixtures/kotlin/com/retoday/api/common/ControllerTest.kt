package com.retoday.api.common

import com.retoday.core.global.extension.limit
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockkStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext

@AutoConfigureRestDocs
abstract class ControllerTest(
    private val version: Int = 1
) : DescribeSpec() {
    private companion object {
        const val RATE_LIMIT_EXTENSION_CLASS = "com.retoday.core.global.extension.RateLimitExtensionsKt"
    }

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var restDocumentationContextProvider: RestDocumentationContextProvider

    protected val webClient: WebTestClient by lazy {
        MockMvcWebTestClient
            .bindToApplicationContext(webApplicationContext)
            .configureClient()
            .baseUrl("/v$version")
            .filter(WebTestClientRestDocumentation.documentationConfiguration(restDocumentationContextProvider))
            .build()
    }

    override suspend fun beforeSpec(spec: Spec) {
        mockkStatic(RATE_LIMIT_EXTENSION_CLASS)
        every { limit(any(), any(), any(), any<() -> Any?>()) } answers { lastArg<() -> Any?>().invoke() }
    }
}
