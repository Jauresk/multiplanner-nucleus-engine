# 🧬 NUCLEUS — Guide Complet d'Exploitation

## 📋 Vue d'ensemble

**NUCLEUS** est un framework transversal pour instrumentaliser et contrôler les opérations de MultiPlanner. Il repose sur **7 principes** de gouvernance (Vie, Protection, Action, etc.) implémentés par des **guards et filtres WebFlux**.

---

## 🏗️ Architecture générale de NUCLEUS

```
┌────────────────────────────────────────────────────────────────┐
│                    COUCHE PRÉSENTATION                          │
│  Controllers (@RestController) + @NucleusOp annotations        │
└─────────────────────────────────┬────────────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────────┐
        │                         │                             │
        ▼                         ▼                             ▼
┌──────────────────┐  ┌──────────────────────┐  ┌──────────────────┐
│ NUCLEUS FILTERS  │  │ NUCLEUS CATALOGUE    │  │ NUCLEUS GUARDS   │
│ (WebFlux)        │  │ Resolver             │  │ (Aspect AOP)     │
│                  │  │                      │  │                  │
│ • Consent        │  │ • OperationCode      │  │ • ConsentGuard   │
│ • Safety Shield  │  │ • QuotaKey           │  │ • SafetyShield   │
│ • LatencyBudget  │  │ • Capabilities       │  │ • LatencyBudget  │
│ • Tracing        │  │                      │  │                  │
└──────────────────┘  └──────────────────────┘  └──────────────────┘
        │                         │                             │
        └─────────────────────────┼─────────────────────────────┘
                                  │
                                  ▼
        ┌─────────────────────────────────────────────────────┐
        │         COUCHE MÉTIER (Services)                    │
        │  • PlaniLoisirService                               │
        │  • PlaniDécouverteService                           │
        │  • PolicyEngineRuntimeService (typé)                │
        └─────────────────────────────────────────────────────┘
                                  │
                                  ▼
        ┌─────────────────────────────────────────────────────┐
        │  COUCHE PERSISTENCE (Repositories + BD)             │
        │  • mp_operation_definitions (opérations)            │
        │  • mp_quota_key_definitions (quotas)                │
        │  • mp_quota_entitlements (droits d'accès)           │
        └─────────────────────────────────────────────────────┘
```

---

## 🎯 Les 7 Principes NUCLEUS

| Principe | Description | Implémentation | Header |
|----------|------------|-----------------|--------|
| **LIFE** | Droit à la vie des données | ConsentGuard | `X-Consent-Version` |
| **PROTECTION** | Sécurité/confidentialité | SafetyShield | `X-Safety-Score` |
| **ACTION** | Budget de latence (perf) | LatencyBudgetFilter | `X-Latency-Overbudget` |
| **EMOTION** | Tonalité/contenu (optionnel) | SafetyShield | Configurable |
| **LOVE** | Données affectives (optionnel) | SafetyShield | Configurable |
| **INTELLECT** | Validation schéma/validité | SchemaValidationFilter | Implicite |
| **IMAGINATION** | Diversité/nouveauté (optionnel) | PolicyEngine | Implicite |

---

## 📦 Composants clés

### 1. **NucleusProbeController** — Sandbox de test

Ce contrôleur démontre comment Nucleus réagit selon les principes :

| Endpoint | Méthode | Principes | Objectif |
|----------|---------|-----------|----------|
| `/nucleus/echo` | GET | EMOTION, LOVE | Ping simple + démo Safety |
| `/nucleus/users` | POST | LIFE, PROTECTION | Probe d'écriture (consentement obligatoire) |
| `/nucleus/delay?ms=…` | GET | ACTION | Simuler la latence (Mono.delay) |
| `/nucleus/explain?cause=…` | GET | INTELLECT | Explicabilité sur les blocages |
| `/nucleus/ideas` | GET | IMAGINATION, ACTION | Retourner 2 idées mock (novelty) |
| `/nucleus/safety/probe` | GET | PROTECTION, LIFE | Écho du header sécurité |

