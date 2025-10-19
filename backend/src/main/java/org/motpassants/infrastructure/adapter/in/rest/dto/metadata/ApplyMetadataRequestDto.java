package org.motpassants.infrastructure.adapter.in.rest.dto.metadata;

/**
 * DTO for requesting to apply metadata to a book.
 */
public class ApplyMetadataRequestDto {
    
    private String source;
    private String providerBookId;
    private boolean downloadCover;

    public ApplyMetadataRequestDto() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProviderBookId() {
        return providerBookId;
    }

    public void setProviderBookId(String providerBookId) {
        this.providerBookId = providerBookId;
    }

    public boolean isDownloadCover() {
        return downloadCover;
    }

    public void setDownloadCover(boolean downloadCover) {
        this.downloadCover = downloadCover;
    }
}
