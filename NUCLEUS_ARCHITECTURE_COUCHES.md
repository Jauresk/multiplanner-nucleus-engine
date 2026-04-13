# 🏛️ NUCLEUS — Architecture en couches & Organisation

## 📐 Vue en couches

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        COUCHE 1 : PRÉSENTATION                          │
│  Controllers (@RestController)                                          │
│  ├─ NucleusProbeController (endpoints probe/démo)                       │
│  ├─ RegisterUserController (register flow)                              │
│  └─ ... autres contrôleurs métier                                       │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ HTTP Requests
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              COUCHE 2 : FILTRES & MIDDLEWARES (WebFlux)                │
│  ├─ ConsentGuard : bloque les writes sans X-Consent-Version            │
│  ├─ SafetyShield : vérifie X-Safety-Score >= seuil                     │
│  ├─ LatencyBudgetFilter : ajoute header X-Latency-Overbudget           │
│  └─ TracingFilter : propage MDC (traceId, spanId)                      │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ request validated
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│           COUCHE 3 : ASPECT AOP (@NucleusOp)                           │
│  ├─ Injecte les logs [NUCLEUS_OP START/END]                            │
│  ├─ Crée les spans OpenTelemetry                                       │
│  ├─ Enregistre les métriques Micrometer (timers)                       │
│  └─ Propage le contexte MDC                                            │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ operation instrumented
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│        COUCHE 4 : SERVICES NUCLEUS CATALOGUES (Business Logic)         │
│  ├─ NucleusCatalogueResolver : résout les opérations/quotas            │
│  ├─ PolicyEngineRuntimeService : check & consume quotas (TYPÉ)         │
│  └─ CatalogueRuntimeService : cache du catalogue                       │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ operation resolved
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│           COUCHE 5 : SERVICES MÉTIER (Domain Logic)                    │
│  ├─ PlaniLoisirService @ @NucleusOp(OP_PLANILOISIR_REGISTER)          │
│  ├─ PlaniDécouverteService @ @NucleusOp(OP_DISCOVERY_AI_POST)         │
│  ├─ BillingService (si app.billing.enabled)                           │
│  └─ ... autres services métier                                         │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ business logic executed
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│         COUCHE 6 : REPOSITORIES & PERSISTENCE (Data Layer)             │
│  ├─ UserRepository (réactif R2DBC)                                     │
│  ├─ PlanningRepository                                                 │
│  ├─ QuotaRepository (pour vérifier les quotas)                         │
│  └─ ... autres repositories                                            │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ data operations
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│           COUCHE 7 : BASE DE DONNÉES (PostgreSQL + Flyway)             │
│  ├─ mp_operation_definitions (opérations canoniques)                   │
│  ├─ mp_quota_key_definitions (quotas)                                  │
│  ├─ mp_quota_entitlements (droits par plan)                            │
│  ├─ planiloisir_users                                                  │
│  └─ ... autres tables métier                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Flux de traitement d'une requête

### Exemple : POST /api/users/register/planiloisir

```
1️⃣ CLIENT envoie
   POST /api/users/register/planiloisir
   Headers:
     - X-Consent-Version: v1.0
     - X-Safety-Score: 0.95
   Body: { email: "user@example.com", interests: [...] }

2️⃣ FILTRES (Couche 2)
   ├─ ConsentGuard
   │  └─ Vérifie : X-Consent-Version présent ✅
   ├─ SafetyShield
   │  └─ Vérifie : X-Safety-Score (0.95) >= 0.80 ✅
   ├─ LatencyBudgetFilter
   │  └─ Démarre le chrono
   └─ TracingFilter
      └─ Génère/propage traceId=abc123, spanId=

3️⃣ CONTROLLER (Couche 1)
   RegisterUserController.register()
   → Appelle le service

4️⃣ ASPECT AOP (Couche 3)
   @NucleusOp(OP_PLANILOISIR_REGISTER)
   [NUCLEUS_OP START] span=planiLoisir/register tags={}
   → Génère spanId=12345

5️⃣ SERVICES NUCLEUS (Couche 4)
   NucleusCatalogueResolver.resolveOperation(OP_PLANILOISIR_REGISTER)
   → Cherche dans mp_operation_definitions
   
   PolicyEngineRuntimeService.checkAndConsume(
     namespace=PLANILOISIR,
     operationCode=OP_PLANILOISIR_REGISTER,
     accountId=123
   )
   → Vérifie les quotas dans mp_quota_entitlements

6️⃣ SERVICE MÉTIER (Couche 5)
   PlaniLoisirService.registerUser(email, interests)
   → Valide les données
   → Cherche les activités matchantes
   → Crée la fiche utilisateur

7️⃣ REPOSITORIES (Couche 6)
   PlaniLoisirUserRepository.save(user)
   → Exécute : INSERT INTO planiloisir_users (...)

8️⃣ BASE DE DONNÉES (Couche 7)
   PostgreSQL
   ├─ INSERT planiloisir_users (email, ...)
   ├─ INSERT planiloisir_user_interests (user_id, interest_id)
   └─ UPDATE mp_quota_consumed (accountId=123, units=1)

9️⃣ RETOUR (Couches 6→5→4→3→2→1)
   ├─ Repository retourne user créé
   ├─ Service retourne Mono<User>
   ├─ PolicyEngine consomme 1 unit de quota
   ├─ Aspect AOP log
   │  [NUCLEUS_OP END] span=planiLoisir/register signal=onComplete
   ├─ LatencyBudgetFilter ajoute header (si dépassement)
   │  X-Latency-Overbudget: true
   └─ Response HTTP 201 CREATED

🔟 CLIENT reçoit
   201 CREATED
   Headers:
     - traceId: abc123
     - X-Latency-Overbudget: (absent si < budget)
   Body: { id: 999, email: "user@example.com", ... }
```