**Exemple de réponses :**
```bash
# 1) Consent OK → 201 CREATED
curl -X POST http://localhost:8080/nucleus/users \
  -H 'Content-Type: application/json' \
  -H 'X-Consent-Version: v1.0' \
  -d '{"email":"a@b.c"}'
→ 201 {"email": "a@b.c"}

# 2) Consent manquant → 400 BAD_REQUEST
curl -X POST http://localhost:8080/nucleus/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@b.c"}'
→ 400 {"error":"PRINCIPLE_VIOLATION","msg":"X-Consent-Version missing"}

# 3) Latence > budget → header overbudget
curl http://localhost:8080/nucleus/delay?ms=250
→ 200 {"sleptMs":250,"done":true}
→ Headers: X-Latency-Overbudget: true (si budget < 250ms)
```

---

### 2. **NucleusHeader** — Enum centralisé

```kotlin
enum class NucleusHeader(val headerName: String) {
    CONSENT_VERSION("X-Consent-Version"),
    SAFETY_SCORE("X-Safety-Score"),
    LATENCY_OVERBUDGET("X-Latency-Overbudget")
}
```

**Utilisation :**
```kotlin
// Dans un contrôleur
@GetMapping("/test")
fun test(
    @RequestHeader(name = NucleusHeader.CONSENT_VERSION.headerName, required = false) 
    consent: String?
) = mapOf("consent" to consent)

// Dans un filtre
val score = exchange.request.headers
    .getFirst(NucleusHeader.SAFETY_SCORE.headerName)
    ?.toDoubleOrNull()
```

---

### 3. **@NucleusOp** — Annotation pour instrumentalisation

```kotlin
@Service
class MyService {
    @NucleusOp(
        op = Operation.PLANILOISIR_REGISTER,
        extraTags = ["endpoint" to "register"]
    )
    fun registerUser(email: String): Mono<User> {
        // Logs automatiques : [NUCLEUS_OP START] / [NUCLEUS_OP END]
        // Métriques Micrometer : op.planiLoisir.register (COUNT, TOTAL_TIME, MAX)
        // Traces OpenTelemetry : span exporté à Jaeger/Tempo
        return userRepository.save(email)
    }
}
```

**Ce que fait l'aspect :**
- ✅ Logs visuels `[NUCLEUS_OP START]` / `[NUCLEUS_OP END]`
- ✅ Tracing MDC : `traceId` et `spanId` propagés partout
- ✅ Métriques Micrometer : timers custom `op.<module>.<action>`
- ✅ Spans OpenTelemetry exportables à Jaeger/Tempo

---

### 4. **Catalogue NUCLEUS** — Registre typé

#### A. Enums canoniques

```kotlin
// OperationCode.kt
enum class OperationCode {
    PLANILOISIR__PLANNING_CREATE,
    PLANILOISIR__PLANNING_READ,
    PLANILOISIR__INTERESTS_WRITE,
    PLANIDECOUVERTE__AI_SUGGESTIONS_POST,
    PLANIDECOUVERTE__METEO_GET,
    // ... (121 opérations au total)
}

// OperationNamespace.kt
enum class OperationNamespace {
    PLANILOISIR,
    PLANIDECOUVERTE,
    PLANIVENTE,
    PLANICOURSE,
    ADMIN,
    // ...
}

// QuotaKey.kt
enum class QuotaKey {
    RES_AI_SUGGESTIONS,
    RES_API_CALLS_DAILY,
    MAPS_GEO_INTENSIVE,
    ROUTING_OPTIMIZE,
    VOICE_STT_MINUTES,
    // ... (25+ clés)
}

// QuotaScope.kt
enum class QuotaScope {
    USER,
    GROUP,
    PLANNING,
    ACCOUNT
}
```

