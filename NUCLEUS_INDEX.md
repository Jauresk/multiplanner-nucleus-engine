# 📚 INDEX NUCLEUS — Navigation complète

## 🎯 Documents créés

Vous avez demandé une documentation légère sur **NUCLEUS** et comment exploiter sa structure d'organisation dans les couches (contrôleurs, services, repositories, etc.).

### **3 documents ont été créés :**

---

## 1️⃣ **NUCLEUS_GUIDE_COMPLET.md** — Vue générale

**Contenu :**
- 🧬 Architecture générale de NUCLEUS
- 🎯 Les 7 principes (Vie, Protection, Action, etc.)
- 📦 Composants clés (ProbeController, NucleusHeader, @NucleusOp, Catalogue)
- 🔐 5 règles OBLIGATOIRES (Nucleus)
- 🎛️ Configuration (application.yml)
- 📊 Observabilité (logs, métriques, traces)
- 🧪 Tests (rapides et automatisés)
- 📈 Exemple PlaniDécouverte (modèle ACCESS)
- ✅ Checklist de vérification

**À lire en premier pour comprendre le concept**

---

## 2️⃣ **NUCLEUS_ARCHITECTURE_COUCHES.md** — En couches

**Contenu :**
- 📐 Vue en couches (7 niveaux)
- 🔄 Flux de traitement d'une requête (détaillé)
- 📦 Détail de chaque couche :
  - Couche 1 : CONTRÔLEURS (Présentation)
  - Couche 2 : FILTRES & MIDDLEWARES (WebFlux)
  - Couche 3 : ASPECT AOP (@NucleusOp)
  - Couche 4 : SERVICES NUCLEUS
  - Couche 5 : SERVICES MÉTIER
  - Couche 6 : REPOSITORIES
  - Couche 7 : BASE DE DONNÉES
- 🔄 Patterns de communication entre couches
- 📊 Tableau de synthèse
- ✅ Checklist d'implémentation

**À lire pour comprendre comment les couches s'articulent**

---

## 3️⃣ **NUCLEUS_BONNES_PRATIQUES.md** — Patterns & anti-patterns

