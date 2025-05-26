// com/example/olx/infrastructure/security/DemoPasswordHasherImpl.java
package com.example.olx.infrastructure.security;

// ВАЖЛИВО: Це НЕБЕЗПЕЧНА реалізація лише для демонстрації!
// У реальному проекті використовуйте bcrypt, scrypt, Argon2.
public class DemoPasswordHasherImpl implements PasswordHasher {
    private static final String SALT = "demo_salt_"; // Ніколи не використовуйте статичну сіль так!

    @Override
    public String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return "hashed_empty_password";
        }
        // Проста імітація хешування
        return SALT + new StringBuilder(plainPassword).reverse().toString() + "_hashed";
    }

    @Override
    public boolean check(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return hashedPassword.equals(hash(plainPassword));
    }
}