#### B. Tables BD synchronisées (Flyway Seeds)

| Table | Rôle | Seed Flyway |
|-------|------|-------------|
| `mp_operation_definitions` | Définitions d'opérations | `V5__seed_platform_operation_definitions.sql` |
| `mp_quota_key_definitions` | Définitions des quotas | `V10_97__seed_planiloisir_quota_key_definitions.sql` |
| `mp_quota_entitlements` | Droits d'accès par plan | `V11__seed_planiloisir_quota_entitlements.sql` |

---

### 5. **Services Nucleus typés**

#### A. NucleusCatalogueResolver

```kotlin
@Service
class NucleusCatalogueResolver(
    private val catalogueRuntimeService: CatalogueRuntimeService
) {
    // Résoudre une opération par son code
    fun resolveOperation(operationCode: OperationCode): Mono<OperationDefinitionDom>
    
    // Résoudre les quotas associés à une opération
    fun resolveQuotaCosts(planCode: String, operationCode: OperationCode): Mono<List<OperationQuotaCostDom>>
    
    // Résoudre les capabilities complètes
    fun resolveCapabilities(planCode: String, operationCode: OperationCode): Mono<OperationCapabilities>
}
```

#### B. PolicyEngineRuntimeService (version typée)

```kotlin
@Service
class PolicyEngineRuntimeService {
    // ✅ PRÉFÉRÉ : signature typée (enums)
    fun checkAndConsume(
        namespace: OperationNamespace,
        operationCode: OperationCode,
        accountId: Long,
        project: String? = null,
        groupId: Long? = null,
        units: Int = 1
    ): Mono<String>
    
    // ⚠️ DEPRECATED : signature String (à éviter)
    @Deprecated("Utiliser la version typée")
    fun checkAndConsume(namespace: String, operationCode: String, ...): Mono<String>
}
```

#### C. NucleusCatalogueInitValidator

Au démarrage, valide que les enums sont **synchronisés avec la BD** :

```kotlin
@Component
class NucleusCatalogueInitValidator {
    @PostConstruct
    fun validateOnStartup() {
        // Compare OperationCode.entries ↔ mp_operation_definitions
        // Compare QuotaKey.entries ↔ mp_quota_key_definitions
        // Si drift : LOG WARNING ou FAIL selon configuration
    }
}
```

**Configuration :**
```yaml
multiplanner:
  nucleus:
    validation:
      enabled: true
      fail-on-drift: false   # DEV : log warning
      # fail-on-drift: true  # PROD : fail-fast
```

---

## 🔐 Règles OBLIGATOIRES (Prompts Copilot)

### ✅ Règle 1 : Jamais de Strings magiques

```kotlin
// ❌ INTERDIT
policyEngine.checkAndConsume("PLANILOISIR", "PLANNING_CREATE", accountId)

// ✅ OBLIGATOIRE
policyEngine.checkAndConsume(
    namespace = OperationNamespace.PLANILOISIR,
    operationCode = OperationCode.PLANILOISIR__PLANNING_CREATE,
    accountId = accountId
)
```

### ✅ Règle 2 : Ajout synchronisé Code + BD

Quand tu ajoutes une **nouvelle opération** :
1. Ajouter dans `OperationCode.kt`
2. Ajouter dans le seed Flyway `mp_operation_definitions`

Quand tu ajoutes un **nouveau quota** :
1. Ajouter dans `QuotaKey.kt`
2. Ajouter dans le seed Flyway `mp_quota_key_definitions`
3. Ajouter les entitlements dans `mp_quota_entitlements`

### ✅ Règle 3 : Validation au boot

Le `NucleusCatalogueInitValidator` compare automatiquement :
- `OperationCode.entries` ↔ `mp_operation_definitions.operation_code`
- `QuotaKey.entries` ↔ `mp_quota_key_definitions.quota_key`

