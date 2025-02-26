# Integrating Keycloak with Spring Boot 3 and Spring Security: A Comprehensive Guide

In this article, we’ll dive into Keycloak, an open-source identity and access management (IAM) solution, and demonstrate how to integrate it with a Spring Boot 3 application using Spring Security and JWT for authentication and authorization. We’ll cover setting up Keycloak with Docker Compose and PostgreSQL, exploring its Web UI, interacting with its REST API, understanding realm vs. client roles, and configuring a Spring Boot project for role-based access control (RBAC).

## What is Keycloak?

Keycloak is an open-source IAM solution developed by Red Hat. It provides robust features like single sign-on (SSO), user federation, social login, and fine-grained authorization. Supporting protocols such as OpenID Connect, OAuth 2.0, and SAML, Keycloak is a powerful tool for securing modern applications.

### Key Features:
- **User Management:** Centralized user registration, login, and profile management.
- **Authentication:** SSO across multiple applications.
- **Authorization:** Role-based and attribute-based access control.
- **Extensibility:** Integration with LDAP, Active Directory, and custom providers.

## Why We Need Keycloak?

Modern applications require secure, scalable, and manageable authentication and authorization mechanisms. Keycloak addresses these needs by:
- **Centralizing Authentication:** Eliminates redundant login logic across services.
- **Simplifying Authorization:** Centralizes role and permission management.
- **Reducing Development Overhead:** Offers built-in support for SSO and token-based security (e.g., JWT).
- **Enhancing Security:** Includes features like two-factor authentication (2FA) and token expiration.

For instance, in a microservices architecture, Keycloak can serve as a single authentication server, issuing JWTs that services validate independently.

## Setting Up Keycloak Using Docker Compose

We’ll use Docker Compose to run Keycloak with PostgreSQL for persistent storage.

### Docker Compose Configuration
Here’s the enhanced `docker-compose.yaml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:latest
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: mysecretpassword
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - database

  keycloak:
    image: quay.io/keycloak/keycloak:latest
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_HOSTNAME_STRICT_BACKCHANNEL: true
      KC_HTTP_RELATIVE_PATH: /
      KC_HTTP_ENABLED: true
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: postgres
      KC_DB_PASSWORD: mysecretpassword
    ports:
      - "8088:8080"
    command:
      - start-dev
    depends_on:
      - postgres
    networks:
      - database

networks:
  database:
    driver: bridge

volumes:
  postgres_data:
```

### Breakdown
- **Postgres Service:**
    - Uses `postgres:latest` image.
    - Configures a `keycloak` database with user `postgres` and password `mysecretpassword`.
    - Persists data in a named volume (`postgres_data`).
- **Keycloak Service:**
    - Uses the latest Keycloak image from `quay.io`.
    - Sets admin credentials (`admin/admin`) for the Admin Console.
    - Connects to PostgreSQL via `KC_DB_URL`.
    - Maps host port `8088` to container port `8080`.
    - Runs in development mode (`start-dev`) for simplicity.
- **Networking:** Both services share a `database` bridge network.

### Running the Setup
1. Save as `docker-compose.yaml`.
2. Run:
   ```bash
   docker-compose up -d
   ```
3. Access Keycloak at `http://localhost:8088`.

## Integrating PostgreSQL with Keycloak

By default, Keycloak uses an in-memory H2 database in dev mode, which isn’t production-ready. Integrating PostgreSQL provides:
- **Persistence:** Durable storage for users, realms, and roles.
- **Scalability:** Support for clustering and high availability.

The `docker-compose.yaml` configures this via:
- `KC_DB: postgres`: Specifies PostgreSQL.
- `KC_DB_URL`, `KC_DB_USERNAME`, `KC_DB_PASSWORD`: Matches the `postgres` service.

Keycloak initializes its schema in the `keycloak` database upon startup.

## Exploring the Keycloak Web UI

Before diving into the REST API, let’s explore Keycloak’s Web UI, which provides a user-friendly interface for managing realms, users, and roles.

### Accessing the Admin Console
1. Open `http://localhost:8088` in your browser.
2. Click **Administration Console**.
3. Log in with `admin/admin`.

### Key Features of the Web UI
- **Realm Management:**
    - Click **Create Realm** (top-left dropdown) to add `my-realm`.
    - Configure settings like SSO, tokens, and login options under **Realm Settings**.
- **User Management:**
    - Navigate to **Users** > **Add User** to create users (e.g., `testuser`).
    - Set credentials via the **Credentials** tab and assign roles under **Role Mappings**.
- **Role Management:**
    - Go to **Roles** > **Add Role** to create realm roles like `USER` and `ADMIN`.
- **Client Management:**
    - Under **Clients**, create `spring-boot-app` (set to Public, enable Direct Access Grants).
    - Configure client scopes and mappers to include roles in JWTs.

The Web UI is ideal for initial setup and manual administration, but for automation, we’ll use the REST API.

