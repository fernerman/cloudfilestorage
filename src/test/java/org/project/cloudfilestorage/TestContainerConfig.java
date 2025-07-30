package org.project.cloudfilestorage;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class TestContainerConfig {

  @Container
  public  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");
  @Container
  public static GenericContainer<?> minio = new GenericContainer<>("minio/minio")
      .withExposedPorts(9000)
      .withEnv("MINIO_ACCESS_KEY", "test-access-key")
      .withEnv("MINIO_SECRET_KEY", "test-secret-key")
      .withCommand("server /data")
      .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000).forStatusCode(200));
  @Container
  public static GenericContainer<?> redis = new GenericContainer<>("redis:6.0.10")
      .withExposedPorts(6379);
  @DynamicPropertySource
  public static void dynamicProperties(DynamicPropertyRegistry registry) {
    postgres.start();
    redis.start();
    minio.start();

    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
    registry.add("minio.access-key", () -> "test-access-key");
    registry.add("minio.secret-key", () -> "test-secret-key");
    registry.add("minio.bucket", () -> "test-bucket");
  }
//  @Test
//  public void testDatabaseAndMinioIntegration() {
//    // Проводим интеграционные тесты с PostgreSQL и MinIO
//    System.out.println("Postgres URL: " + postgres.getJdbcUrl());
//    System.out.println("Redis URL: " + redis.getHost() + ":" + redis.getMappedPort(6379));
//    System.out.println("MinIO URL: " + "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
//
//    // Здесь вы можете писать ваши тесты, которые используют базу данных и MinIO.
//  }
}
