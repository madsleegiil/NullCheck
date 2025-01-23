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
        val fieldsNullCheckResult = fields.map { field ->
            val originalAccesibility = field.isAccessible
            field.isAccessible = true
            val isNull = field.get(any) == null
            field.isAccessible = originalAccesibility
            NullCheckField(
                field.name,
                isNull = isNull
            )
        }
        val nullCheckClass = NullCheckClass(className, fieldsNullCheckResult)
        storeResult(nullCheckClass)
    }

    private fun storeResult(nullCheckClass: NullCheckClass) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false

            val classInstantiationStatement = connection.prepareStatement("""
                INSERT INTO nullcheck_class(class_name, number_of_instantiations)
                VALUES (?, 1)
                ON CONFLICT (class_name) DO UPDATE 
                    SET number_of_instantiations = nullcheck_class.number_of_instantiations + 1 
            """.trimIndent())
            classInstantiationStatement.setString(1, nullCheckClass.className)
            classInstantiationStatement.execute()

            nullCheckClass.fields.forEach { field ->
                val fieldStatement = connection.prepareStatement("""
                    INSERT INTO nullcheck_field(class_name, field_name, number_of_times_null)
                    VALUES (?, ?, ?)
                    ON CONFLICT (class_name, field_name) DO UPDATE 
                        SET number_of_times_null = nullcheck_field.number_of_times_null + ?
                """.trimIndent())
                fieldStatement.setString(1, nullCheckClass.className)
                fieldStatement.setString(2, field.fieldName)
                val nullIntValue = if (field.isNull) 1 else 0
                fieldStatement.setInt(3, nullIntValue)
                fieldStatement.setInt(4, nullIntValue)
                fieldStatement.execute()
            }
            connection.commit()
        }
    }

    private data class NullCheckClass(
        val className: String,
        val fields: List<NullCheckField>
    )

    private data class NullCheckField(
        val fieldName: String,
        val isNull: Boolean
    )
}