## Working with Keycloak Using REST API and cURL

Keycloak’s Admin REST API enables programmatic management. Below are all the operations we discussed, using `cURL`. Replace `$TOKEN` with an admin token.

### 1. Obtain Admin Token
```bash
curl -X POST "http://localhost:8088/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=admin"
```
- Extracts `access_token` from the response.

### 2. Realm CRUD Operations
#### Create a Realm
```bash
curl -X POST "http://localhost:8088/admin/realms" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"realm": "my-realm", "enabled": true}'
```

#### Read a Realm
```bash
curl -X GET "http://localhost:8088/admin/realms/my-realm" \
  -H "Authorization: Bearer $TOKEN"
```

#### Update a Realm
```bash
curl -X PUT "http://localhost:8088/admin/realms/my-realm" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"realm": "my-realm", "displayName": "My Updated Realm"}'
```

#### Delete a Realm
```bash
curl -X DELETE "http://localhost:8088/admin/realms/my-realm" \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Realm User CRUD Operations
#### Create a User
```bash
curl -X POST "http://localhost:8088/admin/realms/my-realm/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "enabled": true, "credentials": [{"type": "password", "value": "password123", "temporary": false}]}'
```

#### Read a User
```bash
curl -X GET "http://localhost:8088/admin/realms/my-realm/users?username=testuser" \
  -H "Authorization: Bearer $TOKEN"
```

#### Update a User
```bash
curl -X PUT "http://localhost:8088/admin/realms/my-realm/users/{user-id}" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Test", "lastName": "User"}'
```

#### Delete a User
```bash
curl -X DELETE "http://localhost:8088/admin/realms/my-realm/users/{user-id}" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Realm Role Association
#### Create a Realm Role
```bash
curl -X POST "http://localhost:8088/admin/realms/my-realm/roles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "USER", "description": "User role"}'
```

#### Associate Role with User
Get the role ID:
```bash
curl -X GET "http://localhost:8088/admin/realms/my-realm/roles/USER" \
  -H "Authorization: Bearer $TOKEN"
```
Assign (replace `{user-id}` and `{role-id}`):
```bash
curl -X POST "http://localhost:8088/admin/realms/my-realm/users/{user-id}/role-mappings/realm" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{"id": "{role-id}", "name": "USER"}]'
```

### 5. User Login JWT
```bash
curl -X POST "http://localhost:8088/realms/my-realm/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=spring-boot-app" \
  -d "username=testuser" \
  -d "password=password123" \
  -d "client_secret=oFKSAvp334RG5oTQwjlmS3LJNSNkvMTN" \
  -d "scope=openid"
```

### 6. User Registration
```bash
curl -X POST "http://localhost:8088/admin/realms/my-realm/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "email": "newuser@example.com", "enabled": true, "emailVerified": false, "credentials": [{"type": "password", "value": "newpassword123", "temporary": false}]}'
```

## Realm Roles vs. Client Roles

Keycloak distinguishes between:
- **Realm Roles:**
    - Defined at the realm level (e.g., `USER`, `ADMIN`).
    - Apply globally across all clients.
    - Appear in JWT under `realm_access.roles`.
    - Use case: Broad permissions like "admin" or "user".
- **Client Roles:**
    - Specific to a client (e.g., `spring-boot-app`).
    - Scoped to that client’s context.
    - Appear in JWT under `resource_access.{client-id}.roles`.
    - Use case: Fine-grained permissions like "read" or "write" for a service.

Example JWT:
```json
{
  "realm_access": {"roles": ["USER"]},
  "resource_access": {"spring-boot-app": {"roles": ["read"]}}
}
```

## Setting Up Spring Boot 3 and Spring Security with Keycloak

Let’s integrate Keycloak with Spring Boot 3 using your code, enhanced with comments.

### Maven `pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.mahmud</groupId>
    <artifactId>keycloak-jwt</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>keycloak-jwt</name>
    <description>Keycloak JWT Integration Demo</description>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <!-- Web support for REST APIs -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- Spring Security core -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <!-- OAuth2 Resource Server for JWT validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>
        <!-- Test dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Breakdown
- **Parent:** Uses Spring Boot 3.4.3.
- **Dependencies:**
    - `spring-boot-starter-web`: REST API support.
    - `spring-boot-starter-security`: Core security.
    - `spring-boot-starter-oauth2-resource-server`: JWT validation.
- **Java Version:** Set to 21 per your setup.

---

### Application Configuration (`application.yaml`)
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8088/realms/my-realm
          jwk-set-uri: http://localhost:8088/realms/my-realm/protocol/openid-connect/certs