---

## 📦 Détail de chaque couche

### **Couche 1 : CONTRÔLEURS (Présentation)**

**Responsabilités :**
- ✅ Exposer les endpoints HTTP
- ✅ Parser les paramètres/body
- ✅ Appeler le service métier
- ✅ Formater la réponse HTTP

**Annotations clés :**
```kotlin
@RestController
@RequestMapping("/api/users")
class RegisterUserController(
    private val planiLoisirService: PlaniLoisirService
) {
    @PostMapping("/register/{project}")
    @NucleusOp(
        op = Operation.PLANILOISIR_REGISTER,
        extraTags = ["project" to "planiloisir"]
    )
    fun register(
        @PathVariable project: String,
        @RequestHeader(name = "X-Consent-Version", required = false) consent: String?,
        @RequestBody request: RegisterRequest
    ): Mono<ResponseEntity<UserResponse>> {
        // Déléguer au service (Couche 5)
        return planiLoisirService.registerUser(request)
            .map { ResponseEntity.created(...).body(it) }
    }
}
```

**Fichiers :**
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/api/controller/RegisterUserController.kt`
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/web/...` (autres contrôleurs)

---

### **Couche 2 : FILTRES & MIDDLEWARES (WebFlux)**

**Responsabilités :**
- ✅ Valider les headers NUCLEUS (Consent, Safety, Latency)
- ✅ Appliquer les règles de sécurité transversales
- ✅ Mesurer la latence
- ✅ Propager le contexte (MDC, traceId)

**Architecture des filtres :**
```kotlin
@Component
class ConsentGuard(val config: NucleusProperties) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        
        // Vérifier si c'est une write (POST/PUT/PATCH/DELETE)
        val isWrite = request.method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)
        
        if (isWrite && !request.headers.containsKey(NucleusHeader.CONSENT_VERSION.headerName)) {
            // Si dans whitelist, passer
            if (config.whitelistPaths.any { path -> 
                exchange.request.path.value().matches(path.toRegex()) 
            }) {
                return chain.filter(exchange)
            }
            // Sinon bloquer
            return Mono.fromRunnable {
                exchange.response.statusCode = HttpStatus.BAD_REQUEST
                exchange.response.headers["X-Nucleus-Error"] = "PRINCIPLE_VIOLATION"
            }
        }
        
        return chain.filter(exchange)
    }
}

@Component
class SafetyShield(val config: NucleusProperties) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val safetyScore = exchange.request.headers
            .getFirst(NucleusHeader.SAFETY_SCORE.headerName)
            ?.toDoubleOrNull() ?: 1.0
        
        if (safetyScore < config.safety.minScore) {
            return Mono.error(SafetyBlockedException("Score: $safetyScore < ${config.safety.minScore}"))
        }
        
        return chain.filter(exchange)
    }
}

@Component
class LatencyBudgetFilter(val config: NucleusProperties) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()
        
        return chain.filter(exchange).doFinally { _ ->
            val elapsed = System.currentTimeMillis() - startTime
            
            if (elapsed > config.latency.budgetMs) {
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

**Fichiers :**
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/guard/ConsentGuard.kt`
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/guard/SafetyShield.kt`
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/filter/LatencyBudgetFilter.kt`

---

### **Couche 3 : ASPECT AOP (@NucleusOp)**

