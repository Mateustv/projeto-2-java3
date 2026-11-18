package br.com.caixa.cobranca.registro;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ObservabilidadeTest {

  @Test
  void healthLiveEReadyFicamUp() {
    given()
      .when().get("/q/health/live")
      .then()
      .statusCode(200)
      .body("status", equalTo("UP"));

    given()
      .when().get("/q/health/ready")
      .then()
      .statusCode(200)
      .body("status", equalTo("UP"));
  }

  @Test
  void metricasEstaoDisponiveis() {
    given()
      .when().get("/q/metrics")
      .then()
      .statusCode(200);
  }
}
