# 🎨 NUCLEUS — Bonnes pratiques & Patterns

## 📋 Table des matières

1. Bonnes pratiques générales
2. Patterns par couche
3. Anti-patterns à éviter
4. Checklist de qualité
5. Exemples concrets

---

## 🎯 Bonnes pratiques générales

### ✅ 1. Utiliser des enums typés (JAMAIS de Strings)

```kotlin
// ❌ MAUVAIS
policyEngine.checkAndConsume(
    "PLANILOISIR",
    "PLANNING_CREATE",
    accountId
)

// ✅ BON
policyEngine.checkAndConsume(
    namespace = OperationNamespace.PLANILOISIR,
    operationCode = OperationCode.PLANILOISIR__PLANNING_CREATE,
    accountId = accountId
)
```

**Avantages :**
- ✅ Type-safe : erreurs détectées à la compilation
- ✅ IDE autocomplete
- ✅ Validation au boot (`NucleusCatalogueInitValidator`)
- ✅ Refactoring sûr

---

### ✅ 2. Synchroniser Code + BD

Quand tu ajoutes une opération :

```kotlin
// 1️⃣ Ajouter dans l'enum
enum class OperationCode {
    PLANILOISIR__PLANNING_CREATE,
    PLANILOISIR__NEW_FEATURE,  // ← NOUVEAU
}

// 2️⃣ Ajouter dans Flyway
// V5__seed_platform_operation_definitions.sql
INSERT INTO mp_operation_definitions (operation_code, namespace, group_code, is_billable)
VALUES ('PLANILOISIR__NEW_FEATURE', 'PLANILOISIR', 'PL_PLANNING', false);

// 3️⃣ Passer les tests
// NucleusCatalogueInitValidator va automatiquement valider
./gradlew test
```

---

### ✅ 3. Ajouter @NucleusOp à tous les services métier

```kotlin
@Service
class MyService {
    // ✅ BON : La fonction est instrumentalisée
    @NucleusOp(
        op = Operation.PLANILOISIR_REGISTER,
        extraTags = ["context" to "user_registration"]
    )
    fun registerUser(email: String): Mono<User> {
        // Logs automatiques
        // Metrics automatiques
        // Tracing automatique
        return userRepository.save(User(email = email))
    }
}
```

**Bénéfices :**
- ✅ Logs visuels `[NUCLEUS_OP START/END]`
- ✅ Métriques Micrometer : `op.planiLoisir.register`
- ✅ Spans OpenTelemetry
- ✅ Contexte MDC propagé

---

### ✅ 4. Respecter le flux de consentement

```kotlin
@RestController
class RegisterUserController(
    private val service: PlaniLoisirService
) {
    @PostMapping("/register")
    fun register(
        @RequestHeader(NucleusHeader.CONSENT_VERSION.headerName) consent: String,
        @RequestBody request: RegisterRequest
    ): Mono<ResponseEntity<User>> {
        // ✅ Le filter ConsentGuard a déjà validé que consent est présent
        // ✅ On peut procéder sans vérifier à nouveau
        return service.registerUser(request)
            .map { ResponseEntity.created(URI.create("/users/${it.id}")).body(it) }
    }
}
```

---

### ✅ 5. Documenter les opérations dans OpenAPI

```kotlin
@PostMapping("/register")
@Operation(
    summary = "Register a new user",
    description = "Register a new user with interests. Requires X-Consent-Version header."
)
@Parameters(
    Parameter(
        name = NucleusHeader.CONSENT_VERSION.headerName,
        description = "Consent version (required for writes)",
        required = true,
        `in` = ParameterIn.HEADER
    )
)
@ApiResponse(responseCode = "201", description = "User created")
@ApiResponse(responseCode = "400", description = "Missing X-Consent-Version")
fun register(
    @RequestHeader(NucleusHeader.CONSENT_VERSION.headerName) consent: String,
    @RequestBody request: RegisterRequest
): Mono<ResponseEntity<User>> {
    // ...
}
```

