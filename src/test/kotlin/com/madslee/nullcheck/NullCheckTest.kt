package com.madslee.nullcheck

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.sql.DataSource


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NullCheckTest {

    private val dataSource: DataSource = EmbeddedPostgres.start().postgresDatabase

    @BeforeAll
    fun beforeAll() {
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()
        NullCheck.setUp(dataSource)
    }

    @Test
    fun `Instantiating "Person" with null for age shall be counted in the database`() {
        Person(null)
        val nullCheckRows = getNullCheckRows(dataSource)
        nullCheckRows shouldHaveSize 1
        nullCheckRows[0].className shouldBe "Person"
        nullCheckRows[0].fieldName shouldBe "age"
        nullCheckRows[0].numberOfTimesNull shouldBe 1
        nullCheckRows[0].numberOfTimesNotNull shouldBe 0
    }
}