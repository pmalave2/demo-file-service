package com.demos.file_service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection(name = "azure-storage/azurite")
  GenericContainer<?> azuriteContainer() {
    return new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:latest"))
        .withExposedPorts(10000, 10001, 10002);
  }

  @Bean
  @ServiceConnection
  MariaDBContainer<?> mariaDbContainer() {
    return new MariaDBContainer<>(DockerImageName.parse("mariadb:latest"));
  }

  @Bean
  DynamicPropertyRegistrar azuriteContainerRegistrar(
      @Qualifier("azuriteContainer") GenericContainer<?> azuriteContainer) {
    var string = String.format("http://%s:%s/devstoreaccount1", azuriteContainer.getHost(),
        azuriteContainer.getMappedPort(10000));

    return registry -> {
      registry.add("spring.cloud.azure.storage.blob.endpoint",
          () -> string);
    };
  }
}