**Responsabilités :**
- ✅ Instrumentaliser les méthodes Reactor
- ✅ Ajouter des logs visuels
- ✅ Créer les spans OpenTelemetry
- ✅ Enregistrer les métriques Micrometer
- ✅ Propager le MDC (traceId, spanId)

**Implémentation :**
```kotlin
@Aspect
@Component
class NucleusOpAspect(
    private val meterRegistry: MeterRegistry,
    private val tracer: Tracer
) {
    @Around("@annotation(nucleusOp)")
    fun instrument(pjp: ProceedingJoinPoint, nucleusOp: NucleusOp): Any? {
        val operation = nucleusOp.op
        val spanName = "${operation.module}/${operation.action}"
        val tags = nucleusOp.extraTags
        
        logger.info("[NUCLEUS_OP START] span=$spanName tags={${tags.joinToString(", ")}}")
        
        val startTime = System.nanoTime()
        
        // Créer un span OpenTelemetry
        tracer.spanBuilder(spanName)
            .setAllAttributes(tags.associate { (k, v) -> k to v })
            .startSpan()
            .use { span ->
                try {
                    val result = pjp.proceed()
                    
                    // Si Mono/Flux, ajouter un callback
                    if (result is Mono<*>) {
                        return (result as Mono<Any?>)
                            .doOnSuccess { _ ->
                                recordMetrics(operation, startTime, tags)
                                logger.info("[NUCLEUS_OP END] span=$spanName signal=onComplete")
                            }
                            .doOnError { _ ->
                                recordMetrics(operation, startTime, tags)
                                logger.error("[NUCLEUS_OP ERROR] span=$spanName")
                            }
                    }
                    
                    return result
                } catch (ex: Exception) {
                    logger.error("[NUCLEUS_OP EXCEPTION] span=$spanName", ex)
                    throw ex
                }
            }
    }
    
    private fun recordMetrics(op: Operation, startTime: Long, tags: Map<String, String>) {
        val durationMs = (System.nanoTime() - startTime) / 1_000_000.0
        
        Timer.builder("op.${op.module}.${op.action}")
            .publishPercentiles(0.5, 0.95, 0.99)
            .tag("module", op.module)
            .tag("op", op.action)
            .tags(tags)
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS)
    }
}
```

**Annotation :**
```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NucleusOp(
    val op: Operation,
    val extraTags: Array<Pair<String, String>> = []
)

enum class Operation(val module: String, val action: String) {
    PLANILOISIR_REGISTER("planiLoisir", "register"),
    PLANILOISIR_FIND("planiLoisir", "find"),
    PLANIDECOUVERTE_AI_POST("planiDécouverte", "aiSuggestions"),
    // ...
}
```

**Fichiers :**
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/aspect/NucleusOpAspect.kt`
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/Operation.kt`

---

### **Couche 4 : SERVICES NUCLEUS**

**Responsabilités :**
- ✅ Résoudre les opérations/quotas (catalogue)
- ✅ Valider les capabilities
- ✅ Vérifier et consommer les quotas

**Services clés :**

#### A. NucleusCatalogueResolver
```kotlin
@Service
class NucleusCatalogueResolver(
    private val catalogueRuntimeService: CatalogueRuntimeService
) {
    fun resolveOperation(operationCode: OperationCode): Mono<OperationDefinitionDom> {
        return catalogueRuntimeService.getOperation(operationCode)
    }
    
    fun resolveQuotaCosts(planCode: String, operationCode: OperationCode): Mono<List<OperationQuotaCostDom>> {
        return catalogueRuntimeService.getQuotaCosts(planCode, operationCode)
    }
    
    fun resolveCapabilities(planCode: String, operationCode: OperationCode): Mono<OperationCapabilities> {
        return catalogueRuntimeService.getCapabilities(planCode, operationCode)
    }
}
```

#### B. PolicyEngineRuntimeService (Typé)
```kotlin
@Service
class PolicyEngineRuntimeService(
    private val quotaRepository: QuotaRepository
) {
    // ✅ PRÉFÉRÉ : Signature typée (enums)
    fun checkAndConsume(
        namespace: OperationNamespace,
        operationCode: OperationCode,
        accountId: Long,
        project: String? = null,
        groupId: Long? = null,
        units: Int = 1
    ): Mono<String> {
        return quotaRepository.findByAccountId(accountId)
            .flatMap { quotas ->
                val opDefinition = catalogueRuntimeService.getOperation(operationCode)
                opDefinition.flatMap { op ->
                    if (op.allowed && quotas.canConsume(operationCode, units)) {
                        quotas.consume(operationCode, units)
                            .map { "ALLOWED" }
                    } else {
                        Mono.just("BLOCKED: quota exceeded")
                    }
                }
            }
    }
}
```

