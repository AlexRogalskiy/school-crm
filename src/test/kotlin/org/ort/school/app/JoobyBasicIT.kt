package org.ort.school.app

import com.github.kittinunf.fuel.httpGet
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.PostgreSQLContainer
import java.net.ServerSocket

abstract class JoobyIntegrationSpec(body: JoobyIntegrationSpec.(port: Int) -> Unit = {}) : BehaviorSpec() {

    class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>()

    private val httpPort = randomPort()
    private val httpsPort = randomPort()
    private val app = SchoolCRM()

    companion object {
        private fun randomPort() = ServerSocket(0).use { it.localPort }
        @JvmStatic
        private val postgres = KPostgreSQLContainer().also { it.start() }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        app.start(
                "server.join=false",
                "application.port=$httpPort",
                "application.securePort=$httpsPort",
                "db.url=${postgres.jdbcUrl.replace("\\?.*".toRegex(), "")}",
                "db.user=${postgres.username}",
                "db.password=${postgres.password}"
        )
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        app.stop()
    }

    init {
        this.body(httpPort)
    }
}

class JoobyFirstIT : JoobyIntegrationSpec({ port ->
    given("application started") {
        `when`("I request /") {
            val response = "http://localhost:$port/"
                    .httpGet()
                    .response()
                    .second

            then("I get status 200") {
                response.statusCode shouldBe 200
            }
        }
    }

})
