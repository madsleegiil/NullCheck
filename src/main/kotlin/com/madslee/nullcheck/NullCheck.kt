package com.madslee.nullcheck

import org.slf4j.LoggerFactory
import javax.sql.DataSource

class NullCheck private constructor(private val dataSource: DataSource) {
    companion object {
        private val logger = LoggerFactory.getLogger(NullCheck::class.java)

        @Volatile
        private var nullCheck: NullCheck? = null

        fun setUp(dataSource: DataSource) {
            if (nullCheck == null) {
                nullCheck = NullCheck(dataSource)
                logger.info("Finished setting up NullCheck")
            } else {
                logger.info("Trying to set up NullCheck, but NullCheck has already been set up")
            }
        }

        fun checkNulls(any: Any) {
            check(nullCheck != null) { "Can't use NullCheck before having set it up" }
            nullCheck!!.checkNulls(any)
        }
    }

    private fun checkNulls(any: Any) {
        val className = any::class.java.simpleName
        val fields = any::class.java.declaredFields
        fields.forEach { field ->
            val originalAccesibility = field.isAccessible
            field.isAccessible = true
            val isNull = field.get(any) == null
            field.isAccessible = originalAccesibility
            storeResult(NewNullCheck(className, field.name, isNull))
        }
    }

    private fun storeResult(newNullCheck: NewNullCheck) {
        dataSource.connection.use { connection ->
            val sql = ""
        }
        logger.info("To be stored!")
    }

    data class NewNullCheck(
        val className: String,
        val fieldName: String,
        val isNull: Boolean
    )
}