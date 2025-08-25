package org.motpassants.infrastructure.adapter.out.readium;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.port.out.readium.EpubPublicationPort;
import org.motpassants.infrastructure.readium.EpubPublicationService;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EpubPublicationAdapter implements EpubPublicationPort {

    private final EpubPublicationService delegate;

    @Inject
    public EpubPublicationAdapter(EpubPublicationService delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<PublicationInfo> openPublication(Book book) {
        return delegate.openPublication(book).map(AdapterPublicationInfo::new);
    }

    @Override
    public Optional<PublicationInfo> openPublication(Path epubPath) {
        return delegate.openPublication(epubPath).map(AdapterPublicationInfo::new);
    }

    @Override
    public Optional<String> findCoverImageZipPath(PublicationInfo pub) {
        return delegate.findCoverImageZipPath(((AdapterPublicationInfo) pub).inner);
    }

    @Override
    public Optional<String> findFirstPageImageZipPath(PublicationInfo pub) {
        return delegate.findFirstPageImageZipPath(((AdapterPublicationInfo) pub).inner);
    }

    @Override
    public Optional<byte[]> readEntryBytes(PublicationInfo pub, String entryPathInZip) {
        return delegate.readEntryBytes(((AdapterPublicationInfo) pub).inner, entryPathInZip);
    }

    @Override
    public String buildZipPath(String opfDir, String href) {
        return delegate.buildZipPath(opfDir, href);
    }

    @Override
    public List<String> extractTocLinks(PublicationInfo pub) throws Exception {
        return delegate.extractTocLinks(((AdapterPublicationInfo) pub).inner);
    }

    @Override
    public InputStream openEntryStream(PublicationInfo pub, String entryPathInZip) throws Exception {
        return delegate.openEntryStream(((AdapterPublicationInfo) pub).inner, entryPathInZip);
    }

    @Override
    public String guessContentType(String hrefOrName) {
        return delegate.guessContentType(hrefOrName);
    }

    @Override
    public Optional<EpubPublicationPort.CoreMetadata> extractCoreMetadata(PublicationInfo pub) {
        return delegate.extractCoreMetadata(((AdapterPublicationInfo) pub).inner)
                .map(src -> new EpubPublicationPort.CoreMetadata() {
                    @Override public String title() { return src.title; }
                    @Override public String language() { return src.language; }
                    @Override public String description() { return src.description; }
                    @Override public String isbn() { return src.isbn; }
                });
    }

    private static class AdapterPublicationInfo implements PublicationInfo {
        private final EpubPublicationService.PublicationInfo inner;
        AdapterPublicationInfo(EpubPublicationService.PublicationInfo inner) { this.inner = inner; }
        @Override public Path getEpubFile() { return inner.getEpubFile(); }
        @Override public String getOpfPath() { return inner.getOpfPath(); }
        @Override public String getOpfDir() { return inner.getOpfDir(); }
    }
}