---

## 🏭 Patterns par couche

### Couche 1 : CONTROLLER

#### Pattern A : Externaliser la logique de parsing

```kotlin
// ❌ MAUVAIS
@PostMapping("/register")
fun register(@RequestBody raw: Map<String, Any>): Mono<ResponseEntity<User>> {
    val email = (raw["email"] as String).trim()
    val interests = (raw["interests"] as List<*>).map { it.toString() }
    // ... grosse logique de parsing
}

// ✅ BON
@PostMapping("/register")
fun register(@Valid @RequestBody request: RegisterRequest): Mono<ResponseEntity<User>> {
    // Le parsing est fait par Spring (ValidatingWebFluxConfiguration)
    return service.registerUser(request)  // ← Delegate au service
        .map { ResponseEntity.created(...).body(it) }
}
```

#### Pattern B : Utiliser les DTOs typés

```kotlin
// ✅ BON
data class RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email
    val email: String,
    
    @Size(min = 1, message = "At least 1 interest required")
    val interests: List<String>
)

// ✅ BON : Réponse aussi typée
data class UserResponse(
    val id: Long,
    val email: String,
    val interestCount: Int,
    val createdAt: LocalDateTime
)
```

---

### Couche 2 : FILTERS

#### Pattern A : Utiliser beforeCommit pour modifier les headers

```kotlin
// ✅ BON : Modification de la réponse via beforeCommit
class LatencyBudgetFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val start = System.currentTimeMillis()
        
        return chain.filter(exchange).doFinally { _ ->
            val elapsed = System.currentTimeMillis() - start
            
            if (elapsed > budgetMs) {
                // ✅ Utiliser beforeCommit pour éviter ReadOnlyHttpHeaders
                exchange.response.beforeCommit {
                    exchange.response.headers.add(
                        NucleusHeader.LATENCY_OVERBUDGET.headerName,
                        "true"
                    )
                    Mono.empty()
                }
            }
        }
    }
}
```

#### Pattern B : Propager le contexte MDC

```kotlin
// ✅ BON
class TracingFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val traceId = UUID.randomUUID().toString()
        
        return chain.filter(exchange)
            .contextWrite(Context.of("traceId", traceId))
            .doOnEachEvent { signal ->
                MDC.put("traceId", traceId)
            }
    }
}
```

---

### Couche 3 : ASPECT AOP

#### Pattern A : Instrumentation réactive

```kotlin
// ✅ BON : Gérer Mono et Flux
@Around("@annotation(nucleusOp)")
fun instrument(pjp: ProceedingJoinPoint, nucleusOp: NucleusOp): Any? {
    val result = pjp.proceed()
    
    return when (result) {
        is Mono<*> -> {
            (result as Mono<Any?>)
                .doOnSuccess { recordSuccess(nucleusOp) }
                .doOnError { recordError(nucleusOp) }
        }
        is Flux<*> -> {
            (result as Flux<Any?>)
                .doOnNext { recordSuccess(nucleusOp) }
                .doOnError { recordError(nucleusOp) }
        }
        else -> result
    }
}
```

---

### Couche 4 : SERVICES NUCLEUS

#### Pattern A : Résoudre avant de décider

```kotlin
// ✅ BON
@Service
class PolicyEngineRuntimeService(
    private val catalogueResolver: NucleusCatalogueResolver
) {
    fun checkAndConsume(
        namespace: OperationNamespace,
        operationCode: OperationCode,
        accountId: Long
    ): Mono<String> {
        // 1️⃣ Résoudre la définition
        return catalogueResolver.resolveOperation(operationCode)
            .flatMap { opDef ->
                // 2️⃣ Vérifier les capabilities
                if (!opDef.allowed) {
                    return@flatMap Mono.just("BLOCKED: operation not available")
                }
                
                // 3️⃣ Consommer les quotas
                consumeQuota(accountId, operationCode)
            }
    }
}
```

