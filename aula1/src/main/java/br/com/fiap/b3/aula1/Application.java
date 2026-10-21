package br.com.fiap.b3.aula1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Inicializa a aplicação Spring Boot e a descoberta dos componentes da aula.
 *
 * <p>Quem chama: JVM pelo método main, após o build Maven.
 *
 * <p>Quem é chamado: SpringApplication e configurações de infra que compõem os objetos.
 */
@SpringBootApplication
@EnableScheduling
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
