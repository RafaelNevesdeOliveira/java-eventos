package br.com.fiap.b3.aula2;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  private static final com.tngtech.archunit.core.domain.JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("br.com.fiap.b3");

  @Test
  void domainDependsOnlyOnJavaAndDomain() {
    classes()
        .that()
        .resideInAPackage("..ordens.domain..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage("java..", "..ordens.domain..")
        .check(CLASSES);
  }

  @Test
  void applicationCannotDependOnFrameworksOrAdapters() {
    classes()
        .that()
        .resideInAPackage("..application..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage("java..", "..application..", "..ordens.domain..")
        .check(CLASSES);
  }

  @Test
  void inboundAdaptersCannotBypassUseCasesAndCallOutboundAdapters() {
    noClasses()
        .that()
        .resideInAPackage("..adapters.in..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..adapters.out..")
        .check(CLASSES);
  }
}
