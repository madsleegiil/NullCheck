package com.madslee.nullcheck

import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

data class NullCheckClass(
    val className: String,
    val numberOfInstantiations: Long,
    val nullCheckFields: List<NullCheckField>
)

data class NullCheckField(
    val fieldName: String,
    val numberOfTimesNull: Long,
)

fun getNullCheckClasses(dataSource: DataSource): List<NullCheckClass> {
    return dataSource.connection.use { connection ->
        val nullCheckClassStatement = connection.prepareStatement("select * from nullcheck_class")
        val nullCheckClassResultSet = nullCheckClassStatement.executeQuery()

        generateSequence {
            if (nullCheckClassResultSet.next()) {
                val className = nullCheckClassResultSet.getString("class_name")
                val numberOfInstantiations = nullCheckClassResultSet.getLong("number_of_instantiations")
                val fields = connection.getFieldsForClass(className)

                NullCheckClass(
                    className = nullCheckClassResultSet.getString("class_name"),
                    numberOfInstantiations = numberOfInstantiations,
                    nullCheckFields = fields
                )
            } else {
                null
            }
        }.toList().also {
            nullCheckClassStatement.close()
            nullCheckClassResultSet.close()
        }
    }
}

private fun Connection.getFieldsForClass(className: String): List<NullCheckField> {
    val statement = this.prepareStatement(
        """
                    select * from nullcheck_field
                    where class_name = ?
                """.trimIndent()
    )
    statement.setString(1, className)
    val resultSet = statement.executeQuery()
    return resultSet.getNullCheckFieldRows().also {
        statement.close()
        resultSet.close()
    }
}

private fun ResultSet.getNullCheckFieldRows(): List<NullCheckField> {
    return generateSequence {
        if (this.next()) {
            NullCheckField(
                fieldName = this.getString("field_name"),
                numberOfTimesNull = this.getLong("number_of_times_null"),
            )
        } else {
            null
        }
    }.toList()
}
