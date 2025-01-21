package com.madslee.nullcheck

import com.madslee.nullcheck.NullCheck.Companion.checkNulls

data class Person(
    var age: Int?
) {
    init { checkNulls(this) }
}
