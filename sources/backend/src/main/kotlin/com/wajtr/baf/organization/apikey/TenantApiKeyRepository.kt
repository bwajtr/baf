package com.wajtr.baf.organization.apikey

import com.wajtr.baf.db.jooq.tables.references.TENANT_API_KEY
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

data class TenantApiKey(
    val id: UUID,
    val apiKey: String,
    val createdAt: Instant,
    val tenantId: UUID
)

@Service
@Transactional
class TenantApiKeyRepository(
    private val dslContext: DSLContext
) {

    fun findByTenantId(tenantId: UUID): TenantApiKey? {
        return dslContext.selectFrom(TENANT_API_KEY)
            .where(TENANT_API_KEY.TENANT_ID.eq(tenantId))
            .fetchOne { record ->
                TenantApiKey(
                    id = record.id!!,
                    apiKey = record.apiKey!!,
                    createdAt = record.createdAt!!.toInstant(),
                    tenantId = record.tenantId!!
                )
            }
    }

    fun insert(apiKey: String, tenantId: UUID): TenantApiKey {
        val record = dslContext.insertInto(TENANT_API_KEY)
            .set(TENANT_API_KEY.API_KEY, apiKey)
            .set(TENANT_API_KEY.TENANT_ID, tenantId)
            .returning()
            .fetchOne()!!

        return TenantApiKey(
            id = record.id!!,
            apiKey = record.apiKey!!,
            createdAt = record.createdAt!!.toInstant(),
            tenantId = record.tenantId!!
        )
    }

    fun deleteByTenantId(tenantId: UUID): Int {
        return dslContext.deleteFrom(TENANT_API_KEY)
            .where(TENANT_API_KEY.TENANT_ID.eq(tenantId))
            .execute()
    }
}
