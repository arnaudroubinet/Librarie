package org.roubinet.librarie.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.JsonFormat;
import jakarta.enterprise.context.ApplicationScoped;
import java.lang.reflect.Type;
import org.hibernate.type.format.AbstractJsonFormatMapper;

/**
 * Persistence-layer JSON mapper for Hibernate ORM JSON columns.
 *
 * This bean is discovered by Quarkus/Hibernate via the {@link JsonFormat}
 * and {@link PersistenceUnitExtension} annotations and ensures database
 * serialization is decoupled from the REST ObjectMapper configuration.
 */
@ApplicationScoped
@JsonFormat
@PersistenceUnitExtension
public class PersistenceJsonFormatMapper extends AbstractJsonFormatMapper {
   public static final String SHORT_NAME = "jackson";
   private final ObjectMapper objectMapper;

   public PersistenceJsonFormatMapper() {
      this((new ObjectMapper()).findAndRegisterModules());
   }

   public PersistenceJsonFormatMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
   }

   public <T> T fromString(CharSequence charSequence, Type type) {
      try {
         return this.objectMapper.readValue(charSequence.toString(), this.objectMapper.constructType(type));
      } catch (JsonProcessingException var4) {
         throw new IllegalArgumentException("Could not deserialize string to java type: " + String.valueOf(type), var4);
      }
   }

   public <T> String toString(T value, Type type) {
      try {
         return this.objectMapper.writerFor(this.objectMapper.constructType(type)).writeValueAsString(value);
      } catch (JsonProcessingException var4) {
         throw new IllegalArgumentException("Could not serialize object of java type: " + String.valueOf(type), var4);
      }
   }
}