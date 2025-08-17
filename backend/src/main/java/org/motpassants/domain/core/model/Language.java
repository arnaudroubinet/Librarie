package org.motpassants.domain.core.model;

/**
 * Language domain model representing supported languages for books and user preferences.
 * Uses BCP 47 language tags (e.g., en-US, fr-FR, zh-Hans) as primary key for better internationalization.
 * Pure domain object without any infrastructure dependencies.
 */
public class Language {

    private String code;
    private String name;
    // RTL = Right-to-Left text direction (e.g., Arabic, Hebrew)
    private Boolean rtl;

    // Default constructor
    public Language() {
        this.rtl = false;
    }

    public Language(String code, String name, Boolean rtl) {
        this.code = code;
        this.name = name;
        this.rtl = rtl != null ? rtl : false;
    }

    // Business methods
    public boolean isRightToLeft() {
        return rtl != null && rtl;
    }

    public void updateDisplayName(String newName) {
        this.name = newName;
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getRtl() {
        return rtl;
    }

    public void setRtl(Boolean rtl) {
        this.rtl = rtl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Language)) return false;
        Language language = (Language) o;
        return code != null && code.equals(language.code);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Language{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", rtl=" + rtl +
                '}';
    }
}