**Fichiers :**
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/resolver/NucleusCatalogueResolver.kt`
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/policy/PolicyEngineRuntimeService.kt`

---

### **Couche 5 : SERVICES MÉTIER**

**Responsabilités :**
- ✅ Logique métier (enregistrement, recherche, etc.)
- ✅ Appeler les repositories
- ✅ Valider les données
- ✅ Orchestrer les opérations

**Exemple :**
```kotlin
@Service
class PlaniLoisirService(
    private val userRepository: PlaniLoisirUserRepository,
    private val interestRepository: PlaniLoisirInterestRepository,
    private val policyEngine: PolicyEngineRuntimeService
) {
    @NucleusOp(
        op = Operation.PLANILOISIR_REGISTER,
        extraTags = ["project" to "planiloisir"]
    )
    fun registerUser(request: RegisterRequest, accountId: Long): Mono<UserResponse> {
        // 1️⃣ Valider les données
        if (request.email.isBlank()) {
            return Mono.error(ValidationException("Email cannot be blank"))
        }
        
        // 2️⃣ Vérifier le quota (Couche 4)
        return policyEngine.checkAndConsume(
            namespace = OperationNamespace.PLANILOISIR,
            operationCode = OperationCode.PLANILOISIR__PLANNING_CREATE,
            accountId = accountId
        ).flatMap { result ->
            if (!result.contains("ALLOWED")) {
                return@flatMap Mono.error(QuotaExceededException("Cannot create planning"))
            }
            
            // 3️⃣ Chercher les activités matchantes
            interestRepository.findByKeywords(request.interests)
        }.flatMap { interests ->
            // 4️⃣ Créer l'utilisateur (Couche 6)
            userRepository.save(PlaniLoisirUser(
                email = request.email,
                interests = interests,
                createdAt = LocalDateTime.now()
            ))
        }.map { user ->
            // 5️⃣ Mapper au DTO de réponse
            UserResponse(
                id = user.id!!,
                email = user.email,
                interestCount = user.interests.size
            )
        }
    }
}
```

**Fichiers :**
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/modules/planiloisir/service/PlaniLoisirService.kt`
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/modules/planidecouverte/service/PlaniDécouverteService.kt`

---

### **Couche 6 : REPOSITORIES**

**Responsabilités :**
- ✅ Opérations CRUD réactives (R2DBC)
- ✅ Requêtes SQL personnalisées
- ✅ Mappage BD ↔ Domaine

**Exemple :**
```kotlin
interface PlaniLoisirUserRepository : R2dbcRepository<PlaniLoisirUser, Long> {
    @Query("""
        SELECT * FROM planiloisir_users 
        WHERE email = :email
    """)
    fun findByEmail(email: String): Mono<PlaniLoisirUser>
    
    @Query("""
        SELECT u.* FROM planiloisir_users u
        JOIN planiloisir_user_interests ui ON u.id = ui.user_id
        WHERE ui.interest_id IN (:ids)
        GROUP BY u.id
    """)
    fun findByInterests(ids: List<Long>): Flux<PlaniLoisirUser>
}

interface QuotaRepository : R2dbcRepository<QuotaEntitlement, Long> {
    @Query("""
        SELECT * FROM mp_quota_entitlements 
        WHERE account_id = :accountId 
        AND operation_code = :opCode
    """)
    fun findByAccountAndOperation(accountId: Long, opCode: String): Mono<QuotaEntitlement>
}
```