Si désalignement :
- **DEV** : Log WARNING + continue
- **PROD** : `fail-on-drift=true` → startup bloqué

### ✅ Règle 4 : Signatures Nucleus typées

Toutes les méthodes Nucleus doivent prendre :
- `operationCode: OperationCode`
- `namespace: OperationNamespace`
- `quotaKey: QuotaKey` (si applicable)

**Jamais de String brut.**

---

## 🎛️ Configuration NUCLEUS

### application.yml (exemple complet)

```yaml
# ========== Nucleus Principles ==========
nucleus7:
  consent:
    enabled: true
    headerName: X-Consent-Version
    requiredOnWrite: true                # POST/PUT/PATCH/DELETE
    whitelistPaths:                      # Chemins exempt de consentement
      - /actuator/**
      - /auth/**
      - /health/**

  safety:
    enabled: true
    minScore: 0.80                       # 0..1 scale
    headerName: X-Safety-Score

  latency:
    enabled: true
    budgetMs: 250                        # Alerte si > 250ms
    hardBlock: false                     # true = refuser la réponse

# ========== Nucleus Catalogue & Validation ==========
multiplanner:
  nucleus:
    validation:
      enabled: true
      fail-on-drift: false               # false (DEV) / true (PROD)

# ========== Observabilité ==========
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true        # p50, p95, p99
  
  tracing:
    enabled: true
    sampling:
      probability: 1.0                   # 100% en DEV

  # (Optionnel) Exporter à Jaeger/Tempo
  # otlp:
  #   tracing:
  #     endpoint: http://tempo:4318

# ========== Logs NUCLEUS ==========
logging:
  level:
    NucleusOp: DEBUG                     # Voir [NUCLEUS_OP START/END]
    com.inokey.solution.dnk.multiplanner.core.nucleus: DEBUG
  
  pattern:
    console: "%d{HH:mm:ss.SSS} %5p [traceId=%X{traceId:-NOTRACE} spanId=%X{spanId:-NOTRACE}] %logger{36} - %msg%n"
```

---

## 📊 Observabilité et Monitoring

### 1. Logs visuels (Niveau 1 — Le plus rapide)

```
2026-04-13 10:23:45.123  INFO [traceId=a1b2c3d4 spanId=12345]
  [NUCLEUS_OP START] span=nucleus/echo tags={}
2026-04-13 10:23:45.234  INFO [traceId=a1b2c3d4 spanId=12345]
  [NUCLEUS_OP END]   span=nucleus/echo signal=onComplete
```

### 2. Métriques Micrometer (Niveau 3)

```bash
# Lister toutes les metrics Nucleus
curl http://localhost:8080/actuator/metrics | jq '.names | map(select(startswith("op.")))'

# Détail d'une metric spécifique
curl http://localhost:8080/actuator/metrics/op.nucleus.echo | jq

# Exemple de réponse :
{
  "name": "op.nucleus.echo",
  "description": null,
  "baseUnit": "seconds",
  "measurements": [
    {"statistic": "COUNT", "value": 5},
    {"statistic": "TOTAL_TIME", "value": 0.042},
    {"statistic": "MAX", "value": 0.015}
  ],
  "availableTags": [
    {"tag": "module", "values": ["nucleus"]},
    {"tag": "op", "values": ["echo"]},
    {"tag": "endpoint", "values": ["probe"]}
  ]
}
```

### 3. Traces OpenTelemetry (Niveau 4 — Avancé)

Avec Jaeger/Tempo activé :

```yaml
management:
  otlp:
    tracing:
      endpoint: http://tempo:4318
```

Puis naviguer dans Jaeger UI → chercher service `multiplanner` → voir les spans en arborescence.

### 4. Health Indicator Nucleus

