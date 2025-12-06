package com.wajtr.baf.core.datasource

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport
import org.springframework.transaction.CannotCreateTransactionException
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier
import javax.sql.DataSource

/**
 *
 *
 * Extension of Spring's [DataSourceTransactionManager] which makes it
 * possible to have "transactions with context". It's possible, using callbacks, to pass additional connection
 * configuration parameters, which are set for the datasource connection at the beginning of each transaction and are
 * available in SQL commands through core.session_param(text) function during the transaction.
 * Note that such settings are not available after transaction ends, no matter if it was committed or rolled back.
 *
 *
 *
 * Use setTransactionContextProperty(String, Supplier) to add new business context properties when Spring context is created.
 *
 *
 * @author Bretislav Wajtr
 */
class ContextAwareTransactionManager(dataSource: DataSource) : DataSourceTransactionManager(dataSource) {
    /**
     * Map where key is property name and value is a implementation of Supplier<String> interface -> so short piece of code
     * which does not need arguments and returns String.
    </String> */
    private val properties: MutableMap<String, Supplier<String?>> = ConcurrentHashMap()

    override fun doBegin(transaction: Any, definition: TransactionDefinition) {
        super.doBegin(transaction, definition)
        setContextToTransaction(transaction)
    }

    override fun doResume(transaction: Any?, suspendedResources: Any) {
        super.doResume(transaction, suspendedResources)
        // set business context also on resumed transactions, in case that different connection is used for resumed
        // transaction and the context is lost. Should not happen with PostgreSQL driver, but might happen with other
        // databases. So just in case set the context again.
        setContextToTransaction(transaction)
    }

    /**
     * Use this method to add new property key and it's supplier. Properties are evaluated (i.e. their suppliers are
     * called) at the beginning of each transaction and usually return data related to current "business context"
     * (currently logged-in user, current environment etc.). These data are then set to the running SQL transaction and
     * are available within SQL commands and statements through core.session_param(text) function call. Note that
     * the session properties set through this mechanism are LOCAL, meaning that they are available only within running
     * transaction and are automatically unset once the transaction ends (and it doesn't matter if it commits, roll
     * backs or fails).
     *
     * @param propertyKey           This key will be used as name of session parameter. Note that for PostgreSQL this
     * key need to contain at least one dot (e.g. "session.my_key" as top-level properties
     * are not allowed to be set by ordinary user (e.g. "sessionMyKey" is disallowed)
     * @param propertyValueSupplier Supplier which will compute value of the business context property at the beginning
     * of each transaction. The supplier is called when new transaction is started to
     * get the current value of the property. This value is then set to running transaction
     * and is available through SQL call to pg_catalog.current_setting(text, boolean)
     * function.
     */
    fun setTransactionContextProperty(propertyKey: String, propertyValueSupplier: Supplier<String?>) {
        properties[propertyKey] = propertyValueSupplier
    }

    fun resetCurrentTransactionContext() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            setContextToTransaction(doGetTransaction())
        }
    }

    private fun setContextToTransaction(transaction: Any?) {
        val txObject = transaction as JdbcTransactionObjectSupport

        if (txObject.hasConnectionHolder()) {
            val connection = txObject.connectionHolder.connection

            try {
                // use() will close the statement automatically
                connection.prepareCall("{call set_config(?, ?, TRUE)}").use { statement ->
                    properties.forEach { (propertyName: String, propertyValueSupplier: Supplier<String?>) ->
                        try {
                            val propertyValue = propertyValueSupplier.get()
                            if (propertyValue != null) {
                                statement.setString(1, propertyName)
                                statement.setString(2, propertyValue)
                                LOG.debug("Setting {}={} on connection {}", propertyName, propertyValue, connection)
                                statement.execute()
                                statement.resultSet.close()
                            }
                        } catch (ex: Throwable) {
                            throw CannotCreateTransactionException(
                                "Could not set business context to transaction $txObject",
                                ex
                            )
                        }
                    }
                }
            } catch (ex: SQLException) {
                throw CannotCreateTransactionException("Could not set business context to transaction $txObject", ex)
            }
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ContextAwareTransactionManager::class.java)
    }
}
