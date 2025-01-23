package com.madslee.nullcheck

import com.madslee.nullcheck.NullCheck.ClassData
import java.sql.Connection
import javax.sql.DataSource

internal class Repository(private val dataSource: DataSource) {

    fun storeResult(classData: ClassData) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            storeClass(className = classData.className, connection)
            classData.fields.forEach {
                storeField(className = classData.className, field = it, connection = connection)
            }
            connection.commit()
        }
    }

    private fun storeClass(className: String, connection: Connection) {
        val classInstantiationStatement = connection.prepareStatement("""
                INSERT INTO nullcheck_class(class_name, number_of_instantiations)
                VALUES (?, 1)
                ON CONFLICT (class_name) DO UPDATE 
                    SET number_of_instantiations = nullcheck_class.number_of_instantiations + 1 
            """.trimIndent())
        classInstantiationStatement.setString(1, className)
        classInstantiationStatement.execute()
    }

    private fun storeField(className: String, field: NullCheck.ClassField, connection: Connection) {
        val fieldStatement = connection.prepareStatement("""
                    INSERT INTO nullcheck_field(class_name, field_name, number_of_times_null)
                    VALUES (?, ?, ?)
                    ON CONFLICT (class_name, field_name) DO UPDATE 
                        SET number_of_times_null = nullcheck_field.number_of_times_null + ?
                """.trimIndent())
        fieldStatement.setString(1, className)
        fieldStatement.setString(2, field.name)
        val nullIntValue = if (field.isNull) 1 else 0
        fieldStatement.setInt(3, nullIntValue)
        fieldStatement.setInt(4, nullIntValue)
        fieldStatement.execute()
    }
}