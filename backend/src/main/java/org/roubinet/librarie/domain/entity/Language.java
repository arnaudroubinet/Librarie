package org.roubinet.librarie.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Language entity representing supported languages for books and user preferences.
 * Uses BCP 47 language tags (e.g., en-US, fr-FR, zh-Hans) as primary key for better internationalization.
 */
@Entity
@Table(name = "languages")
public class Language {

    @Id
    @Column(name = "code", length = 35)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    // RTL = Right-to-Left text direction (e.g., Arabic, Hebrew)
    @Column(name = "rtl", nullable = false, columnDefinition = "boolean default false")
    private Boolean rtl = false;

    // Default constructor
    public Language() {}

    public Language(String code, String name, Boolean rtl) {
        this.code = code;
        this.name = name;
        this.rtl = rtl;
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