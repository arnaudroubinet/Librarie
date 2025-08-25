package org.motpassants.domain.port.out.readium;

import org.motpassants.domain.core.model.Book;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port abstracting EPUB publication operations required by the application layer.
 * This decouples the application from infrastructure-specific Readium service implementation.
 */
public interface EpubPublicationPort {

    Optional<PublicationInfo> openPublication(Book book);

    Optional<PublicationInfo> openPublication(Path epubPath);

    Optional<String> findCoverImageZipPath(PublicationInfo pub);

    Optional<String> findFirstPageImageZipPath(PublicationInfo pub);

    Optional<byte[]> readEntryBytes(PublicationInfo pub, String entryPathInZip);

    String buildZipPath(String opfDir, String href);

    List<String> extractTocLinks(PublicationInfo pub) throws Exception;

    InputStream openEntryStream(PublicationInfo pub, String entryPathInZip) throws Exception;

    String guessContentType(String hrefOrName);

    /** Minimal shape used by IngestService. */
    interface PublicationInfo {
        Path getEpubFile();
        String getOpfPath();
        String getOpfDir();
    }

    /**
     * Basic metadata extracted from OPF for ingest.
     * Must be an interface to comply with outbound port architectural rules.
     */
    interface CoreMetadata {
        String title();
        String language();
        String description();
        String isbn();
    }

    /** Extract core metadata fields from the OPF file. */
    Optional<CoreMetadata> extractCoreMetadata(PublicationInfo pub);
}