```bash
curl http://localhost:8080/actuator/health/nucleusCatalogue

# Réponse si aligné
{
  "status": "UP",
  "details": {
    "status": "ALIGNED",
    "message": "Nucleus catalogue is in sync"
  }
}

# Réponse si drift détecté
{
  "status": "DOWN",
  "details": {
    "status": "DRIFT_DETECTED",
    "operationsMissingInDb": ["NEW_OP_1", "NEW_OP_2"],
    "operationsMissingInDbCount": 2,
    "message": "Enum ↔ DB drift detected! Run Flyway seeds or update enums."
  }
}
```

---

## 🧪 Tests

### Tests rapides (< 2 min)

```bash
# 1) Lancer l'app
./gradlew bootRun

# 2) Appeler un endpoint
curl http://localhost:8080/nucleus/echo

# 3) Vérifier les logs
grep NUCLEUS_OP logs.txt
```

### Tests automatisés

```bash
./gradlew test --tests NucleusOpObservabilityIT -i

# Valide :
# ✅ Les endpoints répondent correctement
# ✅ Les headers de signature sont présents
# ✅ L'enum `Operation` a les bons noms
```

### Tests d'intégration complets

```kotlin
@Test
fun `POST avec consent = 201 CREATED`() {
    client.post().uri("/nucleus/users")
        .header(NucleusHeader.CONSENT_VERSION.headerName, "v1.0")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""{"email":"a@b.c"}""")
        .exchange()
        .expectStatus().isCreated
}

@Test
fun `Requête sans consent = 400 PRINCIPLE_VIOLATION`() {
    client.post().uri("/nucleus/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""{"email":"a@b.c"}""")
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$.error").isEqualTo("PRINCIPLE_VIOLATION")
}

@Test
fun `GET avec latence > budget = header overbudget`() {
    client.get().uri("/nucleus/delay?ms=500")
        .exchange()
        .expectStatus().isOk
        .expectHeader().exists(NucleusHeader.LATENCY_OVERBUDGET.headerName)
}
```

---

## 📈 Exemple : Plan PlaniDécouverte (Modèle ACCESS)

PlaniDécouverte utilise un **modèle d'affaires ACCESS unique** :

| Ressource | Coût | Quota | Effet |
|-----------|------|-------|-------|
| Interne (Postgres, algo maison) | ~0€ | - | ✅ GRATUIT |
| Externe (LLM, météo, trafic) | €€€ | Oui | 💰 QUOTA-GATED |

**Opérations définies :**

```kotlin
enum class OperationCode {
    // Gratuites (internes)
    PLANIDECOUVERTE__DISCOVERY_BASIC_SEARCH,
    PLANIDECOUVERTE__DISCOVERY_HISTORY_READ,
    PLANIDECOUVERTE__DISCOVERY_HISTORY_CLEAR,
    
    // Protection interne
    PLANIDECOUVERTE__DISCOVERY_CONTEXTUAL,        // QuotaKey.RES_API_CALLS_DAILY
    
    // Payantes (externes)
    PLANIDECOUVERTE__AI_SUGGESTIONS_POST,         // QuotaKey.RES_AI_SUGGESTIONS
    PLANIDECOUVERTE__METEO_GET,                   // QuotaKey.MAPS_GEO_INTENSIVE
    PLANIDECOUVERTE__TRAFIC_GET,                  // QuotaKey.ROUTING_OPTIMIZE
    PLANIDECOUVERTE__DISCOVERY_VOICE,             // DENY (désactivé) + quota=0
}
```

**Quotas ACCESS :**

| QuotaKey | Limite | Période | Billing |
|----------|--------|---------|---------|
| `RES_API_CALLS_DAILY` | 500 | DAILY | INTERNAL_PROTECTION |
| `RES_AI_SUGGESTIONS` | 50 | MONTHLY | MONETIZED |
| `MAPS_GEO_INTENSIVE` | 100 | MONTHLY | MONETIZED |
| `ROUTING_OPTIMIZE` | 50 | MONTHLY | MONETIZED |
| `VOICE_STT_MINUTES` | 0 | MONTHLY | MONETIZED (désactivé) |