---

### Couche 5 : SERVICES MÉTIER

#### Pattern A : Orchestration avec @NucleusOp

```kotlin
// ✅ BON
@Service
class PlaniLoisirService(
    private val repository: PlaniLoisirUserRepository,
    private val policyEngine: PolicyEngineRuntimeService
) {
    @NucleusOp(op = Operation.PLANILOISIR_REGISTER)
    fun registerUser(request: RegisterRequest, accountId: Long): Mono<User> {
        return policyEngine.checkAndConsume(
            namespace = OperationNamespace.PLANILOISIR,
            operationCode = OperationCode.PLANILOISIR__PLANNING_CREATE,
            accountId = accountId
        ).flatMap { result ->
            if (!result.contains("ALLOWED")) {
                Mono.error(QuotaExceededException("Cannot create planning"))
            } else {
                repository.save(mapToEntity(request))
            }
        }
    }
}
```

#### Pattern B : Mapper les entités correctement

```kotlin
// ✅ BON : Mappers en companion object
@Service
class PlaniLoisirService {
    companion object {
        private fun mapToEntity(request: RegisterRequest, accountId: Long): PlaniLoisirUser =
            PlaniLoisirUser(
                accountId = accountId,
                email = request.email,
                interests = request.interests,
                createdAt = LocalDateTime.now()
            )
        
        private fun mapToResponse(entity: PlaniLoisirUser): UserResponse =
            UserResponse(
                id = entity.id!!,
                email = entity.email,
                interestCount = entity.interests.size,
                createdAt = entity.createdAt
            )
    }
}
```

---

### Couche 6 : REPOSITORIES

#### Pattern A : Requêtes typées et cachées

```kotlin
// ✅ BON
interface UserRepository : R2dbcRepository<User, Long> {
    @Query("""
        SELECT * FROM users WHERE email = :email LIMIT 1
    """)
    fun findByEmail(email: String): Mono<User>
    
    @Query("""
        SELECT DISTINCT u.* FROM users u
        JOIN user_interests ui ON u.id = ui.user_id
        JOIN interests i ON ui.interest_id = i.id
        WHERE i.keyword IN (:keywords)
    """)
    fun findByInterestKeywords(keywords: List<String>): Flux<User>
}

// ❌ MAUVAIS : Exposer SQL dans le service
fun findUsers(keywords: List<String>): Flux<User> {
    val sql = "SELECT * FROM users WHERE ... LIKE ..."
    return db.sql(sql).map { ... }
}
```

#### Pattern B : Gérer les erreurs de concurrence

```kotlin
// ✅ BON
fun save(user: User): Mono<User> {
    return repository.save(user)
        .onErrorResume { ex ->
            if (ex is DataIntegrityViolationException && ex.message?.contains("email_unique") == true) {
                Mono.error(EmailAlreadyExistsException(user.email))
            } else {
                Mono.error(ex)
            }
        }
}
```

---

### Couche 7 : BASE DE DONNÉES

#### Pattern A : Migrations incrémentales

```sql
-- V20__add_new_operation.sql
BEGIN;

-- 1️⃣ Ajouter à la table
INSERT INTO mp_operation_definitions (operation_code, namespace, group_code, is_billable)
VALUES ('PLANILOISIR__NEW_FEATURE', 'PLANILOISIR', 'PL_FEATURES', false);

-- 2️⃣ Ajouter les entitlements pour les plans existants
INSERT INTO mp_quota_entitlements (plan_code, operation_code)
SELECT DISTINCT plan_code, 'PLANILOISIR__NEW_FEATURE'
FROM mp_quota_entitlements
WHERE namespace = 'PLANILOISIR';

COMMIT;
```

#### Pattern B : Indices pour les requêtes fréquentes

