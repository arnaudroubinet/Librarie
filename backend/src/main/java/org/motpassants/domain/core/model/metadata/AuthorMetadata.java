package org.motpassants.domain.core.model.metadata;

/**
 * Author metadata with role information.
 */
public record AuthorMetadata(
    String name,
    String firstName,
    String lastName,
    String role,  // e.g., "author", "editor", "translator", "illustrator"
    String bio,
    String imageUrl
) {
    
    public static AuthorMetadata author(String name) {
        return new AuthorMetadata(name, null, null, "author", null, null);
    }
    
    public static AuthorMetadata withRole(String name, String role) {
        return new AuthorMetadata(name, null, null, role, null, null);
    }
    
    public static AuthorMetadata complete(String firstName, String lastName, String role) {
        String fullName = (firstName != null && lastName != null) 
            ? firstName + " " + lastName 
            : (firstName != null ? firstName : lastName);
        return new AuthorMetadata(fullName, firstName, lastName, role, null, null);
    }
}