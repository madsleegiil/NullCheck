package com.madslee.nullcheck

import java.sql.ResultSet
import javax.sql.DataSource

data class NullCheckRow(
    val className: String,
    val fieldName: String,
    val numberOfTimesNull: Int,
    val numberOfTimesNotNull: Int,
)

fun getNullCheckRows(dataSource: DataSource): List<NullCheckRow> {
    return dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT * FROM nullcheck").use { statement ->
            statement.executeQuery().use { resultSet -> resultSet.getNullCheckRows() }
        }
    }
}

private fun ResultSet.getNullCheckRows(): List<NullCheckRow> {
    return generateSequence {
        if (this.next()) {
            NullCheckRow(
                className = this.getString("class_name"),
                fieldName = this.getString("field_name"),
                numberOfTimesNull = this.getInt("numberOf_times_null"),
                numberOfTimesNotNull = this.getInt("numberOf_timesNotNull"),
            )
        } else {
            null
        }
    }.toList()
}
