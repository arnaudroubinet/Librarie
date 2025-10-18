package org.motpassants.infrastructure.adapter.out.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.infrastructure.config.LibrarieConfigProperties;

/**
 * Infrastructure adapter for configuration access.
 */
@ApplicationScoped
public class ConfigurationPortAdapter implements ConfigurationPort {
    
    private final LibrarieConfigProperties config;
    private final String activeProfile;
    
    @Inject
    public ConfigurationPortAdapter(LibrarieConfigProperties config,
                                   @ConfigProperty(name = "quarkus.profile") String activeProfile) {
        this.config = config;
        this.activeProfile = activeProfile;
    }
    
    @Override
    public boolean isDemoEnabled() {
        return config.demo().enabled();
    }
    
    @Override
    public String getActiveProfile() {
        return activeProfile;
    }
    
    @Override
    public StorageConfig getStorageConfig() {
        return new StorageConfigImpl(config.storage());
    }
    
    @Override
    public DemoConfig getDemoConfig() {
        return new DemoConfigImpl(config.demo());
    }
    
    @Override
    public SecurityConfig getSecurityConfig() {
        return new SecurityConfigImpl(config.security());
    }
    
    private static class StorageConfigImpl implements StorageConfig {
        private final LibrarieConfigProperties.Storage storage;
        
        public StorageConfigImpl(LibrarieConfigProperties.Storage storage) {
            this.storage = storage;
        }
        
        @Override
        public String getBaseDir() {
            return storage.baseDir();
        }
        
        @Override
        public long getMaxFileSize() {
            return storage.maxFileSize();
        }
        
        @Override
        public String getAllowedBookExtensions() {
            return storage.allowedBookExtensions();
        }
        
        @Override
        public String getAllowedImageExtensions() {
            return storage.allowedImageExtensions();
        }
    }
    
    private static class DemoConfigImpl implements DemoConfig {
        private final LibrarieConfigProperties.Demo demo;
        
        public DemoConfigImpl(LibrarieConfigProperties.Demo demo) {
            this.demo = demo;
        }
        
        @Override
        public boolean isEnabled() {
            return demo.enabled();
        }
        
        @Override
        public int getBookCount() {
            return demo.bookCount();
        }
        
        @Override
        public int getAuthorCount() {
            return demo.authorCount();
        }
        
        @Override
        public int getSeriesCount() {
            return demo.seriesCount();
        }
    }
    
    private static class SecurityConfigImpl implements SecurityConfig {
        private final LibrarieConfigProperties.Security security;
        
        public SecurityConfigImpl(LibrarieConfigProperties.Security security) {
            this.security = security;
        }
        
        @Override
        public boolean isSanitizationEnabled() {
            return security.sanitizationEnabled();
        }
        
        @Override
        public boolean isFileValidationEnabled() {
            return security.fileValidationEnabled();
        }
        
        @Override
        public long getMaxRequestSize() {
            return security.maxRequestSize();
        }
    }
}