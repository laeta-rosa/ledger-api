package org.mtech.ledger;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(packagesOf = LedgerApplication.class)
class NameTest {

    @ArchTest
    void givenRestControllers_thenSuffixedWithController(JavaClasses classes) {
        classes()
                .that()
                .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should()
                .haveSimpleNameEndingWith("Controller")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenUseCaseClasses_thenSuffixedWithUseCase(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..application..")
                .and()
                .areAnnotatedWith("org.springframework.stereotype.Service")
                .should()
                .haveSimpleNameEndingWith("UseCase")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenRepositoryClasses_thenSuffixedWithRepository(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..adapter.outbound.repository..")
                .and()
                .areInterfaces()
                .and()
                .doNotHaveSimpleName("package-info")
                .should()
                .haveSimpleNameEndingWith("Repository")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenConfigurationClasses_thenSuffixedWithConfiguration(JavaClasses classes) {
        classes()
                .that()
                .areAnnotatedWith("org.springframework.context.annotation.Configuration")
                .should()
                .haveSimpleNameEndingWith("Configuration")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenPropertiesClasses_thenSuffixedWithProperties(JavaClasses classes) {
        classes()
                .that()
                .areAnnotatedWith("org.springframework.boot.context.properties.ConfigurationProperties")
                .should()
                .haveSimpleNameEndingWith("Properties")
                .allowEmptyShould(true)
                .check(classes);
    }
}