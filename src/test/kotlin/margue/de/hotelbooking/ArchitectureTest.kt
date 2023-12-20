package margue.de.hotelbooking

import com.tngtech.archunit.core.importer.*
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*
import org.junit.jupiter.api.*

class ArchitectureTest {

    @Test
    fun some_architecture_rule() {
        val importedClasses = ClassFileImporter().importPackages("margue.de.hotelbooking")

        // web to domain
        val webToDomain = classes().that()
            .resideInAPackage("web").should().onlyAccessClassesThat().resideInAPackage("domain")
            .allowEmptyShould(true)
        webToDomain.check(importedClasses)

        // domain to persistence
        val domainToPersistence = classes().that()
            .resideInAPackage("domain").should().onlyAccessClassesThat().resideInAPackage("persistence")
            .allowEmptyShould(true)
        domainToPersistence.check(importedClasses)

        // persistence has no dependencies
        val noDependenciesForPersistence = classes().that()
            .resideInAPackage("persistence").should().onlyBeAccessed().byAnyPackage()
            .allowEmptyShould(true)
        noDependenciesForPersistence.check(importedClasses)
    }
}
