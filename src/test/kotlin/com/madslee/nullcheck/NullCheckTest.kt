package com.madslee.nullcheck

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun beforeEach() {
        dataSource.connection.use { connection ->
            connection.prepareStatement("truncate table nullcheck_field, nullcheck_class").execute()
        }
    }

    @Test
    fun `Instantiating an object shall be counted`() {
        Person(null)
        Person(1)
        Person(null)

        val nullCheckClass = getNullCheckClasses(dataSource)
        nullCheckClass shouldHaveSize 1
        nullCheckClass[0].className shouldBe "Person"
        nullCheckClass[0].numberOfInstantiations shouldBe 3
    }

    @Test
    fun `Field with null shall be counted correctly`() {
        Person(null)

        val nullCheckClass = getNullCheckClasses(dataSource)
        val classFields = nullCheckClass.first().nullCheckFields
        classFields shouldHaveSize 1
        classFields[0].fieldName shouldBe "age"
        classFields[0].numberOfTimesNull shouldBe 1
    }

    @Test
    fun `Field with not null shall be counted correctly`() {
        Person(1)

        val nullCheckClass = getNullCheckClasses(dataSource)
        val classFields = nullCheckClass.first().nullCheckFields
        classFields shouldHaveSize 1
        classFields[0].fieldName shouldBe "age"
        classFields[0].numberOfTimesNull shouldBe 0
    }

    @Test
    fun `Instantiating an object through Jackson Kotlin Module shall trigger the null check`() {
        val personJson = """
            {
                "age": 1
            }
        """.trimIndent()

        jacksonObjectMapper().readValue<Person>(personJson)

        val nullCheckClass = getNullCheckClasses(dataSource)
        nullCheckClass shouldHaveSize 1
        nullCheckClass[0].className shouldBe "Person"
        nullCheckClass[0].numberOfInstantiations shouldBe 1
    }
}