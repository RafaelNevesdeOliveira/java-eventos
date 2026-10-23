package br.com.fiap.b3.aula1.infra.config;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.auth.SessionTokenAuthenticationDetailsProvider;
import com.oracle.bmc.queue.QueueClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Compõe o cliente Queue somente no perfil OCI com a sessão temporária da CLI deste Mac.
 *
 * <p>Quem chama: Spring. Quem é chamado: SDK OCI, que lê ~/.oci/config sem embutir chaves no projeto.
 * O endpoint de mensagens é obrigatório e deve ser o retornado por GetQueue.
 */
@Configuration
@Profile("oci")
public class OciQueueConfiguration {
  @Bean(destroyMethod = "close")
  SessionTokenAuthenticationDetailsProvider ociAuthentication(
      @Value("${lab.oci.profile:JORNADA_B3}") String profile) throws IOException {
    return new SessionTokenAuthenticationDetailsProvider(profile);
  }

  @Bean(destroyMethod = "close")
  QueueClient ociQueueClient(
      SessionTokenAuthenticationDetailsProvider authentication,
      @Value("${lab.oci.queue-endpoint}") String endpoint) {
    return QueueClient.builder()
        .endpoint(endpoint)
        .configuration(ClientConfiguration.builder()
            .connectionTimeoutMillis(5000).readTimeoutMillis(15000).build())
        .build(authentication);
  }
}
