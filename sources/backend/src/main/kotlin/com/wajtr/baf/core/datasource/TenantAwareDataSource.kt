package com.wajtr.baf.core.datasource

import com.wajtr.baf.core.datasource.TenantAwareDataSource.Companion.SESSION_TENANT_ID
import com.wajtr.baf.core.datasource.TenantAwareDataSource.Companion.SESSION_USER_ID
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.NoAuthenticatedUserException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.*
import javax.sql.DataSource

/**
 * A [DataSource] wrapper that sets PostgreSQL session parameters (`session.tenant.id` and `session.user.id`)
 * on every connection borrowed from the underlying connection pool. These session parameters are used by
 * PostgreSQL Row Level Security policies (via `current_tenant()` function) to enforce multi-tenant data isolation.
 *
 * The tenant and user IDs are resolved via the [Identity] bean at the time the connection is borrowed.
 * When the connection is returned to the pool (via [Connection.close]), the session parameters are cleared
 * to prevent context leakage between requests.
 *
 * This approach is framework-agnostic: it works with jOOQ, JPA, raw JDBC, or any other data access technology
 * that obtains connections through this DataSource. It does not depend on Spring's transaction management,
 * so it works correctly regardless of `@Transactional` annotations, auto-commit mode, or transaction boundaries.
 *
 * @param delegate The underlying DataSource (typically HikariCP) to wrap
 *
 * @author Bretislav Wajtr
 */
class TenantAwareDataSource(private val delegate: DataSource, private val identity: Identity) : DataSource by delegate {

    override fun getConnection(): Connection {
        val connection = delegate.connection
        return applyTenantContext(connection)
    }

    override fun getConnection(username: String?, password: String?): Connection {
        val connection = delegate.getConnection(username, password)
        return applyTenantContext(connection)
    }

    private fun applyTenantContext(connection: Connection): Connection {
        val tenantId = resolveAuthenticatedTenantId()
        val userId = resolveAuthenticatedUserId()

        if (tenantId != null || userId != null) {
            try {
                setSessionParameters(connection, tenantId, userId)
            } catch (ex: SQLException) {
                // If setting context fails, close the connection and propagate the error.
                // Do not return a connection without the expected context.
                try {
                    connection.close()
                } catch (closeEx: SQLException) {
                    ex.addSuppressed(closeEx)
                }
                throw ex
            }
        }

        return TenantAwareConnection(connection)
    }

    private fun resolveAuthenticatedUserId(): String? = try {
        identity.authenticatedUser.id.toString()
    } catch (_: NoAuthenticatedUserException) {
        null
    }

    private fun resolveAuthenticatedTenantId(): String? = identity.authenticatedTenant?.id?.toString()

    private fun setSessionParameters(connection: Connection, tenantId: String?, userId: String?) {
        connection.prepareStatement("SELECT set_config(?, ?, false)").use { statement ->
            if (tenantId != null) {
                statement.setString(1, SESSION_TENANT_ID)
                statement.setString(2, tenantId)
                statement.execute()
                LOG.debug("Set {}={} on connection {}", SESSION_TENANT_ID, tenantId, connection)
            }
            if (userId != null) {
                statement.setString(1, SESSION_USER_ID)
                statement.setString(2, userId)
                statement.execute()
                LOG.debug("Set {}={} on connection {}", SESSION_USER_ID, userId, connection)
            }
        }
    }

    override fun createShardingKeyBuilder(): ShardingKeyBuilder? = delegate.createShardingKeyBuilder()
    override fun createConnectionBuilder(): ConnectionBuilder? = delegate.createConnectionBuilder()

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(TenantAwareDataSource::class.java)
        const val SESSION_TENANT_ID = "session.tenant.id"
        const val SESSION_USER_ID = "session.user.id"
    }
}

/**
 * A [Connection] wrapper that clears PostgreSQL session parameters when the connection is returned
 * to the pool (i.e., when [close] is called). This prevents tenant/user context from leaking
 * between requests that reuse the same pooled connection.
 */
private class TenantAwareConnection(private val delegate: Connection) : Connection by delegate {

    override fun close() {
        try {
            clearSessionParameters()
        } catch (ex: SQLException) {
            LOG.warn("Failed to clear session parameters on connection close", ex)
            // Still close the connection — HikariCP will handle the broken connection
        }
        delegate.close()
    }

    override fun beginRequest() {
        delegate.beginRequest()
    }

    override fun endRequest() {
        delegate.endRequest()
    }

    override fun setShardingKeyIfValid(
        shardingKey: ShardingKey?,
        superShardingKey: ShardingKey?,
        timeout: Int
    ): Boolean {
        return delegate.setShardingKeyIfValid(shardingKey, superShardingKey, timeout)
    }

    override fun setShardingKeyIfValid(shardingKey: ShardingKey?, timeout: Int): Boolean {
        return delegate.setShardingKeyIfValid(shardingKey, timeout)
    }

    override fun setShardingKey(shardingKey: ShardingKey?, superShardingKey: ShardingKey?) {
        delegate.setShardingKey(shardingKey, superShardingKey)
    }

    override fun setShardingKey(shardingKey: ShardingKey?) {
        delegate.setShardingKey(shardingKey)
    }

    private fun clearSessionParameters() {
        if (!delegate.isClosed) {
            LOG.debug(
                "Clearing {} and {} session parameters on connection {}",
                SESSION_TENANT_ID,
                SESSION_USER_ID,
                delegate
            )
            delegate.prepareStatement("SELECT set_config(?, '', false), set_config(?, '', false)").use { statement ->
                statement.setString(1, SESSION_TENANT_ID)
                statement.setString(2, SESSION_USER_ID)
                statement.execute()
            }
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(TenantAwareConnection::class.java)
    }
}
