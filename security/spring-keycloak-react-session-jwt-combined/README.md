# Seamless Authentication System: Integrating Keycloak with Spring Boot, Thymeleaf, and React Using OAuth2 and JWT

Authentication is a critical aspect of modern web applications, and Keycloak provides a powerful, open-source identity and access management solution. In this article, we’ll explore integrating Keycloak with a Spring Boot backend—using Thymeleaf for server-side rendering—and a React frontend styled with Tailwind CSS 4 and DaisyUI 5 (beta). We’ll use the same credentials across both, leveraging OAuth2 for session-based web authentication and JWT for stateless API security, with endpoint-specific configurations. This approach ensures a unified user experience across traditional web pages and modern single-page applications (SPAs).

---

## Prerequisites

To follow this tutorial, ensure you have:
- **Java 17+**: Required for Spring Boot 3.x.
- **Node.js 20+**: For Vite and React.
- **Docker**: To run Keycloak and PostgreSQL.
- **Dependencies**: Spring Boot starters for `web`, `security`, `oauth2-client`, `oauth2-resource-server`, and `thymeleaf`.

---

## Setting Up Keycloak with PostgreSQL

We’ll deploy Keycloak and PostgreSQL using Docker Compose for a persistent database setup:

```yaml
# docker-compose.yaml
services:
  postgres:
    image: postgres:latest                  # Official PostgreSQL image
    environment:
      POSTGRES_USER: postgres              # Database username
      POSTGRES_PASSWORD: mysecretpassword  # Database password
      POSTGRES_DB: keycloak                # Database name for Keycloak
    ports:
      - "5432:5432"                        # Expose PostgreSQL port
    volumes:
      - postgres_data:/var/lib/postgresql/data  # Persist data
    networks:
      - database                           # Connect to custom network

  keycloak:
    image: quay.io/keycloak/keycloak:latest  # Latest Keycloak image
    environment:
      KEYCLOAK_ADMIN: admin                # Admin username for Keycloak
      KEYCLOAK_ADMIN_PASSWORD: admin       # Admin password
      KC_HTTP_ENABLED: true                # Enable HTTP access
      KC_DB: postgres                      # Use PostgreSQL as the database
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak  # Database URL
      KC_DB_USERNAME: postgres             # Database username
      KC_DB_PASSWORD: mysecretpassword     # Database password
    ports:
      - "8088:8080"                        # Map host port 8088 to container port 8080
    command:
      - start-dev                          # Run in development mode
    depends_on:
      - postgres                           # Ensure PostgreSQL starts first
    networks:
      - database                           # Connect to custom network

volumes:
  postgres_data:                          # Named volume for PostgreSQL data persistence

networks:
  database:
    driver: bridge                        # Use bridge networking
    name: database                        # Network name
```

Run `docker-compose up` to start Keycloak at `http://localhost:8088` and PostgreSQL at `localhost:5432`. Log in with `admin/admin`, create a realm called `my-realm`, and configure two clients:
- **spring-boot-app**: A confidential client for Spring Boot.
- **react-app**: A public client for React.

Create a realm role `USER` and assign it to a test user (e.g., username: `testuser`, password: `password`) to enable unified login.

### Keycloak Client Configurations
- **spring-boot-app**:
    - Client ID: `spring-boot-app`
    - Client Authentication: On (confidential)
    - Valid Redirect URIs: `http://localhost:8081/*`
    - Valid Post Logout Redirect URIs: `http://localhost:8081/login?logout`
    - Web Origins: `*`

- **react-app**:
    - Client ID: `react-app`
    - Client Authentication: Off (public)
    - Valid Redirect URIs: `http://localhost:5173/*`
    - Valid Post Logout Redirect URIs: `http://localhost:5173/*`
    - Web Origins: `*`

---

## Spring Boot Backend Setup

The backend combines a Thymeleaf web UI with session-based OAuth2 and a REST API secured with JWT, powered by Keycloak.

### Dependencies
In `pom.xml`, include:
```xml
<dependencies>
    <!-- Core web functionality -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- Security framework -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <!-- OAuth2 client for session-based login -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    <!-- OAuth2 resource server for JWT validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    <!-- Thymeleaf for server-side rendering -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
</dependencies>
```

