package org.motpassants.infrastructure.adapter.out.metadata.openlibrary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

/**
 * REST client for Open Library API.
 * Uses Quarkus REST client for async HTTP communication.
 */
@RegisterRestClient(configKey = "open-library-api")
public interface OpenLibraryApiClient {

    /**
     * Search for books using the Open Library Search API.
     * 
     * @param query     The search query (can be title, author, isbn, etc.)
     * @param limit     Maximum number of results to return
     * @return Future containing Open Library search response
     */
    @GET
    @Path("/search.json")
    CompletionStage<OpenLibrarySearchResponse> searchBooks(
            @QueryParam("q") String query,
            @QueryParam("limit") Integer limit
    );

    /**
     * Search for books by ISBN specifically.
     * 
     * @param isbn      The ISBN to search for
     * @param limit     Maximum number of results to return
     * @return Future containing Open Library search response
     */
    @GET
    @Path("/search.json")
    CompletionStage<OpenLibrarySearchResponse> searchByIsbn(
            @QueryParam("isbn") String isbn,
            @QueryParam("limit") Integer limit
    );

    /**
     * Search for books by title.
     * 
     * @param title     The title to search for
     * @param limit     Maximum number of results to return
     * @return Future containing Open Library search response
     */
    @GET
    @Path("/search.json")
    CompletionStage<OpenLibrarySearchResponse> searchByTitle(
            @QueryParam("title") String title,
            @QueryParam("limit") Integer limit
    );

    /**
     * Search for books by author.
     * 
     * @param author    The author to search for
     * @param limit     Maximum number of results to return
     * @return Future containing Open Library search response
     */
    @GET
    @Path("/search.json")
    CompletionStage<OpenLibrarySearchResponse> searchByAuthor(
            @QueryParam("author") String author,
            @QueryParam("limit") Integer limit
    );
}
