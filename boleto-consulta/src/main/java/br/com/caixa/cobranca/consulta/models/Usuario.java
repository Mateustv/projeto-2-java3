package br.com.caixa.cobranca.consulta.models;

import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
@UserDefinition
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Username
  @Column(nullable = false, unique = true)
  public String username;

  @Password
  @Column(nullable = false)
  public String senha;

  @Roles
  @Column(nullable = false)
  public String papeis;
}