### Application Configuration
Configure Keycloak in `application.yml`:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: spring-boot-app          # Client ID for Spring Boot
            client-secret: oFKSAvp334RG5oTQwjlmS3LJNSNkvMTN  # Client secret
            scope: openid,profile,email         # Requested scopes
        provider:
          keycloak:
            issuer-uri: http://localhost:8088/realms/my-realm  # Keycloak realm URL
server:
  port: 8081                                   # Spring Boot runs on port 8081
```

#### Breakdown
- **`spring.security.oauth2.client`**: Configures the OAuth2 client for session-based login.
- **`client-id` and `client-secret`**: Credentials for `spring-boot-app`.
- **`issuer-uri`**: Keycloak’s realm endpoint for OAuth2 discovery.
- **`server.port`**: Runs the app on `8081` to avoid conflicts.

---

### Security Configuration
The updated `SecurityConfig` class defines two filter chains with explicit endpoint matching:

```java
package com.mahmud.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository; // Repository for OAuth2 client registrations

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    // Filter chain for session-based web UI
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/login",                     // Custom login page
                "/login/oauth2/*/**",         // OAuth2 callback endpoints
                "/oauth2/*/**",               // Additional OAuth2 paths
                "/home",                      // Protected home page
                "/logout",                    // Logout endpoint
                "/public"                     // Public page
            )
            .cors(cors -> cors.configurationSource(request -> {   // CORS configuration
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // Allow React origin
                config.setAllowedMethods(Arrays.asList("GET", "POST"));          // Allowed HTTP methods
                config.setAllowedHeaders(Arrays.asList("Authorization"));        // Allow Authorization header
                config.setAllowCredentials(false);                               // No credentials for simplicity
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/public").permitAll()    // Public access to these endpoints
                .anyRequest().authenticated()                        // All other endpoints require auth
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")                                 // Custom login page
                .defaultSuccessUrl("/home", true)                    // Redirect after login
            )
            .logout(logout -> logout
                .logoutUrl("/logout")                                // Logout endpoint
                .logoutSuccessHandler(oidcLogoutSuccessHandler())    // Handle logout with Keycloak
                .invalidateHttpSession(true)                         // Clear session
                .clearAuthentication(true)                           // Clear auth context
            );
        return http.build();
    }

    // Filter chain for JWT-based API
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/secured")                             // Apply to /secured endpoint only
            .cors(cors -> cors.configurationSource(request -> {      // CORS configuration
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // Allow React origin
                config.setAllowedMethods(Arrays.asList("GET", "POST"));          // Allowed methods
                config.setAllowedHeaders(Arrays.asList("Authorization"));        // Allow Authorization header
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()                        // Require authentication
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))               // Validate JWT with custom decoder
            );
        return http.build();
    }

    // Custom logout handler for OAuth2 logout with Keycloak
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:8081/login?logout"); // Redirect after logout
        return logoutSuccessHandler;
    }

    // JWT decoder to validate tokens from Keycloak
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:8088/realms/my-realm/protocol/openid-connect/certs").build();
    }
}
```

#### Breakdown
- **`webSecurityFilterChain`**: Secures Thymeleaf endpoints with `oauth2Login`. Updated `securityMatcher` includes OAuth2 callback paths (`/login/oauth2/*/**`, `/oauth2/*/**`) for proper redirect handling.
- **`apiSecurityFilterChain`**: Secures `/secured` with `oauth2ResourceServer` for JWT validation.
- **`cors`**: Allows React at `http://localhost:5173` to access endpoints, supporting `Authorization` headers.
- **`jwtDecoder`**: Validates JWTs using Keycloak’s JWKS endpoint.
- **`oidcLogoutSuccessHandler`**: Manages logout with Keycloak integration.

---

### Web Controller (Thymeleaf)
```java
package com.mahmud.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "login";  // Returns the login Thymeleaf template
    }

    @GetMapping("/public")
    public String publicPage(Model model) {
        model.addAttribute("message", "This is a public page!"); // Adds message to the model
        return "public";  // Returns the public Thymeleaf template
    }

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal OidcUser user) {
        model.addAttribute("username", user.getPreferredUsername()); // Adds username from OIDC user
        return "home";  // Returns the home Thymeleaf template
    }
}
```

