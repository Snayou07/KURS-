// com/example/olx/infrastructure/security/PasswordHasher.java
package com.example.olx.infrastructure.security;

public interface PasswordHasher {
    String hash(String plainPassword);
    boolean check(String plainPassword, String hashedPassword);
}