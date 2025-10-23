package org.motpassants.infrastructure.adapter.out.metadata.googlebooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a single volume (book) from Google Books API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksVolume {
    
    private String id;
    private String kind;
    private String etag;
    private String selfLink;
    private GoogleBooksVolumeInfo volumeInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public GoogleBooksVolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(GoogleBooksVolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }
}
