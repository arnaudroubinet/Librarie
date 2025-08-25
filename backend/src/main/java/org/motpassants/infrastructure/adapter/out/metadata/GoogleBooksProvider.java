package org.motpassants.infrastructure.adapter.out.metadata;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.metadata.AuthorMetadata;
import org.motpassants.domain.core.model.metadata.BookMetadata;
import org.motpassants.domain.port.out.ConfigurationPort;
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
 * Google Books API implementation of MetadataProviderPort.
 * Provides book metadata from Google Books API without requiring API key for basic operations.
 */
@ApplicationScoped
public class GoogleBooksProvider implements MetadataProviderPort {
    
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_RESULTS = 10;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LoggingPort loggingPort;
    private final ConfigurationPort configurationPort;
    
    @Inject
    public GoogleBooksProvider(ObjectMapper objectMapper, 
                              LoggingPort loggingPort,
                              ConfigurationPort configurationPort) {
        this.objectMapper = objectMapper;
        this.loggingPort = loggingPort;
        this.configurationPort = configurationPort;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();
    }
    
    @Override
    public String getProviderId() {
        return "google-books";
    }
    
    @Override
    public String getProviderName() {
        return "Google Books";
    }
    
    @Override
    public boolean isEnabled() {
        // Google Books API works without API key for basic searches
        return true;
    }
    
    @Override
    public Optional<BookMetadata> findByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            String cleanIsbn = cleanIsbn(isbn);
            String url = BASE_URL + "?q=isbn:" + URLEncoder.encode(cleanIsbn, StandardCharsets.UTF_8);
            
            String response = makeRequest(url);
            if (response == null) {
                return Optional.empty();
            }
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");
            
