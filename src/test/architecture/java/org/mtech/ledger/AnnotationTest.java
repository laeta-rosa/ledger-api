package org.mtech.ledger;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(packagesOf = LedgerApplication.class)
class AnnotationTest {

    @ArchTest
    void givenConfigurationClasses_thenResideInAllowedPackages(JavaClasses classes) {
        classes()
                .that()
                .areAnnotatedWith("org.springframework.context.annotation.Configuration")
                .should()
                .resideInAnyPackage("..configuration..", "..adapter..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenRestControllers_thenResideInInboundAdapter(JavaClasses classes) {
        classes()
                .that()
                .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should()
                .resideInAPackage("..adapter.inbound..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenRepositoryClasses_thenResideInOutboundAdapter(JavaClasses classes) {
        classes()
                .that()
                .areAssignableTo("org.springframework.data.repository.Repository")
                .should()
                .resideInAPackage("..adapter.outbound..")
                .allowEmptyShould(true)
                .check(classes);
    }
}