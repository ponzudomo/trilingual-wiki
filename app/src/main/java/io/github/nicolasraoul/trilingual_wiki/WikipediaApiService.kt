package io.github.nicolasraoul.trilingual_wiki

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WikipediaApiService {
    @GET
    suspend fun getArticleInfo(
        @Url baseUrl: String,
        @Query("action") action: String = "query",
        @Query("titles") titles: String,
        @Query("format") format: String = "json",
        @Query("redirects") redirects: Int = 1
    ): Response<WikipediaResponse>

    @GET
    suspend fun getLanguageLinks(
        @Url baseUrl: String,
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "langlinks",
        @Query("titles") titles: String,
        @Query("lllimit") lllimit: Int = 500,
        @Query("format") format: String = "json"
    ): Response<WikipediaResponse>

    @GET("https://www.wikidata.org/w/api.php")
    suspend fun searchEntities(
        @Query("action") action: String = "wbsearchentities",
        @Query("search") searchTerm: String,
        @Query("language") language: String,
        @Query("limit") limit: Int = 10,
        @Query("type") type: String = "item",
        @Query("format") format: String = "json"
    ): Response<EntitySearchResponse>

    @GET("https://www.wikidata.org/w/api.php")
    suspend fun getEntityClaims(
        @Query("action") action: String = "wbgetentities",
        @Query("ids") ids: String,
        @Query("props") props: String = "info|sitelinks|aliases|labels|descriptions|claims",
        @Query("format") format: String = "json"
    ): Response<EntityClaimsResponse>
}

// Data classes for existing calls
data class WikipediaResponse(
    val query: QueryResponse?
)

data class QueryResponse(
    val pages: Map<String, PageInfo>?
)

data class PageInfo(
    val pageid: Int,
    val title: String?,
    val langlinks: List<LangLink>?,
    val missing: String? = null
)

data class LangLink(
    val lang: String,
    @SerializedName("*")
    val title: String
)

// Data classes for Wikidata entity search
data class EntitySearchResponse(
    val search: List<EntitySearchResult>
)

data class EntitySearchResult(
    val id: String,
    val label: String,
    val description: String?
)

// Data classes for Wikidata entity claims
data class EntityClaimsResponse(
    val entities: Map<String, EntityClaim>
)

data class EntityClaim(
    val id: String,
    val claims: Map<String, List<Claim>>?
)

data class Claim(
    val mainsnak: MainSnak
)

data class MainSnak(
    val datavalue: DataValue?
)

data class DataValue(
    val value: Any? // Changed to Any to handle both String and Object types
)