#### Breakdown
- **`login`**: Serves a login page linking to Keycloak’s OAuth2 flow.
- **`publicPage`**: A publicly accessible page with a message.
- **`home`**: A protected page displaying the authenticated user’s username.

---

### API Controller (JWT)
```java
package com.mahmud.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DemoController {

    @GetMapping("/secured")
    public Map<String, String> securedMethod(@AuthenticationPrincipal Jwt jwt) {
        Map<String, String> map = new HashMap<>();
        map.put("message", "this is a secure message");              // Response message
        map.put("username", jwt.getClaimAsString("preferred_username")); // Username from JWT
        return map;                                                  // Returns JSON response
    }
}
```

#### Breakdown
- **`securedMethod`**: A REST endpoint secured with JWT, returning a message and username from the token.

---

### Thymeleaf Templates
- **`login.html`**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Login</title></head>
<body>
    <h1>Login</h1>
    <!-- Link to initiate Keycloak OAuth2 login -->
    <a href="/oauth2/authorization/keycloak">Login with Keycloak</a>
    <!-- Display logout message if present -->
    <p th:if="${param.logout}">You have been logged out.</p>
</body>
</html>
```

- **`public.html`**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Public Page</title></head>
<body>
    <h1>Public Page</h1>
    <!-- Display message from the model -->
    <p th:text="${message}"></p>
    <!-- Link to login page -->
    <a href="/login">Go to Login</a>
</body>
</html>
```

- **`home.html`**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Home</title></head>
<body>
    <!-- Display welcome message with username -->
    <h1>Welcome, <span th:text="${username}"></span>!</h1>
    <!-- Logout form -->
    <form th:action="@{/logout}" method="post">
        <button type="submit">Logout</button>
    </form>
</body>
</html>
```

#### Breakdown
- **`login.html`**: Links to Keycloak’s OAuth2 login flow.
- **`public.html`**: Displays a public message with a login option.
- **`home.html`**: Shows the username and a logout button for authenticated users.

---

## React Frontend Setup

The React frontend uses Vite, Tailwind CSS 4, DaisyUI 5 (beta), and `keycloak-js`.

### Project Initialization
1. **Create the React App with Vite**:
   ```bash
   npm create vite@latest keycloak-react --template react
   cd keycloak-react
   npm install
   ```

2. **Install Tailwind CSS 4**:
   ```bash
   npm install -D tailwindcss @tailwindcss/vite
   ```
   Update `vite.config.js`:
   ```javascript
   // vite.config.js
   import { defineConfig } from 'vite';
   import tailwindcss from '@tailwindcss/vite';

   export default defineConfig({
     plugins: [
       tailwindcss(), // Integrate Tailwind CSS with Vite
     ],
   });
   ```

3. **Install DaisyUI 5 (Beta)**:
   ```bash
   npm install -D daisyui@beta
   ```
   Update `src/index.css`:
   ```css
   /* src/index.css */
   @import "tailwindcss";  // Import Tailwind CSS
   @plugin "daisyui";      // Add DaisyUI as a plugin
   ```

4. **Install Additional Dependencies**:
   ```bash
   npm install keycloak-js react-router-dom
   ```

### Keycloak Initialization
In `src/keycloak.js`:
```javascript
// src/keycloak.js
import Keycloak from "keycloak-js";

// Initialize Keycloak instance with configuration
const keycloak = new Keycloak({
    url: "http://localhost:8088/", // Keycloak server URL
    realm: "my-realm",            // Realm name
    clientId: "react-app",        // Client ID for React
});

export default keycloak;
```

#### Breakdown
- **`keycloak`**: Configures `keycloak-js` with the `react-app` client settings.

---

### Main Entry (`main.jsx`)
```javascript
// src/main.jsx
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Public from './pages/Public.jsx';

// Define routes for the app
const router = createBrowserRouter([
    { path: '/', element: <App /> },         // Root route to App
    { path: '/public', element: <Public /> } // Public page route
]);

// Render the app with RouterProvider
createRoot(document.getElementById('root')).render(
    <RouterProvider router={router} />
);
```

#### Breakdown
- **`router`**: Sets up routing with `react-router-dom`.
- **`createRoot`**: Renders the React app.

---

### App Component (`App.jsx`)
```javascript
// src/App.jsx
import { useEffect, useState } from "react";
import keycloak from "./keycloak";

