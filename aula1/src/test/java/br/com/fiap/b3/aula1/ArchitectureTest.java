package br.com.fiap.b3.aula1;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/**
 * Protege as dependências de controller, domain, infra e presenter.
 *
 * <p>Quem chama: Maven Surefire ao executar mvn verify.
 *
 * <p>Quem é chamado: ArchUnit sobre as classes de produção, sem incluir os testes.
 */
class ArchitectureTest {
  private static final com.tngtech.archunit.core.domain.JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("br.com.fiap.b3");

  @Test
  void domainDependsOnlyOnJavaAndDomain() {
    classes()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage("java..", "..domain..")
        .check(CLASSES);
  }

  @Test
  void presenterDependsOnlyOnDomainAndPresentation() {
    classes()
        .that()
        .resideInAPackage("..presenter..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage("java..", "..domain..", "..presenter..")
        .check(CLASSES);
  }

  @Test
  void controllersCannotAccessInfraDirectly() {
    noClasses()
        .that()
        .resideInAPackage("..controller..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infra..")
        .check(CLASSES);
  }

  @Test
  void infraCannotCallControllers() {
    noClasses()
        .that()
        .resideInAPackage("..infra..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..controller..")
        .check(CLASSES);
  }
}