            if (items != null && items.isArray() && items.size() > 0) {
                JsonNode firstItem = items.get(0);
                return Optional.of(parseBookMetadata(firstItem));
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            loggingPort.error("Error searching Google Books by ISBN: " + isbn, e);
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
            query.append("intitle:").append(URLEncoder.encode(title.trim(), StandardCharsets.UTF_8));
            
            if (author != null && !author.trim().isEmpty()) {
                query.append("+inauthor:").append(URLEncoder.encode(author.trim(), StandardCharsets.UTF_8));
            }
            
            String url = BASE_URL + "?q=" + query + "&maxResults=" + MAX_RESULTS;
            
            String response = makeRequest(url);
            if (response == null) {
                return List.of();
            }
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");
            
            List<BookMetadata> results = new ArrayList<>();
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    try {
                        results.add(parseBookMetadata(item));
                    } catch (Exception e) {
                        loggingPort.error("Error parsing Google Books item", e);
                    }
                }
            }
            
            return results;
            
        } catch (Exception e) {
            loggingPort.error("Error searching Google Books by title: " + title, e);
            return List.of();
        }
    }
    
    @Override
    public Optional<BookMetadata> findByProviderId(String providerSpecificId) {
        if (providerSpecificId == null || providerSpecificId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            String url = BASE_URL + "/" + URLEncoder.encode(providerSpecificId, StandardCharsets.UTF_8);
            
            String response = makeRequest(url);
            if (response == null) {
                return Optional.empty();
            }
            
            JsonNode root = objectMapper.readTree(response);
            return Optional.of(parseBookMetadata(root));
            
        } catch (Exception e) {
            loggingPort.error("Error getting Google Books by ID: " + providerSpecificId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public boolean testConnection() {
        try {
            // Test with a simple query that should always return results
            String url = BASE_URL + "?q=isbn:9780134685991&maxResults=1";
            String response = makeRequest(url);
            return response != null && response.contains("\"totalItems\":");
        } catch (Exception e) {
            loggingPort.error("Google Books connection test failed", e);
            return false;
        }
    }
    
    @Override
    public int getPriority() {
        return 1; // High priority provider
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
                loggingPort.warn("Google Books API returned status: " + response.statusCode() + " for URL: " + url);
                return null;
            }
            
        } catch (IOException | InterruptedException e) {
            loggingPort.error("Error making request to Google Books API: " + url, e);
            return null;
        }
    }
    
    private BookMetadata parseBookMetadata(JsonNode item) {
        JsonNode volumeInfo = item.get("volumeInfo");
        if (volumeInfo == null) {
            throw new IllegalArgumentException("Invalid Google Books response: missing volumeInfo");
        }
        
        BookMetadata.Builder builder = BookMetadata.builder()
            .providerId(getProviderId())
            .providerName(getProviderName())
            .confidence(0.9); // High confidence for Google Books
        
        // Basic info
        if (volumeInfo.has("title")) {
            builder.title(volumeInfo.get("title").asText());
        }
        
        if (volumeInfo.has("subtitle")) {
            builder.subtitle(volumeInfo.get("subtitle").asText());
        }
        
        if (volumeInfo.has("description")) {
            builder.description(volumeInfo.get("description").asText());
        }
        
        if (volumeInfo.has("language")) {
            builder.language(volumeInfo.get("language").asText());
        }
        
        if (volumeInfo.has("pageCount")) {
            builder.pageCount(volumeInfo.get("pageCount").asInt());
        }
        
        if (volumeInfo.has("publisher")) {
            builder.publisher(volumeInfo.get("publisher").asText());
        }
        
        // Publication date
        if (volumeInfo.has("publishedDate")) {
            String dateStr = volumeInfo.get("publishedDate").asText();
            LocalDate publicationDate = parseDate(dateStr);
            if (publicationDate != null) {
                builder.publicationDate(publicationDate);
                builder.publicationYear(publicationDate.getYear());
            }
        }
        
        // ISBNs
        JsonNode industryIdentifiers = volumeInfo.get("industryIdentifiers");
        if (industryIdentifiers != null && industryIdentifiers.isArray()) {
            for (JsonNode identifier : industryIdentifiers) {
                String type = identifier.get("type").asText();
                String value = identifier.get("identifier").asText();
                
                if ("ISBN_13".equals(type)) {
                    builder.isbn13(value);
                    if (builder.build().isbn() == null) {
                        builder.isbn(value);
                    }
                } else if ("ISBN_10".equals(type)) {
                    builder.isbn10(value);
                    if (builder.build().isbn() == null) {
                        builder.isbn(value);
                    }
                }
            }
        }
        
        // Authors
        JsonNode authors = volumeInfo.get("authors");
        if (authors != null && authors.isArray()) {
            List<AuthorMetadata> authorList = new ArrayList<>();
            for (JsonNode author : authors) {
                authorList.add(AuthorMetadata.author(author.asText()));
            }
            builder.authors(authorList);
        }
        
        // Categories/subjects
        JsonNode categories = volumeInfo.get("categories");
        if (categories != null && categories.isArray()) {
            Set<String> subjects = new HashSet<>();
            for (JsonNode category : categories) {
                subjects.add(category.asText());
            }
            builder.subjects(subjects);
        }
        
        // Images
        JsonNode imageLinks = volumeInfo.get("imageLinks");
        if (imageLinks != null) {
            if (imageLinks.has("smallThumbnail")) {
                builder.smallThumbnail(imageLinks.get("smallThumbnail").asText());
            }
            if (imageLinks.has("thumbnail")) {
                builder.thumbnail(imageLinks.get("thumbnail").asText());
            }
            if (imageLinks.has("small")) {
                builder.mediumImage(imageLinks.get("small").asText());
            }
            if (imageLinks.has("medium")) {
                builder.largeImage(imageLinks.get("medium").asText());
            }
            if (imageLinks.has("large")) {
                builder.extraLargeImage(imageLinks.get("large").asText());
            }
        }
        
        // Ratings
        if (volumeInfo.has("averageRating")) {
            builder.averageRating(volumeInfo.get("averageRating").asDouble());
        }
        
        if (volumeInfo.has("ratingsCount")) {
            builder.ratingsCount(volumeInfo.get("ratingsCount").asInt());
        }
        
        // Google Books ID
        if (item.has("id")) {
            builder.googleBooksId(item.get("id").asText());
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
            // Try different date formats
            if (dateStr.length() == 4) {
                // Year only
                return LocalDate.of(Integer.parseInt(dateStr), 1, 1);
            } else if (dateStr.matches("\\d{4}-\\d{2}")) {
                // Year-month
                return LocalDate.parse(dateStr + "-01");
            } else {
                // Full date
                return LocalDate.parse(dateStr);
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            loggingPort.warn("Could not parse date: " + dateStr);
            return null;
        }
    }
}