const App = () => {
    const [authenticated, setAuthenticated] = useState(false); // Track authentication status
    const [data, setData] = useState(null);                   // Store API response

    useEffect(() => {
        // Initialize Keycloak and require login
        keycloak
            .init({ onLoad: "login-required" })
            .then((authenticated) => {
                setAuthenticated(authenticated);
                if (authenticated) {
                    // Fetch secured endpoint with JWT
                    fetch("http://localhost:8081/secured", {
                        headers: {
                            Authorization: `Bearer ${keycloak.token}`, // Attach JWT
                        },
                    })
                        .then((res) => {
                            if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
                            return res.json();                     // Parse JSON response
                        })
                        .then((d) => setData(d))                   // Set response data
                        .catch((err) => console.error("Fetch error:", err));
                }
            })
            .catch((err) => console.error("Keycloak init error:", err));
    }, []);

    // Handle logout with redirect
    const handleLogout = () => {
        keycloak.logout({ redirectUri: "http://localhost:5173/public" });
    };

    if (!authenticated) {
        return <div className="text-center mt-10">Loading...</div>; // Loading state
    }

    return (
        <div className="min-h-screen bg-base-200 p-4">
            {/* Welcome message with Tailwind and DaisyUI styling */}
            <h1 className="text-3xl font-bold text-center mb-4">
                Welcome, {keycloak.tokenParsed?.preferred_username}
            </h1>
            {/* Logout button */}
            <button onClick={handleLogout} className="btn btn-secondary mb-4">
                Logout
            </button>
            <div className="card bg-base-100 shadow-xl p-4">
                <h3 className="text-xl font-semibold">Response from back-end:</h3>
                {data ? (
                    <div>
                        <h1 className="text-2xl mt-2">{data.message}</h1>
                        <p className="mt-2">Username: {data.username}</p>
                    </div>
                ) : (
                    <p className="mt-2">Loading data...</p>
                )}
            </div>
        </div>
    );
};

export default App;
```

#### Breakdown
- **`useEffect`**: Forces login via Keycloak and fetches `/secured` with JWT.
- **`handleLogout`**: Logs out and redirects to `/public`.
- **UI**: Uses Tailwind CSS 4 and DaisyUI 5 for a responsive card layout.

---

### Public Page (`pages/Public.jsx`)
```javascript
// src/pages/Public.jsx
const Public = () => {
    return (
        <div className="min-h-screen bg-base-200 flex items-center justify-center">
            <div className="card bg-base-100 shadow-xl p-6">
                <h1 className="text-2xl font-bold">Public Page</h1>
                <p className="mt-2">This is a public page accessible to all.</p>
            </div>
        </div>
    );
};

export default Public;
```

#### Breakdown
- **`Public`**: A styled public page using Tailwind and DaisyUI components.

---

## How It Works

- **Session-Based Web UI**:
    - `http://localhost:8081/login` triggers Keycloak’s OAuth2 login.
    - Post-login, `/home` uses a session cookie to display the username.
    - Logout redirects to `/login?logout`.

- **JWT-Based React Frontend**:
    - React forces login via `keycloak-js` and fetches `/secured` with a JWT.
    - Displays the response in a styled UI.

- **Unified Credentials**:
    - Both clients share the `my-realm` realm and `USER` role, allowing `testuser/password` to work across the board.

---

## Testing the Integration

1. **Start Keycloak and PostgreSQL**: `docker-compose up`.
2. **Start Spring Boot**: `mvn spring-boot:run`.
3. **Start React**: `npm run dev` (runs on `http://localhost:5173`).
4. **Test**:
    - `http://localhost:8081/public`: Public Thymeleaf page.
    - `http://localhost:8081/home`: Protected Thymeleaf page.
    - `http://localhost:5173/`: React app with secured API data.

---

## Conclusion

This integration harnesses Keycloak’s versatility to unify authentication across Spring Boot with Thymeleaf and React with Tailwind CSS 4 and DaisyUI 5 (beta). By splitting security into session-based and JWT-based flows with precise endpoint matching, we cater to diverse client needs seamlessly. For production, secure with HTTPS and refine CORS settings. This setup provides a robust foundation for modern full-stack applications.
