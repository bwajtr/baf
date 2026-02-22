package com.wajtr.baf.core

import com.wajtr.baf.core.datasource.TenantAwareDataSource
import jakarta.annotation.PostConstruct
import org.jooq.ExecuteContext
import org.jooq.ExecuteListener
import org.jooq.ExecuteListenerProvider
import org.jooq.conf.RenderKeywordCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DefaultExecuteListenerProvider
import org.jooq.tools.StopWatch
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.flyway.autoconfigure.FlywayDataSource
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.*
import javax.sql.DataSource

/**
 * @author Bretislav Wajtr
 */
@Configuration
class DataSourceConfiguration {
    @Bean
    @Primary
    fun primaryDataSource(): DataSource {
        return TenantAwareDataSource(hikariDataSource())
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    fun hikariDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean
    @FlywayDataSource
    @ConfigurationProperties(prefix = "spring.migrations.datasource")
    fun migrationsDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean
    @Primary
    fun jooqSettings(): Settings {
        val ret = Settings()
        ret.withRenderSchema(true)
        ret.setRenderFormatted(true)
        ret.setRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
        ret.setRenderKeywordCase(RenderKeywordCase.UPPER)

        return ret
    }
}

@Configuration
class CoreDataSourceConfigurationAdditions(private val jooqConfiguration: org.jooq.Configuration) {
    @PostConstruct
    fun addJooqPerformancePrinter() {
        val existingProviders = Arrays.asList<ExecuteListenerProvider?>(*jooqConfiguration.executeListenerProviders())

        val newProviders: MutableList<ExecuteListenerProvider?> = ArrayList<ExecuteListenerProvider?>()
        newProviders.add(DefaultExecuteListenerProvider(CustomStopWatchListener()))
        newProviders.addAll(existingProviders)

        jooqConfiguration.set(*newProviders.toTypedArray<ExecuteListenerProvider?>())
    }
}

/**
 * JOOQ execution listener to be used for monitoring Jooq and Database performance (prints out execution times)
 */
class CustomStopWatchListener : ExecuteListener {
    protected val watch: StopWatch = StopWatch()

    override fun start(ctx: ExecuteContext?) {
        watch.splitDebug("Initialising")
    }

    override fun renderStart(ctx: ExecuteContext?) {
        watch.splitTrace("Rendering query")
    }

    override fun renderEnd(ctx: ExecuteContext?) {
        watch.splitTrace("Query rendered")
    }

    override fun prepareStart(ctx: ExecuteContext?) {
        watch.splitTrace("Preparing statement")
    }

    override fun prepareEnd(ctx: ExecuteContext?) {
        watch.splitTrace("Statement prepared")
    }

    override fun bindStart(ctx: ExecuteContext?) {
        watch.splitTrace("Binding variables")
    }

    override fun bindEnd(ctx: ExecuteContext?) {
        watch.splitTrace("Variables bound")
    }

    override fun executeStart(ctx: ExecuteContext?) {
        watch.splitTrace("Executing query")
    }

    override fun executeEnd(ctx: ExecuteContext?) {
        watch.splitDebug("Query executed")
    }

    override fun outStart(ctx: ExecuteContext?) {
        watch.splitDebug("Fetching out values")
    }

    override fun outEnd(ctx: ExecuteContext?) {
        watch.splitDebug("Out values fetched")
    }

    override fun fetchStart(ctx: ExecuteContext?) {
        watch.splitTrace("Fetching results")
    }

    override fun resultStart(ctx: ExecuteContext?) {
        watch.splitTrace("Fetching result")
    }

    override fun recordStart(ctx: ExecuteContext?) {
        watch.splitTrace("Fetching record")
    }

    override fun recordEnd(ctx: ExecuteContext?) {
        watch.splitTrace("Record fetched")
    }

    override fun resultEnd(ctx: ExecuteContext?) {
        watch.splitTrace("Result fetched")
    }

    override fun fetchEnd(ctx: ExecuteContext?) {
        watch.splitTrace("Results fetched")
    }

    override fun end(ctx: ExecuteContext?) {
        watch.splitDebug("Finishing")
    }

    override fun exception(ctx: ExecuteContext?) {
        watch.splitDebug("Exception")
    }

    override fun warning(ctx: ExecuteContext?) {
        watch.splitDebug("Warning")
    }

    companion object {
        /**
         * Generated UID
         */
        private const val serialVersionUID = 7399239846062763212L
    }
}


