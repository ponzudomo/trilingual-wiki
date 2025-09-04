package io.github.nicolasraoul.rosette

import org.junit.Test
import org.junit.Assert.*

/**
 * Test for random article functionality
 */
class RandomArticleTest {

    @Test
    fun `random article API response parsing works`() {
        // Test that our data classes can parse a typical random article response
        val sampleResponse = RandomArticlesResponse(
            query = RandomQueryResponse(
                random = listOf(
                    RandomArticle(id = 123, title = "Test Article"),
                    RandomArticle(id = 456, title = "Another Article")
                )
            )
        )
        
        assertEquals(2, sampleResponse.query?.random?.size)
        assertEquals("Test Article", sampleResponse.query?.random?.first()?.title)
        assertEquals(123, sampleResponse.query?.random?.first()?.id)
    }

    @Test
    fun `empty random response is handled`() {
        val emptyResponse = RandomArticlesResponse(query = null)
        assertNull(emptyResponse.query)
        
        val emptyRandomList = RandomArticlesResponse(
            query = RandomQueryResponse(random = emptyList())
        )
        assertEquals(0, emptyRandomList.query?.random?.size)
    }

    @Test
    fun `disambiguation page data structure works`() {
        // Test that our EntityClaim structure can represent disambiguation page data
        val disambiguationClaim = Claim(
            mainsnak = MainSnak(
                datavalue = DataValue(
                    value = mapOf("id" to "Q4167410") // Wikimedia disambiguation page
                )
            )
        )
        
        val entity = EntityClaim(
            id = "Q123",
            claims = mapOf("P31" to listOf(disambiguationClaim)),
            sitelinks = null,
            labels = null
        )
        
        // Verify the structure can hold disambiguation data
        assertNotNull(entity.claims)
        assertTrue(entity.claims!!.containsKey("P31"))
        assertEquals(1, entity.claims!!["P31"]?.size)
        
        val claim = entity.claims!!["P31"]?.first()
        assertNotNull(claim?.mainsnak?.datavalue?.value)
        
        // This verifies our data structure can represent the disambiguation check
        val dataValue = claim?.mainsnak?.datavalue?.value as? Map<*, *>
        assertEquals("Q4167410", dataValue?.get("id"))
    }
}