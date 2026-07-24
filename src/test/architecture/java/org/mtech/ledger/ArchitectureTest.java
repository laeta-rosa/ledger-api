package org.mtech.ledger;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(packagesOf = LedgerApplication.class)
class ArchitectureTest {

    private static final String BASE = LedgerApplication.class.getPackageName() + "..";

    private static final DescribedPredicate<JavaClass> CONCRETE_USE_CASE = resideInAPackage("..application..")
            .and(simpleNameEndingWith("UseCase"))
            .and(describe("are concrete", (JavaClass uc) -> !uc.getModifiers().contains(JavaModifier.ABSTRACT)));

    @ArchTest
    void givenDomainSlices_thenFreeOfCycles(JavaClasses classes) {
        slices().matching("..domain.(**)..")
                .should()
                .beFreeOfCycles()
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenApplicationSlices_thenFreeOfCycles(JavaClasses classes) {
        slices().matching("..application.(**)..")
                .should()
                .beFreeOfCycles()
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenDomainLayer_thenOnlyDependOnDomain(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .onlyDependOnClassesThat(resideOutsideOfPackage(BASE).or(resideInAnyPackage("..domain..")))
                .allowEmptyShould(true)
                .because("""
                the domain is the innermost layer: it may depend only on other domain classes and on \
                external (third-party/JDK) types — never on the application, adapter, or common layers. \
                Depending on an outer layer inverts the dependency direction and lets infrastructure \
                concerns leak into the core model. Move the offending collaborator behind a domain \
                abstraction the domain owns, or relocate the logic out of the domain.""")
                .check(classes);
    }

    @ArchTest
    void givenApplicationLayer_thenOnlyDependOnAllowedLayers(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..application..")
                .should()
                .onlyDependOnClassesThat(resideOutsideOfPackage(BASE)
                        .or(resideInAnyPackage("..domain..", "..application..", "..adapter.outbound..", "..common..")))
                .allowEmptyShould(true)
                .because("""
                the application layer orchestrates the domain and drives side effects through outbound \
                adapters, so it may depend on the domain, itself, outbound adapters, and common — but never \
                on inbound adapters, which would invert the driving/driven direction and let a use case \
                reach back into a controller. Depend on the domain or an outbound adapter instead of an \
                inbound type.""")
                .check(classes);
    }

    @ArchTest
    void givenApplicationSegments_thenNotDependOnEachOther(JavaClasses classes) {
        slices().matching("..application.(*)..")
                .should()
                .notDependOnEachOther()
                .because("""
                each top-level application segment (the first package under the application package) is an \
                independent feature and must not depend on another segment; this is the coarse, \
                package-level counterpart to the rule forbidding one use-case class from depending on \
                another. A violation means two features are coupled at the package level — keep shared \
                logic in the domain or behind an outbound adapter rather than reaching across segments.""")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenUseCaseClass_thenNotDependOnAnotherUseCaseClass(JavaClasses classes) {
        noClasses()
                .that(CONCRETE_USE_CASE)
                .should()
                .dependOnClassesThat(CONCRETE_USE_CASE)
                .because("""
                a use case must not call another use case directly, at any package depth (the segment \
                slice rule only catches cross-segment dependencies). The rule targets concrete *UseCase \
                classes in the application layer, so implementing the CommandUseCase/QueryUseCase base \
                interface is not a violation. Direct calls create hidden coupling and stack two business \
                operations into one transaction — share logic through the domain or an outbound adapter \
                injected into both, or orchestrate from an inbound adapter.""")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenInboundAdapterSlices_thenNotDependOnEachOther(JavaClasses classes) {
        slices().matching("..adapter.inbound.(*)..")
                .should()
                .notDependOnEachOther()
                .because("""
                inbound adapter slices (the first package under adapter.inbound, e.g. rest) are \
                independent entry points and must not depend on each other; one transport must not reach \
                into another's package — route shared behaviour through the application layer instead.""")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void givenInboundAdapters_thenOnlyDependOnAllowedLayers(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..adapter.inbound..")
                .should()
                .onlyDependOnClassesThat(resideOutsideOfPackage(BASE)
                        .or(resideInAnyPackage("..domain..", "..application..", "..adapter.inbound..", "..common..")))
                .allowEmptyShould(true)
                .because("""
                inbound (driving) adapters must reach side effects through the application layer, not by \
                depending on adapter.outbound directly; otherwise a controller could call a repository and \
                skip the use case entirely. A violation usually means an inbound class imports an outbound \
                repository or its types. Fix it by routing the call through a use case in the application \
                layer rather than adding adapter.outbound to this allow-list.""")
                .check(classes);
    }

    @ArchTest
    void givenOutboundAdapters_thenOnlyDependOnAllowedLayers(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..adapter.outbound..")
                .should()
                .onlyDependOnClassesThat(resideOutsideOfPackage(BASE)
                        .or(resideInAnyPackage("..domain..", "..application..", "..adapter.outbound..", "..common..")))
                .allowEmptyShould(true)
                .because("""
                outbound (driven) adapters must not depend on adapter.inbound (driving) adapters; that \
                inverts the hexagonal boundary. A violation usually means an outbound class imports an \
                inbound controller or request/response DTO. Fix it by breaking that dependency — move the \
                shared type into the domain or application layer, or duplicate the small DTO — rather than \
                adding a package to this allow-list.""")
                .check(classes);
    }
}