```sql
-- V100__performance_indices.sql

-- Index sur les recherches par email
CREATE INDEX idx_users_email ON users(email);

-- Index sur les jointures user_interests
CREATE INDEX idx_user_interests_user_id ON user_interests(user_id);
CREATE INDEX idx_user_interests_interest_id ON user_interests(interest_id);

-- Index sur les quotas consommés
CREATE INDEX idx_quota_consumed_account_period ON mp_quota_consumed(account_id, period_start);
```

---

## ❌ Anti-patterns à éviter

### ❌ 1. Strings magiques

```kotlin
// ❌ MAUVAIS
if (operationType == "PLANILOISIR.REGISTER") { ... }
quotaKey == "RES_AI_SUGGESTIONS" { ... }

// ✅ BON
if (operationType == OperationCode.PLANILOISIR__PLANNING_CREATE) { ... }
quotaKey == QuotaKey.RES_AI_SUGGESTIONS { ... }
```

### ❌ 2. Ignorer les filtres Nucleus

```kotlin
// ❌ MAUVAIS : Vérifier le consentement manuellement
@PostMapping("/register")
fun register(@RequestBody request: RegisterRequest): Mono<User> {
    val consent = request.headers.get("X-Consent-Version") ?: throw Exception("No consent")
    // ...
}

// ✅ BON : Laisser le filtre ConsentGuard faire le travail
@PostMapping("/register")
fun register(
    @RequestHeader(NucleusHeader.CONSENT_VERSION.headerName) consent: String,
    @RequestBody request: RegisterRequest
): Mono<User> {
    // Le filtre a déjà validé
    // ...
}
```

### ❌ 3. Appels bloquants en WebFlux

```kotlin
// ❌ MAUVAIS : Thread.sleep en Mono
fun process(): Mono<User> {
    return Mono.fromCallable {
        Thread.sleep(1000)  // ← BLOQUE le thread
        "result"
    }
}

// ✅ BON : Utiliser Mono.delay
fun process(): Mono<User> {
    return Mono.delay(Duration.ofSeconds(1))
        .map { "result" }
}
```

### ❌ 4. Modification des headers après commit

```kotlin
// ❌ MAUVAIS : Exception ReadOnlyHttpHeaders
return chain.filter(exchange).doFinally { _ ->
    exchange.response.headers.add(HEADER, "value")  // ← Trop tard !
}

// ✅ BON : Utiliser beforeCommit
return chain.filter(exchange).doFinally { _ ->
    exchange.response.beforeCommit {
        exchange.response.headers.add(HEADER, "value")
        Mono.empty()
    }
}
```

### ❌ 5. Ajouter une opération sans mettre à jour la BD

```kotlin
// ❌ MAUVAIS : Drift détecté au boot
enum class OperationCode {
    NEW_OPERATION,  // ← Ajouté mais pas dans mp_operation_definitions
}

// Le validateur va LOG:
// ❌ NUCLEUS CATALOGUE VALIDATION REPORT
//    Operations Missing in DB: 1
//      - NEW_OPERATION

// ✅ BON : Toujours synchroniser avec Flyway
// 1️⃣ Ajouter dans l'enum
// 2️⃣ Ajouter dans V*.sql
// 3️⃣ ./gradlew test (validation passe)
```

---

## ✅ Checklist de qualité

### Avant de merger une PR

- [ ] **Types sûrs :** Pas de Strings, utiliser les enums Nucleus
- [ ] **Synchronisation :** Code + BD synchronisés
- [ ] **@NucleusOp :** Tous les services métier ont l'annotation
- [ ] **Tests :** Couvrir 7 couches (IT + UT)
- [ ] **Documentation :** OpenAPI à jour + commentaires clairs
- [ ] **Logs :** Voir `[NUCLEUS_OP START/END]` en logs
- [ ] **Métriques :** Vérifier `op.*` dans Actuator
- [ ] **Validation au boot :** `NucleusCatalogueInitValidator` passe
- [ ] **Latence :** Aucune opération > 500ms en DEV
- [ ] **Sécurité :** Consentement/Safety respectés

