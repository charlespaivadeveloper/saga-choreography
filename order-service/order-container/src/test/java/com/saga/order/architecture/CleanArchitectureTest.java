package com.saga.order.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Clean Architecture Rules")
class CleanArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.saga.order");
    }

    @Test
    @DisplayName("domain core should not depend on Spring Framework")
    void domainCoreShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.saga.order.domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .because("Domain layer must be framework-independent (Clean Architecture)");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("domain core should not depend on infrastructure")
    void domainCoreShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.saga.order.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.saga.order.infrastructure..")
            .because("Domain must not know about infrastructure (Dependency Rule)");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("application service should not depend on infrastructure")
    void applicationServiceShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.saga.order.service..")
            .and().doNotHaveSimpleName("OrderCreateCommandHandler")
            .and().doNotHaveSimpleName("PaymentResponseMessageListenerImpl")
            .and().doNotHaveSimpleName("InventoryResponseMessageListenerImpl")
            .should().dependOnClassesThat().resideInAPackage("com.saga.order.infrastructure..")
            .because("Application service must not depend on infrastructure (Dependency Inversion)");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("infrastructure should not depend on REST controllers")
    void infrastructureShouldNotDependOnRestLayer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.saga.order.infrastructure..")
            .should().dependOnClassesThat().resideInAPackage("com.saga.order.application.rest..")
            .because("Infrastructure must not depend on presentation layer");

        rule.check(importedClasses);
    }
}
