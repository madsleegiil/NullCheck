package com.madslee.nullcheck

import com.madslee.nullcheck.NullCheck.ClassData

internal class ClassInspector(private val any: Any) {

    fun inspectClass(): ClassData =
        ClassData(className = getClassName(), fields = getFields())

    private fun getClassName() = any::class.java.simpleName

    private fun getFields(): List<NullCheck.ClassField> = any::class.java.declaredFields.map { field ->
        val originalAccesibility = field.canAccess(any)
        field.isAccessible = true

        NullCheck.ClassField(
            name = field.name,
            isNull = field.get(any) == null
        ).also { field.isAccessible = originalAccesibility }
    }
}