### Avant de déployer en PROD

- [ ] Tous les tests passent
- [ ] Validation Nucleus catalogue : `status=UP`
- [ ] Logs d'observabilité activés
- [ ] Jaeger/Tempo configuré pour tracer
- [ ] Alertes configurées (p95 latence, erreurs)
- [ ] Rollback plan préparé

---

## 🎯 Exemples concrets

### Exemple 1 : Ajouter une nouvelle opération

**Étapes :**

```kotlin
// 1️⃣ Ajouter dans l'enum
enum class OperationCode {
    PLANILOISIR__EXPORT_PDF,  // ← NOUVEAU
}

enum class Operation {
    PLANILOISIR_EXPORT("planiLoisir", "exportPdf"),  // ← NOUVEAU
}

// 2️⃣ Ajouter dans Flyway
// V30__add_export_operation.sql
INSERT INTO mp_operation_definitions (operation_code, namespace, group_code)
VALUES ('PLANILOISIR__EXPORT_PDF', 'PLANILOISIR', 'PL_EXPORT');

// 3️⃣ Créer le service
@Service
class PlaniLoisirExportService {
    @NucleusOp(op = Operation.PLANILOISIR_EXPORT)
    fun exportToPdf(planningId: Long): Mono<ByteArray> {
        // Logique d'export
    }
}

// 4️⃣ Créer le controller
@PostMapping("/export/{id}/pdf")
fun export(@PathVariable id: Long): Mono<ResponseEntity<ByteArray>> {
    return service.exportToPdf(id)
        .map { ResponseEntity.ok().body(it) }
}

// 5️⃣ Tester
./gradlew test
// ✅ NucleusCatalogueInitValidator passe
```

---

### Exemple 2 : Quota-gater une opération

```kotlin
// 1️⃣ Ajouter le quota
enum class QuotaKey {
    RES_EXPORT_PDF,  // ← NOUVEAU
}

// 2️⃣ Ajouter dans Flyway
INSERT INTO mp_quota_key_definitions (quota_key, category, scope, default_limit, period)
VALUES ('RES_EXPORT_PDF', 'PROVIDER', 'USER', 10, 'MONTHLY');

// 3️⃣ Ajouter dans service
@Service
class PlaniLoisirExportService(
    private val policyEngine: PolicyEngineRuntimeService
) {
    @NucleusOp(op = Operation.PLANILOISIR_EXPORT)
    fun exportToPdf(planningId: Long, accountId: Long): Mono<ByteArray> {
        return policyEngine.checkAndConsume(
            namespace = OperationNamespace.PLANILOISIR,
            operationCode = OperationCode.PLANILOISIR__EXPORT_PDF,
            accountId = accountId,
            units = 1
        ).flatMap { result ->
            if (!result.contains("ALLOWED")) {
                Mono.error(QuotaExceededException("Export quota exceeded"))
            } else {
                generatePdf(planningId)
            }
        }
    }
}

// 4️⃣ Tester
./gradlew test
```

---

### Exemple 3 : Implémenter un guard personnalisé

```kotlin
// ✅ BON : Guard personnalisé pour contrôler une logique métier
@Component
class GeoLocationGuard : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val geoLocation = exchange.request.headers.getFirst("X-Geo-Location")
        
        // Vérifier si l'utilisateur est en région autorisée
        if (!isAuthorizationAllowed(geoLocation)) {
            return sendError(exchange, 403, "GEO_RESTRICTED")
        }
        
        return chain.filter(exchange)
    }
}
```

---

## 📚 Ressources

- **Docs :** `/docs/README_NUCLEUS.md`, `/docs/NUCLEUS_CATALOGUE_CANON.md`
- **Code :** `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/`
- **Tests :** `src/test/kotlin/.../nucleus/`

---

**Dernière mise à jour :** 2026-04-13

