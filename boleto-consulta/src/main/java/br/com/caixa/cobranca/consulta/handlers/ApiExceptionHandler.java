package br.com.caixa.cobranca.consulta.handlers;

import br.com.caixa.cobranca.consulta.dtos.ApiError;
import br.com.caixa.cobranca.consulta.exceptions.IdentificadorInvalidoException;
import br.com.caixa.cobranca.consulta.exceptions.RecursoNaoEncontradoException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Provider
public class ApiExceptionHandler implements ExceptionMapper<RuntimeException> {

  private static final Logger LOG = Logger.getLogger(ApiExceptionHandler.class);

  @Override
  public Response toResponse(RuntimeException exception) {
    if (exception instanceof IdentificadorInvalidoException) {
      return build(400, "BAD_REQUEST", exception.getMessage());
    }
    if (exception instanceof RecursoNaoEncontradoException) {
      return build(404, "NOT_FOUND", exception.getMessage());
    }
    if (exception instanceof ConstraintViolationException validationException) {
      return mapConstraintViolationException(validationException);
    }
    if (exception instanceof WebApplicationException webEx) {
      return mapWebApplicationException(webEx);
    }
    LOG.error("Erro inesperado ao processar requisicao", exception);
    return build(500, "INTERNAL_SERVER_ERROR", "erro inesperado");
  }

  @ServerExceptionMapper
  public Response mapConstraintViolationException(ConstraintViolationException exception) {
    return build(400, "BAD_REQUEST", validationMessage(exception));
  }

  private String validationMessage(ConstraintViolationException exception) {
    String message = exception.getConstraintViolations().stream()
      .map(violation -> {
        String path = violation.getPropertyPath().toString();
        int separator = path.lastIndexOf('.');
        String field = separator < 0 ? path : path.substring(separator + 1);
        return field.isBlank() ? violation.getMessage() : field + ": " + violation.getMessage();
      })
      .sorted()
      .collect(Collectors.joining("; "));
    return message.isBlank() ? "dados invalidos" : message;
  }

  @ServerExceptionMapper
  public Response mapWebApplicationException(WebApplicationException exception) {
    Response original = exception.getResponse();
    if (original == null || original.getStatus() < 400 || original.getStatus() >= 600) {
      LOG.error("WebApplicationException sem status HTTP de erro valido", exception);
      return build(500, "INTERNAL_SERVER_ERROR", "erro inesperado");
    }

    int status = original.getStatus();
    ApiError error = new ApiError(status, errorCode(status), errorMessage(status));
    return Response.fromResponse(original)
      .type(MediaType.APPLICATION_JSON_TYPE)
      .entity(error)
      .build();
  }

  private String errorCode(int status) {
    return switch (status) {
      case 400 -> "BAD_REQUEST";
      case 401 -> "UNAUTHORIZED";
      case 403 -> "FORBIDDEN";
      case 404 -> "NOT_FOUND";
      case 405 -> "METHOD_NOT_ALLOWED";
      case 406 -> "NOT_ACCEPTABLE";
      case 408 -> "REQUEST_TIMEOUT";
      case 409 -> "CONFLICT";
      case 413 -> "PAYLOAD_TOO_LARGE";
      case 415 -> "UNSUPPORTED_MEDIA_TYPE";
      case 422 -> "UNPROCESSABLE_ENTITY";
      case 429 -> "TOO_MANY_REQUESTS";
      case 500 -> "INTERNAL_SERVER_ERROR";
      case 501 -> "NOT_IMPLEMENTED";
      case 502 -> "BAD_GATEWAY";
      case 503 -> "SERVICE_UNAVAILABLE";
      case 504 -> "GATEWAY_TIMEOUT";
      default -> status >= 500 ? "INTERNAL_SERVER_ERROR" : "HTTP_ERROR";
    };
  }

  private String errorMessage(int status) {
    return switch (status) {
      case 400 -> "requisicao invalida";
      case 401 -> "autenticacao necessaria";
      case 403 -> "acesso negado";
      case 404 -> "recurso nao encontrado";
      case 405 -> "metodo nao permitido";
      case 406 -> "tipo de resposta nao aceito";
      case 408 -> "tempo limite da requisicao excedido";
      case 409 -> "conflito de recurso";
      case 413 -> "corpo da requisicao muito grande";
      case 415 -> "tipo de conteudo nao suportado";
      case 422 -> "regra de negocio nao atendida";
      case 429 -> "limite de requisicoes excedido";
      case 502 -> "erro no servico dependente";
      case 503 -> "servico indisponivel";
      case 504 -> "tempo limite no servico dependente";
      default -> status >= 500 ? "erro interno" : "requisicao rejeitada";
    };
  }

  private Response build(int status, String codigo, String mensagem) {
    return Response.status(status)
      .type(MediaType.APPLICATION_JSON_TYPE)
      .entity(new ApiError(status, codigo, mensagem))
      .build();
  }
}
