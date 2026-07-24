package org.mtech.ledger;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(packagesOf = LedgerApplication.class)
class StructureTest {

    @ArchTest
    void givenDomainClasses_thenNotAnnotatedWithSpringStereotypes(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .notBeAnnotatedWith("org.springframework.stereotype.Component")
                .andShould()
                .notBeAnnotatedWith("org.springframework.stereotype.Service")
                .andShould()
                .notBeAnnotatedWith("org.springframework.stereotype.Repository")
                .andShould()
                .notBeAnnotatedWith("org.springframework.context.annotation.Configuration")
                .because("""
                the domain is framework-agnostic and must not know about the DI container; a Spring \
                stereotype (@Component/@Service/@Repository/@Configuration) on a domain class couples the \
                core model to the framework and lets infrastructure concerns leak inward. Move that wiring \
                to an adapter or configuration class and keep the domain a plain-Java model.""")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenCommandClasses_thenAreRecords(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..application..")
                .and()
                .haveSimpleNameEndingWith("Command")
                .should()
                .beRecords()
                .because("""
                a *Command is an immutable input to a command use case, so a record keeps it a pure data \
                carrier with value semantics and immutability.""")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenQueryClasses_thenAreRecords(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..application..")
                .and()
                .haveSimpleNameEndingWith("Query")
                .should()
                .beRecords()
                .because("""
                a *Query is an immutable input to a query use case, so a record keeps it a pure data \
                carrier with value semantics and immutability.""")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenResultClasses_thenAreRecords(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..application..")
                .and()
                .haveSimpleNameEndingWith("Result")
                .should()
                .beRecords()
                .because("""
                a *Result is an immutable output of a use case that decouples adapters from the domain \
                aggregate, so a record keeps it a pure data carrier with value semantics and immutability.""")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenAllClasses_thenDoNotUseFieldInjection(JavaClasses classes) {
        NO_CLASSES_SHOULD_USE_FIELD_INJECTION.check(classes);
    }

    @ArchTest
    void givenAllClasses_thenDoNotAccessStandardStreams(JavaClasses classes) {
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(classes);
    }

    @ArchTest
    void givenAllClasses_thenDoNotThrowGenericExceptions(JavaClasses classes) {
        NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(classes);
    }

    @ArchTest
    void givenAllClasses_thenDoNotUseJavaUtilLogging(JavaClasses classes) {
        NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(classes);
    }
}