package org.mtech.ledger;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packagesOf = LedgerApplication.class)
class AnnotationTest {

    private static final String NULL_MARKED = "org.jspecify.annotations.NullMarked";

    @ArchTest
    void givenProductionClasses_thenResideInNullMarkedPackage(JavaClasses classes) {
        classes()
                .should(new ArchCondition<>("reside in a @NullMarked package") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        if (!javaClass.getPackage().isAnnotatedWith(NULL_MARKED)) {
                            events.add(SimpleConditionEvent.violated(
                                    javaClass,
                                    "Package %s of class %s is not annotated with @NullMarked"
                                            .formatted(javaClass.getPackageName(), javaClass.getName())));
                        }
                    }
                })
                .check(classes);
    }

    @ArchTest
    void givenConfigurationClasses_thenResideInAllowedPackages(JavaClasses classes) {
        classes()
                .that()
                .areAnnotatedWith("org.springframework.context.annotation.Configuration")
                .should()
                .resideInAnyPackage("..configuration..", "..adapter..", "..common..")
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