**Exemple de code (Kotlin) :**

```kotlin
class DecouverteIAService(
    private val policyEngine: PolicyEngineRuntimeService
) {
    companion object {
        private val NAMESPACE = OperationNamespace.PLANIDECOUVERTE
        private val OP_AI = OperationCode.PLANIDECOUVERTE__AI_SUGGESTIONS_POST
    }
    
    @NucleusOp(op = Operation.PLANIDECOUVERTE_AI_SUGGESTIONS, extraTags = ["type" to "ia"])
    fun generateSuggestions(accountId: Long, estimatedCost: Int): Mono<List<String>> {
        // Vérifier le quota et consommer
        return policyEngine.checkAndConsume(
            namespace = NAMESPACE,
            operationCode = OP_AI,
            accountId = accountId,
            project = NAMESPACE.name,
            units = estimatedCost
        ).flatMap { result ->
            if (result.contains("ALLOWED")) {
                generateAISuggestions()
            } else {
                Mono.error(QuotaExceededException("RES_AI_SUGGESTIONS"))
            }
        }
    }
}
```

---

## ✅ Checklist de vérification

- [ ] Configuration NUCLEUS dans `application.yml`
- [ ] En-têtes standards documentés dans OpenAPI
- [ ] Tests d'intégration couvrent 400/403/overbudget
- [ ] Dashboards Prometheus/Grafana sur `nucleus7.*` et p95
- [ ] CI : `./gradlew test` passe sans erreur
- [ ] Health check `nucleusCatalogue` activé
- [ ] Enums synchronisés avec BD (validation au boot)
- [ ] Logs `[NUCLEUS_OP START/END]` visibles en DEV
- [ ] Métriques `op.*` disponibles dans Actuator

---

## 🔗 Fichiers de référence

| Fichier | Rôle |
|---------|------|
| `OperationCode.kt` | Enum des 121 opérations |
| `OperationNamespace.kt` | Enum des namespaces |
| `OperationGroup.kt` | Enum des groupes fonctionnels |
| `QuotaKey.kt` | Enum des 25+ quota keys |
| `QuotaScope.kt` | Enum des scopes |
| `NucleusHeader.kt` | Enum des headers |
| `NucleusProbeController.kt` | Contrôleur sandbox |
| `NucleusCatalogueResolver.kt` | Service de résolution |
| `NucleusCatalogueInitValidator.kt` | Validateur boot |
| `PolicyEngineRuntimeService.kt` | Moteur policy (typé) |
| `V5__seed_platform_operation_definitions.sql` | Seed opérations |
| `V10_97__seed_planiloisir_quota_key_definitions.sql` | Seed quotas |

---

## 🚀 Prochaines étapes

1. **Lancer et tester :**
   ```bash
   ./gradlew bootRun
   curl http://localhost:8080/nucleus/echo
   ```

2. **Vérifier les logs :**
   ```bash
   grep NUCLEUS_OP <logs>
   ```

3. **Ajouter d'autres opérations au catalogue**

4. **Exporter les traces à Jaeger** (optionnel pour PROD)

5. **Alertes sur les métriques** (ex: si `op.planiLoisir.register` > 500ms)

6. **Dashboard Prometheus/Grafana** pour visualiser les timers

---

## 📚 Documentation officielle

Voir les fichiers source dans `/docs/` :
- `README_NUCLEUS.md` — Vue générale et endpoints probe
- `NUCLEUS7_SETUP_GUIDE.md` — Configuration
- `NUCLEUS_OP_QUICK_START.md` — Démarrage rapide observabilité
- `NUCLEUS_OP_VERIFICATION.md` — Vérification 4 niveaux
- `NUCLEUS_CATALOGUE_CANON.md` — Registre canonique typé

---

**Dernière mise à jour :** 2026-04-13  
**Auteur :** GitHub Copilot + Documentation NUCLEUS MultiPlanner

