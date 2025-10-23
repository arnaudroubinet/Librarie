package org.motpassants.infrastructure.adapter.out.metadata.googlebooks;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

/**
 * REST client for Google Books API.
 * Uses Quarkus REST client for async HTTP communication.
 */
@RegisterRestClient(configKey = "google-books-api")
@Path("/volumes")
public interface GoogleBooksApiClient {

    /**
     * Search for books using a query string.
     * Google Books API supports queries like:
     * - isbn:1234567890
     * - intitle:Python+Programming
     * - inauthor:Guido
     *
     * @param query     The search query
     * @param maxResults Maximum number of results to return (1-40, default 10)
     * @param key       Optional API key for higher rate limits
     * @return Future containing Google Books API response
     */
    @GET
    CompletionStage<GoogleBooksResponse> searchBooks(
            @QueryParam("q") String query,
            @QueryParam("maxResults") Integer maxResults,
            @QueryParam("key") String key
    );
}
