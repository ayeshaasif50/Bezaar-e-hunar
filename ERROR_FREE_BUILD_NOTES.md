# Error-Free Build Notes

The previous build failed while Spring Data MongoDB was creating `MongoTemplate` because the local Java/Atlas connection ended during the SSL handshake. The application itself compiled, but startup depended on an external Atlas connection.

This build makes the university demo self-contained:

1. MongoDB runtime dependency was removed from the default build.
2. `UserRepository` is now a Spring repository backed by a local JSON file.
3. Registration, login, JWT, BCrypt, role checks, profile endpoint, and Proxy Pattern remain functional.
4. Backend default port is 8081.
5. Frontend default API URL is `http://localhost:8081/api`.
6. One demo ADMIN account is seeded automatically.
7. Helper PowerShell launchers are included at the project root.

MongoDB Atlas can be re-added in a later database sprint using a repository adapter without changing controllers or authentication flow.
