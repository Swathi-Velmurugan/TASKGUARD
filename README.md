TaskGuard Frontend (HTML / CSS / vanilla JS + Bootstrap + Axios)
==================================================================

HOW TO RUN
----------
1. Start your Spring Boot backend (localhost:8080).
2. Open this folder with VS Code "Live Server" (or any static file server)
   and open login.html. Don't just double-click the HTML file - some
   browsers block localStorage/fetch on file:// pages.

REQUIRED BACKEND CHANGE - CORS
-------------------------------
Your SecurityConfig currently has no CORS configuration at all. Without it,
the browser will block every request from your frontend page to
http://localhost:8080 (the request never even reaches your controllers).

Add this to SecurityConfig.java:

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

And update securityFilterChain to actually use it and to let CORS
pre-flight (OPTIONS) requests through:

    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/login", "/api/accounts").permitAll()
            .anyRequest().authenticated());

(imports needed: org.springframework.web.cors.CorsConfiguration,
 org.springframework.web.cors.CorsConfigurationSource,
 org.springframework.web.cors.UrlBasedCorsConfigurationSource,
 org.springframework.http.HttpMethod, java.util.List)

FIRST ACCOUNT / LOGGING IN
--------------------------
Your backend has no DataSeeder, and account creation (POST /api/accounts)
requires an already-logged-in PROJECT_DIRECTOR (@PreAuthorize on the method
runs even though the URL itself is "permitAll"). So there's no public
self-registration path - by design, only a director can provision accounts.

That means you need at least one PROJECT_DIRECTOR row in your database
before this frontend can be used at all. If you don't already have one,
insert it directly into MySQL, e.g.:

    INSERT INTO system_account (email, password_hash, full_name, domain_role, is_active, created_at)
    VALUES ('director@gmail.com', '$2a$10$<bcrypt-hash-of-password>', 'Director', 'PROJECT_DIRECTOR', 1, NOW());

(You can generate a BCrypt hash quickly with an online bcrypt generator,
or write a tiny throwaway @Test that prints new BCryptPasswordEncoder().encode("password").)

Once you can log in as that director, use the "Provision Account" button
on the Accounts page to create your PROJECT_MANAGER and TEAM_CONTRIBUTOR
accounts normally.

A FEW REAL QUIRKS IN YOUR BACKEND THIS FRONTEND WORKS AROUND
--------------------------------------------------------------
- Login only returns a token (no id/email/role) - the frontend decodes the
  JWT itself to get your email and role.
- GET /api/accounts is PROJECT_DIRECTOR-only, so Managers/Contributors can't
  look up account IDs. That means on the "Add Task" form, the Manager has
  to type the contributor's Account ID by hand (ask the Director for it).
- GET /api/tasks and GET /api/submissions are only allowed for
  PROJECT_MANAGER and TEAM_CONTRIBUTOR (not PROJECT_DIRECTOR) - so those two
  tabs are hidden for the Director role, matching what your API actually allows.
- SystemAccount's password field is literally named "passwordHash" even when
  creating an account (the raw password is sent there and the server BCrypt-
  encodes it) - the "Provision Account" form sends it that way on purpose.
