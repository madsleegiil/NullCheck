package com.madslee.nullcheck

import org.slf4j.LoggerFactory
import javax.sql.DataSource

class NullCheck private constructor(dataSource: DataSource) {
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

        fun checkNulls() {
            check(nullCheck != null) { "Can't use NullCheck before having set it up" }
            nullCheck!!.checkNulls()
        }
    }

    private fun checkNulls() {

    }
}