**Fichiers :**
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/modules/planiloisir/repository/PlaniLoisirUserRepository.kt`
- `src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/repository/QuotaRepository.kt`

---

### **Couche 7 : BASE DE DONNÉES**

**Schéma NUCLEUS :**
```sql
-- Opérations canoniques
CREATE TABLE mp_operation_definitions (
    id BIGSERIAL PRIMARY KEY,
    operation_code VARCHAR(255) NOT NULL UNIQUE,  -- ex: PLANILOISIR__PLANNING_CREATE
    namespace VARCHAR(100) NOT NULL,              -- ex: PLANILOISIR
    group_code VARCHAR(100),                      -- ex: PL_PLANNING
    description TEXT,
    is_billable BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Quotas
CREATE TABLE mp_quota_key_definitions (
    id BIGSERIAL PRIMARY KEY,
    quota_key VARCHAR(255) NOT NULL UNIQUE,      -- ex: RES_AI_SUGGESTIONS
    category VARCHAR(100),                       -- ex: PROVIDER
    scope VARCHAR(50),                           -- USER, GROUP, PLANNING, ACCOUNT
    default_limit INT,
    period VARCHAR(50),                          -- DAILY, MONTHLY, YEARLY
    created_at TIMESTAMP DEFAULT NOW()
);

-- Droits d'accès
CREATE TABLE mp_quota_entitlements (
    id BIGSERIAL PRIMARY KEY,
    plan_code VARCHAR(255),                      -- ex: PLANIDECOUVERTE_ACCESS
    operation_code VARCHAR(255),
    quota_key VARCHAR(255),
    quota_limit INT,
    scope VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    
    FOREIGN KEY (operation_code) REFERENCES mp_operation_definitions(operation_code),
    FOREIGN KEY (quota_key) REFERENCES mp_quota_key_definitions(quota_key)
);

-- Consommation
CREATE TABLE mp_quota_consumed (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    operation_code VARCHAR(255) NOT NULL,
    units_consumed INT DEFAULT 1,
    period_start DATE,
    period_end DATE,
    consumed_at TIMESTAMP DEFAULT NOW()
);
```

**Migrations Flyway :**
- `V5__seed_platform_operation_definitions.sql` — 121 opérations
- `V10_97__seed_planiloisir_quota_key_definitions.sql` — Quotas
- `V11__seed_planiloisir_quota_entitlements.sql` — Droits
- ... autres migrations métier

---

## 🔄 Patterns de communication entre couches

### Pattern 1 : Controller → Service Métier

```kotlin
// Couche 1 (Controller)
@PostMapping("/register")
fun register(@RequestBody req: RegisterRequest): Mono<ResponseEntity<User>> {
    return service.registerUser(req)  // ← Couche 5
        .map { ResponseEntity.created(...).body(it) }
}

// Couche 5 (Service)
fun registerUser(req: RegisterRequest): Mono<User> {
    return userRepository.save(mapToEntity(req))  // ← Couche 6
        .map { mapToResponse(it) }
}
```

### Pattern 2 : Service Métier → Service Nucleus

```kotlin
// Couche 5 (Service Métier)
@NucleusOp(op = Operation.PLANILOISIR_REGISTER)
fun registerUser(req: RegisterRequest, accountId: Long): Mono<User> {
    // 1️⃣ Appeler PolicyEngine (Couche 4)
    return policyEngine.checkAndConsume(
        namespace = OperationNamespace.PLANILOISIR,
        operationCode = OperationCode.PLANILOISIR__PLANNING_CREATE,
        accountId = accountId
    ).flatMap { _ ->
        // 2️⃣ Si autorisé, appeler repository (Couche 6)
        userRepository.save(mapToEntity(req))
    }
}

// Couche 4 (PolicyEngine)
fun checkAndConsume(...): Mono<String> {
    // Appeler CatalogueResolver pour résoudre
    return catalogueResolver.resolveCapabilities(...)
        .flatMap { caps ->
            if (!caps.allowed) Mono.error(...)
            else quotaRepository.consume(...)
        }
}
```

### Pattern 3 : Filters → Handler

```kotlin
// Couche 2 (Filter)
class ConsentGuard : WebFilter {
    fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (!validateConsent(exchange)) {
            return sendError(exchange, 400)  // ← Couche 2 s'arrête
        }
        return chain.filter(exchange)  // ← Passer à la suite (Couche 3 puis 1)
    }
}
```

---

## 📊 Tableau de synthèse

| Couche | Composant | Pattern | Exemple |
|--------|-----------|---------|---------|
| 1 | Controller | @RestController | RegisterUserController |
| 2 | Filter | implements WebFilter | ConsentGuard, SafetyShield |
| 3 | Aspect | @Aspect @Around | NucleusOpAspect |
| 4 | Service Nucleus | @Service (Catalogue) | NucleusCatalogueResolver |
| 5 | Service Métier | @Service @NucleusOp | PlaniLoisirService |
| 6 | Repository | R2dbcRepository | PlaniLoisirUserRepository |
| 7 | BD | SQL Tables | mp_operation_definitions |

---

## ✅ Checklist d'implémentation

Quand tu ajoutes une nouvelle fonctionnalité :

- [ ] **Couche 7 :** Créer les tables/seeds Flyway
- [ ] **Couche 6 :** Créer les repositories R2DBC
- [ ] **Couche 5 :** Créer le service métier avec @NucleusOp
- [ ] **Couche 4 :** Enregistrer l'opération dans les enums Nucleus
- [ ] **Couche 3 :** L'aspect AOP s'applique automatiquement
- [ ] **Couche 2 :** Vérifier que les guards s'appliquent
- [ ] **Couche 1 :** Créer le controller @RestController
- [ ] **Tests :** Couvrir les 7 couches (IT + UT)

---

**Dernière mise à jour :** 2026-04-13

