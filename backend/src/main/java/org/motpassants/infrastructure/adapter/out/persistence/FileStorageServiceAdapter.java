package org.motpassants.infrastructure.adapter.out.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.motpassants.domain.port.out.FileStorageService;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;
import org.motpassants.infrastructure.config.LibrarieConfigProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File system implementation of FileStorageService.
 */
@ApplicationScoped
public class FileStorageServiceAdapter implements FileStorageService {
    
    private static final Logger LOG = Logger.getLogger(FileStorageServiceAdapter.class);
    
    private final LibrarieConfigProperties config;
    private final SecureFileProcessingPort secureFileProcessingPort;
    
    @Inject
    public FileStorageServiceAdapter(LibrarieConfigProperties config,
                                   SecureFileProcessingPort secureFileProcessingPort) {
        this.config = config;
        this.secureFileProcessingPort = secureFileProcessingPort;
    }
    
    @Override
    public Path storeFile(byte[] content, String relativePath) throws IOException {
        Path absolutePath = secureFileProcessingPort.sanitizePath(
            config.storage().baseDir(), relativePath);
        
        // Ensure parent directory exists
        Path parentDir = absolutePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        // Write file
        Files.write(absolutePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        LOG.info("Stored file: " + absolutePath);
        return absolutePath;
    }
    
    @Override
    public byte[] retrieveFile(String relativePath) throws IOException {
        Path absolutePath = secureFileProcessingPort.sanitizePath(
            config.storage().baseDir(), relativePath);
        
        if (!Files.exists(absolutePath)) {
            throw new IOException("File not found: " + relativePath);
        }
        
        return Files.readAllBytes(absolutePath);
    }
    
    @Override
    public boolean fileExists(String relativePath) {
        try {
            Path absolutePath = secureFileProcessingPort.sanitizePath(
                config.storage().baseDir(), relativePath);
            return Files.exists(absolutePath);
        } catch (Exception e) {
            LOG.warn("Error checking file existence: " + relativePath, e);
            return false;
        }
    }
    
    @Override
    public boolean deleteFile(String relativePath) throws IOException {
        Path absolutePath = secureFileProcessingPort.sanitizePath(
            config.storage().baseDir(), relativePath);
        
        if (Files.exists(absolutePath)) {
            Files.delete(absolutePath);
            LOG.info("Deleted file: " + absolutePath);
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<String> listFiles(String directoryPath) throws IOException {
        Path absolutePath = secureFileProcessingPort.sanitizePath(
            config.storage().baseDir(), directoryPath);
        
        if (!Files.exists(absolutePath) || !Files.isDirectory(absolutePath)) {
            throw new IOException("Directory not found: " + directoryPath);
        }
        
        try (Stream<Path> files = Files.list(absolutePath)) {
            return files
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
        }
    }
    
    @Override
    public Path getAbsolutePath(String relativePath) {
        return secureFileProcessingPort.sanitizePath(
            config.storage().baseDir(), relativePath);
    }
    
    @Override
    public void ensureDirectoryExists(String directoryPath) throws IOException {
        Path absolutePath = secureFileProcessingPort.sanitizePath(
            config.storage().baseDir(), directoryPath);
        
        if (!Files.exists(absolutePath)) {
            Files.createDirectories(absolutePath);
            LOG.info("Created directory: " + absolutePath);
        }
    }
}