**Contenu :**
- 🎯 Bonnes pratiques générales (5 principes)
- 🏭 Patterns par couche (A, B, C)
- ❌ Anti-patterns à éviter (5 erreurs communes)
- ✅ Checklist de qualité (avant merge, avant PROD)
- 🎯 Exemples concrets (3 cas d'usage)

**À lire pour éviter les pièges et appliquer les bonnes pratiques**

---

## 🗺️ Ordre de lecture recommandé

### Pour un nouveau développeur

```
1. NUCLEUS_GUIDE_COMPLET.md
   ├─ Lire : "Vue d'ensemble" + "Les 7 Principes"
   ├─ Lire : "Composants clés"
   └─ Essayer : "Test en 3 étapes"

2. NUCLEUS_ARCHITECTURE_COUCHES.md
   ├─ Lire : "Vue en couches"
   ├─ Lire : "Flux de traitement d'une requête"
   └─ Comprendre : "Détail de chaque couche"

3. NUCLEUS_BONNES_PRATIQUES.md
   ├─ Lire : "Bonnes pratiques générales"
   └─ Lire : "Anti-patterns à éviter"
```

### Pour implémenter une nouvelle feature

```
1. NUCLEUS_BONNES_PRATIQUES.md
   └─ Chercher l'exemple concret pertinent

2. NUCLEUS_ARCHITECTURE_COUCHES.md
   └─ Vérifier comment chaque couche s'intègre

3. NUCLEUS_GUIDE_COMPLET.md
   └─ Vérifier la configuration et les règles obligatoires
```

### Pour dépanner un bug

```
1. NUCLEUS_GUIDE_COMPLET.md
   ├─ Section "Observabilité et Monitoring"
   └─ Section "Troubleshooting" (si disponible)

2. NUCLEUS_ARCHITECTURE_COUCHES.md
   └─ "Flux de traitement" pour identifier où est le bug

3. NUCLEUS_BONNES_PRATIQUES.md
   └─ Anti-patterns pour vérifier si c'est une erreur connue
```

---

## 📍 Index par sujet

### Architecture & Design

| Sujet | Fichier | Section |
|-------|---------|---------|
| Vue générale | NUCLEUS_GUIDE_COMPLET.md | "Architecture générale" |
| 7 couches | NUCLEUS_ARCHITECTURE_COUCHES.md | "Vue en couches" |
| Flux requête | NUCLEUS_ARCHITECTURE_COUCHES.md | "Flux de traitement d'une requête" |
| Patterns | NUCLEUS_BONNES_PRATIQUES.md | "Patterns par couche" |

### Concepts Nucleus

| Sujet | Fichier | Section |
|-------|---------|---------|
| 7 principes | NUCLEUS_GUIDE_COMPLET.md | "Les 7 principes NUCLEUS" |
| Headers standards | NUCLEUS_GUIDE_COMPLET.md | "NucleusHeader" |
| @NucleusOp | NUCLEUS_GUIDE_COMPLET.md | "@NucleusOp" |
| Catalogue typé | NUCLEUS_GUIDE_COMPLET.md | "Catalogue NUCLEUS" |
| Enums | NUCLEUS_GUIDE_COMPLET.md | "Enums canoniques" |

### Implémentation

| Sujet | Fichier | Section |
|-------|---------|---------|
| Controller | NUCLEUS_ARCHITECTURE_COUCHES.md | "Couche 1" |
| Filters | NUCLEUS_ARCHITECTURE_COUCHES.md | "Couche 2" |
| AOP Aspect | NUCLEUS_ARCHITECTURE_COUCHES.md | "Couche 3" |
| Services Nucleus | NUCLEUS_ARCHITECTURE_COUCHES.md | "Couche 4" |
| Services métier | NUCLEUS_ARCHITECTURE_COUCHES.md | "Couche 5" |
| Repositories | NUCLEUS_ARCHITECTURE_COUCHES.md | "Couche 6" |
| BD | NUCLEUS_ARCHITECTURE_COUCHES.md | "Couche 7" |

### Bonnes pratiques

| Sujet | Fichier | Section |
|-------|---------|---------|
| Enums typés | NUCLEUS_BONNES_PRATIQUES.md | "Bonnes pratiques générales" |
| Sync Code+BD | NUCLEUS_BONNES_PRATIQUES.md | "Bonnes pratiques générales" |
| @NucleusOp | NUCLEUS_BONNES_PRATIQUES.md | "Bonnes pratiques générales" |
| Anti-patterns | NUCLEUS_BONNES_PRATIQUES.md | "Anti-patterns à éviter" |
| Exemples concrets | NUCLEUS_BONNES_PRATIQUES.md | "Exemples concrets" |

### Configuration & Tests

| Sujet | Fichier | Section |
|-------|---------|---------|
| Configuration | NUCLEUS_GUIDE_COMPLET.md | "Configuration NUCLEUS" |
| Tests rapides | NUCLEUS_GUIDE_COMPLET.md | "Tests" |
| Tests IT | NUCLEUS_BONNES_PRATIQUES.md | "Tests d'intégration" |
| Observabilité | NUCLEUS_GUIDE_COMPLET.md | "Observabilité et Monitoring" |

---

## 🚀 Quick Start

### Lancer et tester en 2 minutes

```bash
# 1. Lancer l'app
./gradlew bootRun

# 2. Appeler un endpoint
curl http://localhost:8080/nucleus/echo

# 3. Vérifier les logs
grep NUCLEUS_OP logs/console.log

# ✅ Si tu vois [NUCLEUS_OP START/END], c'est bon !
```

### Ajouter une opération en 5 étapes

```
1. Ajouter dans OperationCode.kt
2. Ajouter dans V*.sql (Flyway seed)
3. Créer le service avec @NucleusOp
4. Créer le controller
5. ./gradlew test (validation passe)
```

Voir **NUCLEUS_BONNES_PRATIQUES.md → "Exemple 1 : Ajouter une nouvelle opération"**

---

## 📋 Fichiers de référence

| Enum | Classe | Description |
|------|--------|-------------|
| `OperationCode` | `OperationCode.kt` | 121 opérations canoniques |
| `OperationNamespace` | `OperationNamespace.kt` | Namespaces des modules |
| `QuotaKey` | `QuotaKey.kt` | 25+ clés de quotas |
| `NucleusHeader` | `NucleusHeader.kt` | Headers standards (Consent, Safety, Latency) |
| `Operation` (Aspect) | `Operation.kt` | Enum pour @NucleusOp |

| Service | Fichier | Rôle |
|---------|---------|------|
| `NucleusCatalogueResolver` | `NucleusCatalogueResolver.kt` | Résoudre opérations/quotas |
| `PolicyEngineRuntimeService` | `PolicyEngineRuntimeService.kt` | Check & consume (TYPÉ) |
| `NucleusCatalogueInitValidator` | `NucleusCatalogueInitValidator.kt` | Validateur boot |

| Filtre | Fichier | Rôle |
|--------|---------|------|
| `ConsentGuard` | `ConsentGuard.kt` | Valider X-Consent-Version |
| `SafetyShield` | `SafetyShield.kt` | Valider X-Safety-Score |
| `LatencyBudgetFilter` | `LatencyBudgetFilter.kt` | Mesurer latence |
| `NucleusOpAspect` | `NucleusOpAspect.kt` | Instrumentalisation AOP |

---

## ✅ Checklist de compréhension

Après avoir lu les 3 documents, tu dois être capable de :

- [ ] Expliquer les 7 principes NUCLEUS
- [ ] Dessiner la vue en 7 couches
- [ ] Tracer le flux d'une requête POST
- [ ] Créer une nouvelle opération (Code + BD)
- [ ] Ajouter @NucleusOp à un service
- [ ] Implémenter un filtre personnalisé
- [ ] Lire les logs `[NUCLEUS_OP START/END]`
- [ ] Consulter les métriques via Actuator
- [ ] Identifier les anti-patterns dans du code
- [ ] Expliquer pourquoi on utilise des enums (pas de Strings)

---

## 🎓 Ressources additionnelles

### Fichiers source

```
src/main/kotlin/com/inokey/solution/dnk/multiplanner/core/nucleus/
├── guard/
│   ├── ConsentGuard.kt
│   └── SafetyShield.kt
├── filter/
│   ├── LatencyBudgetFilter.kt
│   └── TracingFilter.kt
├── aspect/
│   └── NucleusOpAspect.kt
├── resolver/
│   └── NucleusCatalogueResolver.kt
├── policy/
│   └── PolicyEngineRuntimeService.kt
├── probe/
│   └── NucleusProbeController.kt
└── ...
```

### Documentation officielle

```
docs/
├── README_NUCLEUS.md
├── NUCLEUS7_SETUP_GUIDE.md
├── NUCLEUS_OP_QUICK_START.md
├── NUCLEUS_OP_VERIFICATION.md
├── NUCLEUS_CATALOGUE_CANON.md
└── (3 nouveaux docs créés pour vous)
```

### Tests

```
src/test/kotlin/.../nucleus/
├── NucleusOpObservabilityIT.kt
├── NucleusCatalogueValidationIT.kt
└── ...
```

---

## 📞 Besoin d'aide ?

- **Erreur au boot :** Vérifier `NucleusCatalogueInitValidator` (drift?)
- **Pas de logs `[NUCLEUS_OP START/END]` :** Vérifier `logging.level.NucleusOp: DEBUG`
- **Requête bloquée 400/403 :** Vérifier les headers (`X-Consent-Version`, `X-Safety-Score`)
- **Métriques manquantes :** Vérifier que le service a `@NucleusOp`
- **String magique détectée :** Utiliser `OperationCode.*` ou `QuotaKey.*`

---

## 🎉 Prochaines étapes

1. **Lire** les 3 documents dans l'ordre
2. **Lancer l'app** et faire le test en 3 étapes
3. **Consulter les logs** pour voir `[NUCLEUS_OP START/END]`
4. **Implémenter** une nouvelle opération test
5. **Vérifier** avec les checklists

**Bon développement ! 🚀**

---

**Créé :** 2026-04-13  
**Auteur :** GitHub Copilot + Documentation NUCLEUS MultiPlanner

