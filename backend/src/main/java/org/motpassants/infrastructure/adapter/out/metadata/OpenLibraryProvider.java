package org.motpassants.infrastructure.adapter.out.metadata;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.metadata.AuthorMetadata;
import org.motpassants.domain.core.model.metadata.BookMetadata;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.port.out.metadata.MetadataProviderPort;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Open Library implementation of MetadataProviderPort.
 * Provides book metadata from Open Library API (openlibrary.org).
 */
@ApplicationScoped
public class OpenLibraryProvider implements MetadataProviderPort {
    
    private static final String BASE_URL = "https://openlibrary.org/api";
    private static final String SEARCH_URL = "https://openlibrary.org/search.json";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_RESULTS = 10;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LoggingPort loggingPort;
    
    @Inject
    public OpenLibraryProvider(ObjectMapper objectMapper, LoggingPort loggingPort) {
        this.objectMapper = objectMapper;
        this.loggingPort = loggingPort;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();
    }
    
    @Override
    public String getProviderId() {
        return "open-library";
    }
    
    @Override
    public String getProviderName() {
        return "Open Library";
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public Optional<BookMetadata> findByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            String cleanIsbn = cleanIsbn(isbn);
            String url = BASE_URL + "/books?bibkeys=ISBN:" + URLEncoder.encode(cleanIsbn, StandardCharsets.UTF_8) + "&format=json&jscmd=data";
            
            String response = makeRequest(url);
            if (response == null) {
                return Optional.empty();
            }
            
            JsonNode root = objectMapper.readTree(response);
            String key = "ISBN:" + cleanIsbn;
            
            if (root.has(key)) {
                JsonNode bookData = root.get(key);
                return Optional.of(parseBookMetadata(bookData));
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            loggingPort.error("Error searching Open Library by ISBN: " + isbn, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<BookMetadata> searchByTitle(String title, String author) {
        if (title == null || title.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            StringBuilder query = new StringBuilder();
            query.append("title=").append(URLEncoder.encode(title.trim(), StandardCharsets.UTF_8));
            
            if (author != null && !author.trim().isEmpty()) {
                query.append("&author=").append(URLEncoder.encode(author.trim(), StandardCharsets.UTF_8));
            }
            
            String url = SEARCH_URL + "?" + query + "&limit=" + MAX_RESULTS;
            
            String response = makeRequest(url);
            if (response == null) {
                return List.of();
            }
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode docs = root.get("docs");
            
            List<BookMetadata> results = new ArrayList<>();
            if (docs != null && docs.isArray()) {
                for (JsonNode doc : docs) {
                    try {
                        results.add(parseSearchResult(doc));
                    } catch (Exception e) {
                        loggingPort.error("Error parsing Open Library search result", e);
                    }
                }
            }
            
            return results;
            
        } catch (Exception e) {
            loggingPort.error("Error searching Open Library by title: " + title, e);
            return List.of();
        }
    }
    
    @Override
    public Optional<BookMetadata> findByProviderId(String providerSpecificId) {
        if (providerSpecificId == null || providerSpecificId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            String url = BASE_URL + "/books?bibkeys=OLID:" + URLEncoder.encode(providerSpecificId, StandardCharsets.UTF_8) + "&format=json&jscmd=data";
            
            String response = makeRequest(url);
            if (response == null) {
                return Optional.empty();
            }
            
            JsonNode root = objectMapper.readTree(response);
            String key = "OLID:" + providerSpecificId;
            
            if (root.has(key)) {
                JsonNode bookData = root.get(key);
                return Optional.of(parseBookMetadata(bookData));
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            loggingPort.error("Error getting Open Library by ID: " + providerSpecificId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public boolean testConnection() {
        try {
            // Test with a simple query that should always work
            String url = SEARCH_URL + "?title=test&limit=1";
            String response = makeRequest(url);
            return response != null && response.contains("\"docs\":");
        } catch (Exception e) {
            loggingPort.error("Open Library connection test failed", e);
            return false;
        }
    }
    
    @Override
    public int getPriority() {
        return 2; // Medium priority provider
    }
    
    private String makeRequest(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                loggingPort.warn("Open Library API returned status: " + response.statusCode() + " for URL: " + url);
                return null;
            }
            
        } catch (IOException | InterruptedException e) {
            loggingPort.error("Error making request to Open Library API: " + url, e);
            return null;
        }
    }
    
    private BookMetadata parseBookMetadata(JsonNode bookData) {
        BookMetadata.Builder builder = BookMetadata.builder()
            .providerId(getProviderId())
            .providerName(getProviderName())
            .confidence(0.85); // Good confidence for Open Library
        
        // Basic info
        if (bookData.has("title")) {
            builder.title(bookData.get("title").asText());
        }
        
        if (bookData.has("subtitle")) {
            builder.subtitle(bookData.get("subtitle").asText());
        }
        
        // Publishers
        JsonNode publishers = bookData.get("publishers");
        if (publishers != null && publishers.isArray() && publishers.size() > 0) {
            builder.publisher(publishers.get(0).get("name").asText());
        }
        
        // Publication date
        if (bookData.has("publish_date")) {
            String dateStr = bookData.get("publish_date").asText();
            LocalDate publicationDate = parseDate(dateStr);
            if (publicationDate != null) {
                builder.publicationDate(publicationDate);
                builder.publicationYear(publicationDate.getYear());
            }
        }
        
        // Authors
        JsonNode authors = bookData.get("authors");
        if (authors != null && authors.isArray()) {
            List<AuthorMetadata> authorList = new ArrayList<>();
            for (JsonNode author : authors) {
                String name = author.get("name").asText();
                authorList.add(AuthorMetadata.author(name));
            }
            builder.authors(authorList);
        }
        
        // ISBNs
        JsonNode identifiers = bookData.get("identifiers");
        if (identifiers != null) {
            JsonNode isbn13 = identifiers.get("isbn_13");
            if (isbn13 != null && isbn13.isArray() && isbn13.size() > 0) {
                String isbn13Value = isbn13.get(0).asText();
                builder.isbn13(isbn13Value);
                builder.isbn(isbn13Value);
            }
            
            JsonNode isbn10 = identifiers.get("isbn_10");
            if (isbn10 != null && isbn10.isArray() && isbn10.size() > 0) {
                builder.isbn10(isbn10.get(0).asText());
                if (builder.build().isbn() == null) {
                    builder.isbn(isbn10.get(0).asText());
                }
            }
        }
        
        // Subjects
        JsonNode subjects = bookData.get("subjects");
        if (subjects != null && subjects.isArray()) {
            Set<String> subjectSet = new HashSet<>();
            for (JsonNode subject : subjects) {
                subjectSet.add(subject.get("name").asText());
            }
            builder.subjects(subjectSet);
        }
        
        // Cover
        if (bookData.has("cover")) {
            JsonNode cover = bookData.get("cover");
            if (cover.has("small")) {
                builder.smallThumbnail(cover.get("small").asText());
            }
            if (cover.has("medium")) {
                builder.thumbnail(cover.get("medium").asText());
            }
            if (cover.has("large")) {
                builder.largeImage(cover.get("large").asText());
            }
        }
        
        // Open Library ID
        if (bookData.has("key")) {
            String key = bookData.get("key").asText();
            if (key.startsWith("/books/")) {
                builder.openLibraryId(key.substring("/books/".length()));
            }
        }
        
        return builder.build();
    }
    
    private BookMetadata parseSearchResult(JsonNode doc) {
        BookMetadata.Builder builder = BookMetadata.builder()
            .providerId(getProviderId())
            .providerName(getProviderName())
            .confidence(0.75); // Lower confidence for search results
        
        // Basic info
        if (doc.has("title")) {
            builder.title(doc.get("title").asText());
        }
        
        if (doc.has("subtitle")) {
            builder.subtitle(doc.get("subtitle").asText());
        }
        
        // Authors
        JsonNode authorNames = doc.get("author_name");
        if (authorNames != null && authorNames.isArray()) {
            List<AuthorMetadata> authorList = new ArrayList<>();
            for (JsonNode authorName : authorNames) {
                authorList.add(AuthorMetadata.author(authorName.asText()));
            }
            builder.authors(authorList);
        }
        
        // Publishers
        JsonNode publishers = doc.get("publisher");
        if (publishers != null && publishers.isArray() && publishers.size() > 0) {
            builder.publisher(publishers.get(0).asText());
        }
        
        // Publication year
        if (doc.has("first_publish_year")) {
            int year = doc.get("first_publish_year").asInt();
            builder.publicationYear(year);
            builder.publicationDate(LocalDate.of(year, 1, 1));
        }
        
        // ISBNs
        JsonNode isbn = doc.get("isbn");
        if (isbn != null && isbn.isArray() && isbn.size() > 0) {
            String isbnValue = isbn.get(0).asText();
            builder.isbn(isbnValue);
            if (isbnValue.length() == 13) {
                builder.isbn13(isbnValue);
            } else if (isbnValue.length() == 10) {
                builder.isbn10(isbnValue);
            }
        }
        
        // Subjects
        JsonNode subjects = doc.get("subject");
        if (subjects != null && subjects.isArray()) {
            Set<String> subjectSet = new HashSet<>();
            for (JsonNode subject : subjects) {
                subjectSet.add(subject.asText());
            }
            builder.subjects(subjectSet);
        }
        
        // Open Library ID
        if (doc.has("key")) {
            String key = doc.get("key").asText();
            if (key.startsWith("/works/")) {
                builder.openLibraryId(key.substring("/works/".length()));
            }
        }
        
        return builder.build();
    }
    
    private String cleanIsbn(String isbn) {
        return isbn.replaceAll("[^0-9X]", "").toUpperCase();
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Open Library dates are often just years or "Month Year" format
            if (dateStr.matches("\\d{4}")) {
                // Year only
                return LocalDate.of(Integer.parseInt(dateStr), 1, 1);
            } else if (dateStr.matches("\\w+ \\d{4}")) {
                // Month Year
                String[] parts = dateStr.split(" ");
                int year = Integer.parseInt(parts[1]);
                return LocalDate.of(year, 1, 1); // Simplified
            } else {
                // Try to parse as date
                return LocalDate.parse(dateStr);
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            loggingPort.warn("Could not parse date: " + dateStr);
            return null;
        }
    }
}