package org.motpassants.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
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
        classes = new ClassFileImporter().importPackages("org.motpassants");
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
                            "jakarta.inject..",
                            "jakarta.transaction..",
                            "org.motpassants.domain..",
                            "org.motpassants.application.."
                    )
                    .allowEmptyShould(true)
                    .because("Application layer should only depend on domain and standard Java/Jakarta APIs");

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
            // This test will become meaningful when we have classes in the packages
            // For now, we'll just skip the empty layer validation
            ArchRule rule = layeredArchitecture().consideringAllDependencies()
                    .layer("Domain").definedBy("..domain..")
                    .layer("Application").definedBy("..application..")
                    .layer("Infrastructure").definedBy("..infrastructure..")
                    .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Domain", "Application", "Infrastructure")
                    .ignoreDependency(Object.class, Object.class); // Allow when layers are empty

            // Only check if we have classes in the org.motpassants package
            if (classes.stream().anyMatch(clazz -> clazz.getPackageName().startsWith("org.motpassants") 
                    && !clazz.getPackageName().contains("architecture"))) {
                rule.check(classes);
            }
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
            ArchRule rule = classes().that()
                    .resideInAPackage("..infrastructure.adapter.in..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..domain.port.in..")
                    .allowEmptyShould(true)
                    .because("Inbound adapters should implement inbound ports");

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
    }
}