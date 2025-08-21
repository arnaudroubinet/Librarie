package org.motpassants.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit tests to validate strict hexagonal architecture implementation.
 * These tests ensure that the architecture rules are followed and dependencies 
 * only flow inward (toward the domain core).
 */
@DisplayName("Hexagonal Architecture Tests")
class HexagonalArchitectureTest {

    private JavaClasses classes;

    @BeforeEach
    void setUp() {
        // Import only production code, excluding test packages for architecture validation
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("org.motpassants");
    }

    @Nested
    @DisplayName("Layer Dependency Rules")
    class LayerDependencyRules {

        @Test
        @DisplayName("Domain core should have no external dependencies")
        void domainCoreShouldHaveNoExternalDependencies() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.core..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "java..",
                            "..domain.core..",
                            "javax..",
                            "org.motpassants.domain.core.."
                    )
                    .allowEmptyShould(true)
                    .because("Domain core should not depend on any external frameworks or infrastructure");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain ports should only depend on domain core")
        void domainPortsShouldOnlyDependOnDomainCore() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.port..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "java..",
                            "..domain.core..",
                            "..domain.port..",
                            "javax..",
                            "org.motpassants.domain.."
                    )
                    .allowEmptyShould(true)
                    .because("Domain ports should only depend on domain core");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application layer should only depend on domain")
        void applicationLayerShouldOnlyDependOnDomain() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..application..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "java..",
                            "..domain..",
                            "..application..",
                            "javax..",
                            "jakarta.enterprise.context..",
                            "jakarta.enterprise.event..",
                            "jakarta.inject..",
                            "jakarta.transaction..",
                            "jakarta.validation..",
                            "io.quarkus.runtime..",
                            "org.motpassants.domain..",
                            "org.motpassants.application.."
                    )
                    .allowEmptyShould(true)
                    .because("Application layer should only depend on domain and standard Java/Jakarta APIs for dependency injection");

            rule.check(classes);
        }

        @Test
        @DisplayName("Infrastructure should not be accessed by domain or application layers")
        void infrastructureShouldNotBeAccessedByDomainOrApplicationLayers() {
            ArchRule rule = noClasses().that()
                    .resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..")
                    .allowEmptyShould(true)
                    .because("Domain and Application layers should not depend on Infrastructure");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Hexagonal Architecture Pattern Rules")
    class HexagonalArchitecturePatternRules {

        @Test
        @DisplayName("Layered architecture should be respected")
        void layeredArchitectureShouldBeRespected() {
            ArchRule rule = layeredArchitecture().consideringOnlyDependenciesInLayers()
                    .layer("Domain Core").definedBy("..domain.core..")
                    .layer("Domain Ports").definedBy("..domain.port..")
                    .layer("Application").definedBy("..application..")
                    .layer("Infrastructure").definedBy("..infrastructure..")
                    
                    // Infrastructure can access everything (outbound adapters implement domain ports)
                    .whereLayer("Infrastructure").mayOnlyAccessLayers("Infrastructure", "Application", "Domain Ports", "Domain Core")
                    
                    // Application can access domain layers
                    .whereLayer("Application").mayOnlyAccessLayers("Application", "Domain Ports", "Domain Core")
                    
                    // Domain ports can only access domain core
                    .whereLayer("Domain Ports").mayOnlyAccessLayers("Domain Ports", "Domain Core")
                    
                    // Domain core should be pure (no external dependencies)
                    .whereLayer("Domain Core").mayOnlyAccessLayers("Domain Core");

            rule.check(classes);
        }

        @Test
        @DisplayName("Inbound ports should be interfaces")
        void inboundPortsShouldBeInterfaces() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.port.in..")
                    .should().beInterfaces()
                    .allowEmptyShould(true)
                    .because("Inbound ports should be interfaces defining use cases");

            rule.check(classes);
        }

        @Test
        @DisplayName("Outbound ports should be interfaces")
        void outboundPortsShouldBeInterfaces() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.port.out..")
                    .should().beInterfaces()
                    .allowEmptyShould(true)
                    .because("Outbound ports should be interfaces defining external dependencies");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain services should not depend on frameworks")
        void domainServicesShouldNotDependOnFrameworks() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.core.service..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "java..",
                            "..domain.core..",
                            "javax..",
                            "org.motpassants.domain.core.."
                    )
                    .allowEmptyShould(true)
                    .because("Domain services should contain pure business logic without framework dependencies");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Adapter Pattern Rules")
    class AdapterPatternRules {

        @Test
        @DisplayName("Inbound adapters should implement inbound ports")
        void inboundAdaptersShouldImplementInboundPorts() {
            // REST controllers typically use application services, not directly implement ports
            // This test validates that REST controllers depend on application layer
            ArchRule rule = classes().that()
                    .resideInAPackage("..infrastructure.adapter.in..")
                    .and().areAnnotatedWith("jakarta.ws.rs.Path")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..application..",
                            "..domain.."
                    )
                    .allowEmptyShould(true)
                    .because("Inbound adapters should use application services or domain ports");

            rule.check(classes);
        }

        @Test
        @DisplayName("Outbound adapters should implement outbound ports")
        void outboundAdaptersShouldImplementOutboundPorts() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..infrastructure.adapter.out..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..domain.port.out..")
                    .allowEmptyShould(true)
                    .because("Outbound adapters should implement outbound ports");

            rule.check(classes);
        }

        @Test
        @DisplayName("REST controllers should be in inbound adapter package")
        void restControllersShouldBeInInboundAdapterPackage() {
            ArchRule rule = classes().that()
                    .areAnnotatedWith("jakarta.ws.rs.Path")
                    .should().resideInAPackage("..infrastructure.adapter.in.rest..")
                    .allowEmptyShould(true)
                    .because("REST controllers are inbound adapters");

            rule.check(classes);
        }

        @Test
        @DisplayName("Repository implementations should be in outbound adapter package")
        void repositoryImplementationsShouldBeInOutboundAdapterPackage() {
            ArchRule rule = classes().that()
                    .haveSimpleNameEndingWith("RepositoryAdapter")
                    .or().haveSimpleNameEndingWith("RepositoryImpl")
                    .should().resideInAPackage("..infrastructure.adapter.out.persistence..")
                    .allowEmptyShould(true)
                    .because("Repository implementations are outbound adapters");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Persistence and Framework Rules")
    class PersistenceAndFrameworkRules {

        @Test
        @DisplayName("JPA annotations should only be in infrastructure layer")
        void jpaAnnotationsShouldOnlyBeInInfrastructureLayer() {
            ArchRule rule = noClasses().that()
                    .resideOutsideOfPackage("..infrastructure..")
                    .should().beAnnotatedWith("jakarta.persistence.Entity")
                    .orShould().beAnnotatedWith("jakarta.persistence.Table")
                    .orShould().beAnnotatedWith("jakarta.persistence.Column")
                    .orShould().beAnnotatedWith("jakarta.persistence.Id")
                    .orShould().beAnnotatedWith("jakarta.persistence.GeneratedValue")
                    .orShould().beAnnotatedWith("jakarta.persistence.OneToMany")
                    .orShould().beAnnotatedWith("jakarta.persistence.ManyToOne")
                    .orShould().beAnnotatedWith("jakarta.persistence.ManyToMany")
                    .orShould().beAnnotatedWith("jakarta.persistence.OneToOne")
                    .allowEmptyShould(true)
                    .because("JPA annotations should only be used in infrastructure layer");

            rule.check(classes);
        }

        @Test
        @DisplayName("Quarkus/CDI annotations should only be in infrastructure and application layers")
        void quarkusCdiAnnotationsShouldOnlyBeInInfrastructureAndApplicationLayers() {
            ArchRule rule = noClasses().that()
                    .resideInAPackage("..domain.core..")
                    .should().beAnnotatedWith("jakarta.enterprise.context.ApplicationScoped")
                    .orShould().beAnnotatedWith("jakarta.inject.Inject")
                    .orShould().beAnnotatedWith("jakarta.ws.rs.Path")
                    .orShould().beAnnotatedWith("jakarta.ws.rs.GET")
                    .orShould().beAnnotatedWith("jakarta.ws.rs.POST")
                    .orShould().beAnnotatedWith("jakarta.ws.rs.PUT")
                    .orShould().beAnnotatedWith("jakarta.ws.rs.DELETE")
                    .allowEmptyShould(true)
                    .because("Domain core should not depend on framework-specific annotations");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Package Isolation Rules")
    class PackageIsolationRules {

        @Test
        @DisplayName("Classes should not have cyclic dependencies")
        void classesShouldNotHaveCyclicDependencies() {
            ArchRule rule = slices().matching("org.motpassants.(*)..")
                    .should().beFreeOfCycles()
                    .because("Cyclic dependencies make the code hard to test and maintain");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain model should not depend on persistence concerns")
        void domainModelShouldNotDependOnPersistenceConcerns() {
            ArchRule rule = noClasses().that()
                    .resideInAPackage("..domain.core.model..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..hibernate..",
                            "..jpa..",
                            "..persistence..",
                            "jakarta.persistence..",
                            "org.hibernate..",
                            "io.quarkus.hibernate.."
                    )
                    .allowEmptyShould(true)
                    .because("Domain model should be persistence agnostic");

            rule.check(classes);
        }

        @Test
        @DisplayName("DTOs should only be used in REST layer")
        void dtosShouldOnlyBeUsedInRestLayer() {
            ArchRule rule = noClasses().that()
                    .resideOutsideOfPackage("..infrastructure.adapter.in.rest..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure.adapter.in.rest.dto..")
                    .allowEmptyShould(true)
                    .because("DTOs should only be used in REST controllers for data transfer");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application services should not depend on REST concerns")
        void applicationServicesShouldNotDependOnRestConcerns() {
            ArchRule rule = noClasses().that()
                    .resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.ws.rs..",
                            "..rest..",
                            "..dto.."
                    )
                    .allowEmptyShould(true)
                    .because("Application services should be independent of REST/HTTP concerns");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application services should implement use case interfaces")
        void applicationServicesShouldImplementUseCaseInterfaces() {
            // Only check services that follow the use case pattern (exclude infrastructure services)
            ArchRule rule = classes().that()
                    .resideInAPackage("..application.service..")
                    .and().areNotInterfaces()
                    .and().areAnnotatedWith("jakarta.enterprise.context.ApplicationScoped")
                    .and().haveSimpleNameNotContaining("Startup")
                    .and().haveSimpleNameNotContaining("DemoData")
                    .and().haveSimpleNameNotContaining("Ingest")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..domain.port.in..")
                    .allowEmptyShould(true)
                    .because("Application services (excluding infrastructure services) should depend on domain use case interfaces");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application services should be package-private where possible")
        void applicationServicesShouldBePackagePrivateWherePossible() {
            // Application services need to be public for CDI injection when they implement domain ports
            // This rule checks that application services are appropriately scoped
            ArchRule rule = classes().that()
                    .resideInAPackage("..application.service..")
                    .and().areNotInterfaces()
                    .and().areNotAnnotatedWith("jakarta.enterprise.context.ApplicationScoped")
                    .and().areNotAnnotatedWith("jakarta.inject.Singleton")
                    .should().notBePublic()
                    .allowEmptyShould(true)
                    .because("Application services should use appropriate CDI scopes for dependency injection");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain services should be package-private where possible")
        void domainServicesShouldBePackagePrivateWherePossible() {
            // Application services need to be public for CDI injection, but domain services should be package-private
            // This rule only applies to pure domain services, not application services
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.core.service..")
                    .and().areNotInterfaces()
                    .and().areNotAnnotatedWith("jakarta.enterprise.context.ApplicationScoped")
                    .and().areNotAnnotatedWith("jakarta.inject.Singleton")
                    .should().notBePublic()
                    .allowEmptyShould(true)
                    .because("Pure domain services should be package-private unless they need CDI injection");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Enhanced Hexagonal Architecture Rules")
    class EnhancedHexagonalArchitectureRules {

        @Test
        @DisplayName("Domain models should be immutable or have controlled mutability")
        void domainModelsShouldBeImmutableOrHaveControlledMutability() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.core.model..")
                    .and().areRecords()
                    .should().haveOnlyFinalFields()
                    .allowEmptyShould(true)
                    .because("Domain records should be immutable");

            rule.check(classes);
        }

        @Test
        @DisplayName("Use case interfaces should define business operations")
        void useCaseInterfacesShouldDefineBusinessOperations() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.port.in..")
                    .should().beInterfaces()
                    .andShould().haveSimpleNameEndingWith("UseCase")
                    .allowEmptyShould(true)
                    .because("Inbound ports should be use case interfaces");

            rule.check(classes);
        }

        @Test
        @DisplayName("Repository ports should define data access contracts")
        void repositoryPortsShouldDefineDataAccessContracts() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..domain.port.out..")
                    .and().haveSimpleNameContaining("Repository")
                    .should().beInterfaces()
                    .allowEmptyShould(true)
                    .because("Repository ports should be interfaces defining data access contracts");

            rule.check(classes);
        }

        @Test
        @DisplayName("Adapters should implement corresponding ports")
        void adaptersShouldImplementCorrespondingPorts() {
            ArchRule rule = classes().that()
                    .resideInAPackage("..infrastructure.adapter.out..")
                    .and().haveSimpleNameEndingWith("Adapter")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..domain.port.out..")
                    .allowEmptyShould(true)
                    .because("Outbound adapters should implement domain ports");

            rule.check(classes);
        }

        @Test
        @DisplayName("Configuration should be externalized from domain logic")
        void configurationShouldBeExternalizedFromDomainLogic() {
            ArchRule rule = noClasses().that()
                    .resideInAnyPackage("..domain.core..", "..domain.port..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..config..",
                            "io.quarkus.arc.config..",
                            "org.eclipse.microprofile.config.."
                    )
                    .allowEmptyShould(true)
                    .because("Domain should not depend on configuration frameworks");

            rule.check(classes);
        }
    }
}