package org.motpassants.infrastructure.adapter.out.metadata.googlebooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Image links from Google Books API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksImageLinks {
    
    private String smallThumbnail;
    private String thumbnail;
    private String small;
    private String medium;
    private String large;
    private String extraLarge;

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public void setSmallThumbnail(String smallThumbnail) {
        this.smallThumbnail = smallThumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public String getExtraLarge() {
        return extraLarge;
    }

    public void setExtraLarge(String extraLarge) {
        this.extraLarge = extraLarge;
    }

    /**
     * Get the best available image URL, preferring larger sizes.
     */
    public String getBestImageUrl() {
        if (extraLarge != null) return extraLarge;
        if (large != null) return large;
        if (medium != null) return medium;
        if (small != null) return small;
        if (thumbnail != null) return thumbnail;
        return smallThumbnail;
    }
}
