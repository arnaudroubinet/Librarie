package org.roubinet.librarie.domain.entity;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for User entity and OIDC integration
 */
@QuarkusTest
public class UserEntityTest {

    @Inject
    EntityManager entityManager;

    @Test
    @Transactional
    public void testUserCreation() {
        // Create a test user
        User user = new User();
        user.setOidcOrigin("keycloak");
        user.setOidcOriginUrl("http://localhost:8180/auth/realms/librarie");
        user.setOidcSubject("123e4567-e89b-12d3-a456-426614174000");
        user.setUsername("testuser");

        entityManager.persist(user);
        entityManager.flush();

        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("keycloak", user.getOidcOrigin());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    @Transactional
    public void testUserRatingRelationship() {
        // Create a test user
        User user = new User();
        user.setOidcOrigin("keycloak");
        user.setOidcOriginUrl("http://localhost:8180/auth/realms/librarie");
        user.setOidcSubject("rating-user-123");
        user.setUsername("ratinguser");
        entityManager.persist(user);

        // Create a test book
        Book book = new Book();
        book.setTitle("Test Book");
        book.setTitleSort("Test Book");
        book.setPath("/books/test-book.epub");
        entityManager.persist(book);

        // Create a rating
        Rating rating = new Rating();
        rating.setBook(book);
        rating.setUser(user);
        rating.setRating(5);
        rating.setReview("Excellent book!");
        entityManager.persist(rating);

        entityManager.flush();

        assertNotNull(rating.getId());
        assertEquals(user, rating.getUser());
        assertEquals(book, rating.getBook());
        assertEquals(5, rating.getRating());
    }
}