```

#### Breakdown
- **issuer-uri:** Matches Keycloak’s realm issuer for JWT validation.
- **jwk-set-uri:** Keycloak’s public key endpoint for signature verification.

---

### Security Configuration (`SecurityConfig.java`)
```java
package com.mahmud.keycloakjwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity // Enables Spring Security web features
@EnableMethodSecurity // Enables method-level security (e.g., @PreAuthorize)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST APIs
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions, JWT-based
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public").permitAll() // Allow unauthenticated access to /public
                .requestMatchers("/user/**").hasRole("USER") // Restrict /user/** to USER role
                .requestMatchers("/admin/**").hasRole("ADMIN") // Restrict /admin/** to ADMIN role
                .anyRequest().authenticated() // All other requests require authentication
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())) // Custom JWT converter
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract realm roles from JWT
            var realmAccess = (java.util.Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", java.util.Collections.emptyMap());
            var realmRoles = (List<String>) realmAccess.getOrDefault("roles", java.util.Collections.emptyList());

            // Convert realm roles to Spring Security authorities with ROLE_ prefix
            return realmRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        });
        return converter;
    }
}
```

#### Breakdown
- **Annotations:**
    - `@EnableWebSecurity`: Activates Spring Security.
    - `@EnableMethodSecurity`: Enables `@PreAuthorize`.
- **SecurityFilterChain:**
    - Disables CSRF and sessions for stateless API.
    - Configures RBAC: `/user/**` for `ROLE_USER`, `/admin/**` for `ROLE_ADMIN`.
    - Uses OAuth2 resource server with JWT.
- **JwtAuthenticationConverter:**
    - Extracts roles from `realm_access.roles`.
    - Adds `ROLE_` prefix for Spring Security.

---

### Controller (`DemoController.java`)
```java
package com.mahmud.keycloakjwt.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint!";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')") // Requires ROLE_USER authority
    public String userEndpoint() {
        return "Hello, User!";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')") // Requires ROLE_ADMIN authority
    public String adminEndpoint() {
        return "Hello, Admin!";
    }
}
```

#### Breakdown
- **Endpoints:**
    - `/public`: Open to all.
    - `/user`: Secured with `ROLE_USER`.
    - `/admin`: Secured with `ROLE_ADMIN`.
- **`@PreAuthorize`:** Enforces method-level RBAC.

---

## Configuring JwtAuthenticationConverter for Realm Roles vs. Client Roles

The default converter handles realm roles. For client roles (e.g., from `spring-boot-app`), update it:

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        // Extract realm roles
        var realmAccess = (java.util.Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", java.util.Collections.emptyMap());
        var realmRoles = (List<String>) realmAccess.getOrDefault("roles", java.util.Collections.emptyList());

        // Extract client roles for 'spring-boot-app'
        var resourceAccess = (java.util.Map<String, Object>) jwt.getClaims().getOrDefault("resource_access", java.util.Collections.emptyMap());
        var clientAccess = (java.util.Map<String, Object>) resourceAccess.getOrDefault("spring-boot-app", java.util.Collections.emptyMap());
        var clientRoles = (List<String>) clientAccess.getOrDefault("roles", java.util.Collections.emptyList());

        // Combine realm and client roles with ROLE_ prefix
        var authorities = realmRoles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
        authorities.addAll(clientRoles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .toList());

        return authorities;
    });
    return converter;
}
```

### Breakdown
- **Realm Roles:** From `realm_access.roles`.
- **Client Roles:** From `resource_access.spring-boot-app.roles`.
- **Combination:** Both prefixed with `ROLE_` and merged into authorities.
- **Usage:** Client role `read` becomes `ROLE_read`, usable in `@PreAuthorize("hasRole('read')")`.

---

## Running and Testing

1. **Start Keycloak and Postgres:**
   ```bash
   docker-compose up -d
   ```
2. **Configure Keycloak via Web UI:**
    - Create realm `my-realm`.
    - Create client `spring-boot-app` (Public, Direct Access Grants enabled).
    - Add realm roles `USER` and `ADMIN`.
    - Create user `testuser`, assign `USER` role.
3. **Run Spring Boot:**
   ```bash
   mvn spring-boot:run
   ```
4. **Test:**
    - Get JWT:
      ```bash
      curl -X POST "http://localhost:8088/realms/my-realm/protocol/openid-connect/token" \
        -d "grant_type=password" \
        -d "client_id=spring-boot-app" \
        -d "username=testuser" \
        -d "password=password123"
      ```
    - Access `/user`:
      ```bash
      curl -H "Authorization: Bearer <token>" http://localhost:8081/user
      ```

## More Resources
- [Keycloak]()
- [Keeycloak Official Docs]()
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/index.html)
- [Spring Security Official Docs](https://spring.io/projects/spring-security/)
- [Configuring Keycloak](https://www.keycloak.org/guides)
- [Using Relational Database with Keycloak](https://www.keycloak.org/server/db)

## Conclusion

Keycloak streamlines authentication and authorization for Spring Boot applications. This guide covered its setup with Docker and PostgreSQL, Web UI exploration, full REST API usage, role distinctions, and Spring Security integration, providing a robust foundation for secure application development.
perations and the Web UI section, ensuring completeness.
