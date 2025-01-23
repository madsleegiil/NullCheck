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
    fun `Instantiating Person-object with null for age shall be counted in the database`() {
        Person(null)

        val nullCheckClass = getNullCheckClasses(dataSource)
        nullCheckClass shouldHaveSize 1
        nullCheckClass[0].className shouldBe "Person"
        nullCheckClass[0].numberOfInstantiations shouldBe 1

        val classFields = nullCheckClass.first().nullCheckFields
        classFields shouldHaveSize 1
        classFields[0].fieldName shouldBe "age"
        classFields[0].numberOfTimesNull shouldBe 1
    }
}