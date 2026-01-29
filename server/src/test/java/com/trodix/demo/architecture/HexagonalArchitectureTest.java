package com.trodix.demo.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Tests d'architecture pour garantir le respect de l'architecture hexagonale
 */
class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.trodix.demo");
    }

    /**
     * Vérifie que l'architecture hexagonale est respectée avec les couches suivantes:
     * - Domain (coeur métier)
     * - Application (use cases)
     * - Adapter (in/out)
     * - Infrastructure (configuration, filters)
     *
     * Notes pragmatiques:
     * 1. L'Application peut accéder aux DTOs des adapters pour éviter la duplication.
     *    Les mappers MapStruct sont disponibles pour les cas où une séparation stricte est nécessaire.
     * 2. L'Application peut accéder aux adapters de sécurité (SpringAuthenticationAdapter).
     *    Idéalement, elle devrait utiliser l'interface AuthenticationAdapter, mais pour des raisons
     *    de simplicité, l'accès direct est autorisé.
     */
    @Test
    void hexagonalArchitectureShouldBeRespected() {
        Architectures.LayeredArchitecture architecture = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("com.trodix.demo.domain..")
            .layer("Application").definedBy("com.trodix.demo.application..")
            .layer("Adapter-DTO").definedBy("com.trodix.demo.adapter..dto..")
            .layer("Adapter-Security").definedBy("com.trodix.demo.adapter..security..")
            .layer("Adapter").definedBy("com.trodix.demo.adapter..")
            .layer("Infrastructure").definedBy("com.trodix.demo.infrastructure..")

            // Le Domain ne doit dépendre de personne
            .whereLayer("Domain").mayNotAccessAnyLayer()

            // L'Application peut accéder au Domain, aux DTOs et aux adapters de sécurité
            .whereLayer("Application").mayOnlyAccessLayers("Domain", "Adapter-DTO", "Adapter-Security")

            // Les Adapters peuvent accéder à Application et Domain
            .whereLayer("Adapter").mayOnlyAccessLayers("Application", "Domain")

            // Infrastructure peut accéder à toutes les couches (config, filters, etc.)
            .whereLayer("Infrastructure").mayOnlyAccessLayers("Adapter", "Application", "Domain")

            .ignoreDependency(java.lang.Object.class, Object.class);

        architecture.check(classes);
    }

    /**
     * Le domaine ne doit dépendre d'aucune autre couche
     */
    @Test
    void domainShouldNotDependOnAnyOtherLayer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.trodix.demo.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.trodix.demo.application..",
                "com.trodix.demo.adapter..",
                "com.trodix.demo.infrastructure.."
            )
            .as("Domain layer should not depend on any other layer");

        rule.check(classes);
    }

    /**
     * Le domaine ne doit pas dépendre de frameworks externes (sauf annotations de base)
     * On autorise lombok car ce sont des annotations de compile-time qui ne créent pas de couplage runtime
     */
    @Test
    void domainShouldNotDependOnSpringFramework() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.trodix.demo.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence.."
            )
            .as("Domain should not depend on Spring or JPA");

        rule.check(classes);
    }

    /**
     * Les use cases (application) doivent se terminer par "UseCase"
     */
    @Test
    void useCasesShouldBeNamedWithSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage("com.trodix.demo.application.usecase")
            .should().haveSimpleNameEndingWith("UseCase")
            .as("Use cases should end with 'UseCase'");

        rule.check(classes);
    }

    /**
     * Les use cases doivent être annotés @Service
     */
    @Test
    void useCasesShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
            .that().resideInAPackage("com.trodix.demo.application.usecase")
            .should().beAnnotatedWith(org.springframework.stereotype.Service.class)
            .as("Use cases should be annotated with @Service");

        rule.check(classes);
    }

    /**
     * Les adapters REST doivent se terminer par "Adapter" ou "RestAdapter"
     */
    @Test
    void restAdaptersShouldBeNamedWithSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage("com.trodix.demo.adapter.in")
            .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().haveSimpleNameEndingWith("Adapter")
            .orShould().haveSimpleNameEndingWith("RestAdapter")
            .as("REST adapters should end with 'Adapter' or 'RestAdapter'");

        rule.check(classes);
    }

    /**
     * Les DTOs publics (Request/Response) doivent être dans le package adapter.in.dto
     * Les DTOs internes aux adapters out sont autorisés (classes internes)
     */
    @Test
    void publicDtosShouldBeInDtoPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Request")
            .or().haveSimpleNameEndingWith("Response")
            .and().resideInAPackage("com.trodix.demo.adapter.in..")
            .and().areNotMemberClasses()  // Exclure les classes internes
            .should().resideInAPackage("..adapter.in.dto..")
            .as("Public DTOs should be in the dto package");

        rule.check(classes);
    }

    /**
     * Les services métier doivent être dans le package service ou usecase
     */
    @Test
    void servicesShouldBeInCorrectPackage() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(org.springframework.stereotype.Service.class)
            .and().resideInAPackage("com.trodix.demo.application..")
            .should().resideInAnyPackage(
                "..application.usecase..",
                "..application.service.."
            )
            .as("Services should be in usecase or service packages");

        rule.check(classes);
    }

    /**
     * Les ports (interfaces) doivent être dans le package port
     */
    @Test
    void portsShouldBeInterfaces() {
        ArchRule rule = classes()
            .that().resideInAPackage("..port..")
            .should().beInterfaces()
            .as("Ports should be interfaces");

        rule.check(classes);
    }

    /**
     * Les classes du domaine ne doivent pas utiliser d'annotations Spring
     */
    @Test
    void domainModelsShouldNotUseSpringAnnotations() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.trodix.demo.domain.model..")
            .should().beAnnotatedWith(org.springframework.stereotype.Component.class)
            .orShould().beAnnotatedWith(org.springframework.stereotype.Service.class)
            .orShould().beAnnotatedWith(org.springframework.stereotype.Repository.class)
            .as("Domain models should not use Spring annotations");

        rule.check(classes);
    }

    /**
     * Les exceptions métier doivent être dans le package domain ou application.exceptions
     */
    @Test
    void businessExceptionsShouldBeInCorrectPackage() {
        ArchRule rule = classes()
            .that().areAssignableTo(RuntimeException.class)
            .and().resideInAPackage("com.trodix.demo.application.exceptions..")
            .should().haveSimpleNameEndingWith("Exception")
            .as("Business exceptions should end with 'Exception'");

        rule.check(classes);
    }

    /**
     * Les adapters IN ne doivent pas dépendre des adapters OUT
     */
    @Test
    void inAdaptersShouldNotDependOnOutAdapters() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.trodix.demo.adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("com.trodix.demo.adapter.out..")
            .as("IN adapters should not depend on OUT adapters");

        rule.check(classes);
    }

    /**
     * Les adapters OUT ne doivent pas dépendre des adapters IN
     */
    @Test
    void outAdaptersShouldNotDependOnInAdapters() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.trodix.demo.adapter.out..")
            .should().dependOnClassesThat().resideInAPackage("com.trodix.demo.adapter.in..")
            .as("OUT adapters should not depend on IN adapters");

        rule.check(classes);
    }

    /**
     * Les classes de configuration doivent être annotées @Configuration
     */
    @Test
    void configurationClassesShouldBeAnnotated() {
        ArchRule rule = classes()
            .that().resideInAPackage("..infrastructure.config..")
            .and().haveSimpleNameEndingWith("Config")
            .or().haveSimpleNameEndingWith("Configuration")
            .should().beAnnotatedWith(org.springframework.context.annotation.Configuration.class)
            .as("Configuration classes should be annotated with @Configuration");

        rule.check(classes);
    }

    /**
     * Les mappers MapStruct doivent être des interfaces dans le package mapper
     */
    @Test
    void mappersShouldBeInterfacesInMapperPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Mapper")
            .and().resideInAPackage("..adapter..")
            .should().beInterfaces()
            .andShould().resideInAPackage("..adapter..mapper..")
            .as("Mappers should be interfaces in the mapper package");

        rule.check(classes);
    }

    /**
     * Les mappers MapStruct doivent être annotés @Mapper
     */
    @Test
    void mappersShouldBeAnnotatedWithMapStructAnnotation() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Mapper")
            .and().resideInAPackage("..adapter..mapper..")
            .and().areInterfaces()
            .should().beAnnotatedWith(org.mapstruct.Mapper.class)
            .as("Mappers should be annotated with @Mapper");

        rule.check(classes);
    }

    /**
     * RÈGLE STRICTE HEXAGONALE : Application NE DOIT JAMAIS dépendre de Adapter
     * Application doit être complètement indépendante de la couche Adapter
     * Utilise les ports (interfaces dans application.port) pour l'inversion de dépendance
     */
    @Test
    void applicationShouldNotDependOnAdapterLayer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.trodix.demo.application..")
            .should().dependOnClassesThat().resideInAnyPackage("..adapter..")
            .because("Application layer must be completely independent of Adapter layer in strict hexagonal architecture. " +
                     "Application defines Ports (interfaces in application.port), Adapters implement them.")
            .as("Application should NOT depend on Adapter layer");

        rule.check(classes);
    }


    /**
     * RÈGLE STRICTE HEXAGONALE : Application NE DOIT JAMAIS dépendre de Infrastructure
     * Application doit rester indépendante des frameworks et de la configuration technique
     */
    @Test
    void applicationShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.trodix.demo.application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..")
            .because("Application layer must be independent of Infrastructure (config, filters, etc.). " +
                     "Infrastructure depends on Application, not the reverse.")
            .as("Application should NOT depend on Infrastructure layer");

        rule.check(classes);
    }
}
