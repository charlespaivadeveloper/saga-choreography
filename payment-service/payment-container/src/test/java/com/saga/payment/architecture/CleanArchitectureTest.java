package com.saga.payment.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Payment Service Clean Architecture Rules")
class CleanArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.saga.payment");
    }

    @Test
    @DisplayName("domain core should not depend on Spring Framework")
    void domainCoreShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.saga.payment.domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .because("Domain layer must be framework-independent (Clean Architecture)");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("domain core should not depend on infrastructure")
    void domainCoreShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.saga.payment.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.saga.payment.infrastructure..")
            .because("Domain must not know about infrastructure (Dependency Rule)");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("application service should not depend on infrastructure")
    void applicationServiceShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.saga.payment.service..")
            .and().doNotHaveSimpleName("OrderEventMessageListenerImpl")
            .should().dependOnClassesThat().resideInAPackage("com.saga.payment.infrastructure..")
            .because("Application service must not depend on infrastructure (Dependency Inversion)");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("domain should not depend on JPA annotations")
    void domainShouldNotDependOnJpa() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.saga.payment.domain..")
            .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
            .because("Domain entities must not use JPA annotations");

        rule.check(importedClasses);
    }
}
