package org.ballastlane.pokeapp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "org.ballastlane.pokeapp",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final String DOMAIN = "..pokeapp.domain..";
  private static final String APPLICATION = "..pokeapp.application..";
  private static final String INFRASTRUCTURE = "..pokeapp.infrastructure..";
  private static final String WEB = "..pokeapp.web..";
  private static final String CONFIG = "..pokeapp.config..";

  @ArchTest
  static final ArchRule layersRespectTheDependencyRule =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .withOptionalLayers(true)
          .layer("Domain")
          .definedBy(DOMAIN)
          .layer("Application")
          .definedBy(APPLICATION)
          .layer("Infrastructure")
          .definedBy(INFRASTRUCTURE)
          .layer("Web")
          .definedBy(WEB)
          .layer("Config")
          .definedBy(CONFIG)
          .whereLayer("Domain")
          .mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Web", "Config")
          .whereLayer("Application")
          .mayOnlyBeAccessedByLayers("Infrastructure", "Web", "Config")
          .whereLayer("Infrastructure")
          .mayOnlyBeAccessedByLayers("Config")
          .whereLayer("Web")
          .mayOnlyBeAccessedByLayers("Config");

  @ArchTest
  static final ArchRule coreIsFrameworkFree =
      noClasses()
          .that()
          .resideInAnyPackage(DOMAIN, APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "jakarta.persistence..",
              "jakarta.validation..",
              "com.fasterxml..",
              "org.hibernate..");

  @ArchTest
  static final ArchRule controllersDependOnInputPortsOnly =
      noClasses()
          .that()
          .resideInAPackage(WEB)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application.service..", "..application.port.out..");

  @ArchTest
  static final ArchRule jpaEntitiesStayInPersistence =
      classes()
          .that()
          .haveSimpleNameEndingWith("JpaEntity")
          .should()
          .resideInAPackage("..infrastructure.persistence..");

  @ArchTest
  static final ArchRule useCaseImplementationsAreNotSpringBeans =
      noClasses()
          .that()
          .resideInAPackage("..application.service..")
          .should()
          .beAnnotatedWith("org.springframework.stereotype.Service");
}
