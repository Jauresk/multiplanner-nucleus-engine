package com.inokey.solution.dnk.nucleus.observability.autoconfigure

/**
 * 📇 Catalogue des opérations métier observables dans MultiPlanner.
 *
 * **Buts** :
 * 1. **Centraliser les noms** : chaque opération a un nom unique, cohérent.
 * 2. **Instrumenter facilement** : `@NucleusOp(Operation.PLANI_LOISIR_REGISTER)` suffit.
 * 3. **Corréler** : logs, métriques, traces partagent les mêmes noms.
 *
 * **Propriétés dérivées** :
 * - `metricName`: Nom Micrometer → ex: `op.planiLoisir.register` (pour Prometheus/Grafana).
 * - `spanName`: Nom du span pour traces → ex: `planiLoisir/register` (pour Tempo/Jaeger).
 * - `otelName`: Nom OpenTelemetry → ex: `planiLoisir.register` (format OTEL standard).
 *
 * **Conventions** :
 * - Module en camelCase (ex: `planiLoisir`, `oasis`, `nucleus`).
 * - Action en verbe simpl (ex: `register`, `find`, `import`, `validate`).
 * - Metrics/spans toujours lowercase (PostgreSQL, OpenTelemetry standards).
 *
 * **Usage** :
 * ```kotlin
 * @NucleusOp(Operation.PLANI_LOISIR_REGISTER, extraTags = ["endpoint=post"])
 * fun registerUser(...): Mono<Response> { ... }
 * ```
 *
 * → Génère automatiquement :
 * - Métrique Micrometer : `op.planiLoisir.register` (latence, succès/erreur).
 * - Span OpenTelemetry : `planiLoisir/register` (corrélé via traceId).
 * - Tags standards : `module=planiLoisir`, `op=register`, `endpoint=post`.
 */
enum class MultiplannerOperation(val module: String, val action: String) {

    // ========== PlaniLoisir ==========
    /** Enregistrement d'un nouvel utilisateur PlaniLoisir (write). */
    PLANI_LOISIR_ACTIVATE("planiLoisir", "activation"),

    /** Audit des suggestions d'activités favorites (read/analysis). */
    PLANI_LOISIR_AUDIT_FAVORITE_ACTIVITIES_SUGGESTIONS(
        "planiLoisir",
        "auditFavoriteActivitiesSuggestions"
    ),

    /** Recherche d'un utilisateur PlaniLoisir par email (read). */
    PLANI_LOISIR_FIND("planiLoisir", "find"),

    /** Récupération du statut d'activation PlaniLoisir (read). */
    PLANI_LOISIR_GET_STATUS("planiLoisir", "getStatus"),

    /** Désactivation du module PlaniLoisir. */
    PLANI_LOISIR_DEACTIVATE("planiLoisir", "deactivate"),

    /** Suspension du module PlaniLoisir. */
    PLANI_LOISIR_SUSPEND("planiLoisir", "suspend"),

    /** Réactivation du module PlaniLoisir après suspension. */
    PLANI_LOISIR_RESUME("planiLoisir", "resume"),

    /** Reprise d'un workflow de souscription échoué. */
    PLANI_LOISIR_RESUME_WORKFLOW("planiLoisir", "resumeWorkflow"),

    // ========== Multiplanner ==========
    /** Complétion d\'une opération idempotente. */
    MULTIPLANNER_IDEMPOTENCY_COMPLETE("multiplanner", "idempotencyComplete"),

    /** Protection/guard d\'une opération idempotente. */
    MULTIPLANNER_IDEMPOTENCY_GUARD("multiplanner", "idempotencyGuard"),

    /** Échec d\'une opération idempotente. */
    MULTIPLANNER_IDEMPOTENCY_FAIL("multiplanner", "idempotencyFail"),

    /** Provisioning d\'un compte MultiPlanner. */
    MULTIPLANNER_ACCOUNT_PROVISION("multiplanner", "accountProvision"),

    /** Enregistrement local d\'un utilisateur (auth module). */
    MULTIPLANNER_AUTH_REGISTER_LOCAL("auth", "registerLocal"),

    /** Enregistrement local via Keycloak (auth module). */
    MULTIPLANNER_AUTH_REGISTER_LOCAL_KEYCLOAK("auth", "registerLocalKeycloak"),

    /** Provisioning local d\'un utilisateur (auth module). */
    MULTIPLANNER_AUTH_REGISTER_LOCAL_PROVISION("auth", "registerLocalProvision"),

    /** Cleanup local d\'un utilisateur (auth module). */
    MULTIPLANNER_AUTH_REGISTER_LOCAL_CLEANUP("auth", "registerLocalCleanup"),

    /** Provisioning après login JWT (auth module). */
    MULTIPLANNER_AUTH_PROVISION("auth", "provision"),

    /** Whoami / snapshot utilisateur courant (auth module). */
    MULTIPLANNER_AUTH_WHOAMI("auth", "whoami"),

    /** Démarrage liaison d'identité (identity linking). */
    MULTIPLANNER_IDENTITY_LINK_START("auth", "identityLinkStart"),

    /** Complétion du profil utilisateur (gender, birthDate, address). */
    MULTIPLANNER_ACCOUNT_COMPLETE_PROFILE("account", "completeProfile"),

    /** Vérification et consommation d\'une règle PolicyEngine. */
    POLICYENGINE_CHECK_AND_CONSUME("policyEngine", "checkAndConsume"),

    // ========== OASIS (partenaire/imports) ==========
    /** Importation de données depuis le système OASIS (batch/API). */
    OASIS_IMPORT("oasis", "import"),

    /** Synchronisation des données OASIS. */
    OASIS_SYNC("oasis", "sync"),

    // ========== Nucleus (probes/santé) ==========
    /** Probe générique Nucleus pour tests/health-checks. */
    NUCLEUS_PROBE("nucleus", "probe"),

    /** Contraction du registre des principes (audit/discovery). */
    NUCLEUS_CONTRACT_INTROSPECT("nucleus", "contractIntrospect"),

    /** Récupération des champs d\'un contrat (inspection/schema). */
    NUCLEUS_CONTRACT_FIELDS("nucleus", "contractFields"),

    /** Récupération du catalogue complet des opérations nucleus (inspection). */
    NUCLEUS_OPERATIONS_CATALOG("nucleus", "operationsCatalog"),

    /** Echo simple pour vérifier la disponibilité. */
    NUCLEUS_ECHO("nucleus", "echo"),

    /** Création d'utilisateur en probe (démonstration). */
    NUCLEUS_PROBE_CREATE_USER("nucleus", "probeCreateUser"),

    /** Simulation de latence. */
    NUCLEUS_DELAY("nucleus", "delay"),

    /** Explication des blocages. */
    NUCLEUS_EXPLAIN("nucleus", "explain"),

    /** Suggestions d'idées. */
    NUCLEUS_IDEAS("nucleus", "ideas"),

    /** Probe sécurité. */
    NUCLEUS_SAFETY_PROBE("nucleus", "safetyProbe"),

    /** Démonstration d'erreur. */
    NUCLEUS_ERROR_DEMO("nucleus", "errorDemo"),

    // ========== AuthZ (Authorization Engine) ==========
    /** Décision d'autorisation principale. */
    AUTHZ_AUTHORIZE("authz", "authorize"),

    /** Vérification des quotas (mode SOFT). */
    AUTHZ_QUOTA_CHECK("authz", "quotaCheck"),

    /** Consommation des quotas (mode HARD). */
    AUTHZ_QUOTA_CONSUME("authz", "quotaConsume"),

    /** Résolution des policies. */
    AUTHZ_POLICY_RESOLVE("authz", "policyResolve"),

    // ========== Project Activation Facade ==========
    /** Façade d'activation de projets (routing). */
    PROJECT_ACTIVATION_FACADE("activation", "projectFacade"),

    // ========== PlaniLoisir Activation ==========
    /** Activation du module PlaniLoisir. */
    PLANILOISIR_ACTIVATE("planiloisir", "activate"),

    /** Récupération du statut d'activation PlaniLoisir. */
    PLANILOISIR_GET_STATUS("planiloisir", "getStatus"),

    /** Récupération des groupes PlaniLoisir. */
    PLANILOISIR_GET_GROUPS("planiloisir", "getGroups"),

    /** Création d'un groupe PlaniLoisir. */
    PLANILOISIR_CREATE_GROUP("planiloisir", "createGroup"),

    /** Récupération d'un groupe PlaniLoisir. */
    PLANILOISIR_GROUP_GET("planiloisir", "groupGet"),

    /** Invitation d'un membre dans un groupe PlaniLoisir. */
    PLANILOISIR_GROUP_INVITE("planiloisir", "groupInvite"),

    /** Liste des membres d'un groupe PlaniLoisir. */
    PLANILOISIR_GROUP_MEMBERS("planiloisir", "groupMembers"),

    /** Souscription PlaniLoisir avec groupe et membres. */
    PLANILOISIR_SUBSCRIBE("planiloisir", "subscribe"),

    /** Création d'une souscription MultiPlanner (point d'entrée unifié). */
    MULTIPLANNER_SUBSCRIPTION_CREATE("subscription", "create"),

    /** Audit des suggestions d'activités. */
    PLANILOISIR_AUDIT_FAVORITE_ACTIVITIES_SUGGESTIONS("planiloisir", "auditFavoriteActivitiesSuggestions"),

    // ========== PlaniLoisir - Plannings ==========
    /** Création d'un planning PlaniLoisir. */
    PLANILOISIR_PLANNING_CREATE("planiloisir", "planningCreate"),

    /** Liste des plannings PlaniLoisir. */
    PLANILOISIR_PLANNING_LIST("planiloisir", "planningList"),

    /** Récupération d'un planning PlaniLoisir. */
    PLANILOISIR_PLANNING_GET("planiloisir", "planningGet"),

    /** Mise à jour d'un planning PlaniLoisir. */
    PLANILOISIR_PLANNING_UPDATE("planiloisir", "planningUpdate"),

    /** Suppression d'un planning PlaniLoisir. */
    PLANILOISIR_PLANNING_DELETE("planiloisir", "planningDelete"),

    /** Activation d'un planning PlaniLoisir. */
    PLANILOISIR_PLANNING_ACTIVATE("planiloisir", "planningActivate"),

    /** Archivage d'un planning PlaniLoisir. */
    PLANILOISIR_PLANNING_ARCHIVE("planiloisir", "planningArchive"),

    // ========== PlaniLoisir - AutoPlan ==========
    /** Génération d'un autoplan (draft non persisté). */
    PLANILOISIR_AUTOPLAN_GENERATE("planiloisir", "autoplanGenerate"),

    /** Recalcul d'un autoplan après modification. */
    PLANILOISIR_AUTOPLAN_RECOMPUTE("planiloisir", "autoplanRecompute"),

    /** Confirmation et persistance d'un autoplan. */
    PLANILOISIR_AUTOPLAN_CONFIRM("planiloisir", "autoplanConfirm"),

    // ========== PlaniLoisir - Profile ==========
    /** Récupération du profil PlaniLoisir. */
    PLANILOISIR_PROFILE_GET("planiloisir", "profileGet"),

    /** Mise à jour du consentement géolocalisation. */
    PLANILOISIR_PROFILE_UPDATE_GEO_CONSENT("planiloisir", "profileUpdateGeoConsent"),

    /** Mise à jour des budgets PlaniLoisir. */
    PLANILOISIR_PROFILE_UPDATE_BUDGETS("planiloisir", "profileUpdateBudgets"),

    /** Mise à jour des centres d'intérêt PlaniLoisir. */
    PLANILOISIR_PROFILE_UPDATE_INTERESTS("planiloisir", "profileUpdateInterests"),

    /** Mise à jour des activités favorites PlaniLoisir. */
    PLANILOISIR_PROFILE_UPDATE_FAVORITE_ACTIVITIES("planiloisir", "profileUpdateFavoriteActivities"),

    // ========== PlaniLoisir - Jobs ==========
    /** Récupération du statut d'un job de génération. */
    PLANILOISIR_JOB_STATUS("planiloisir", "jobStatus"),

    /** Récupération du résultat d'un job de génération. */
    PLANILOISIR_JOB_RESULT("planiloisir", "jobResult"),

    /** Annulation d'un job de génération. */
    PLANILOISIR_JOB_CANCEL("planiloisir", "jobCancel"),

    /** Stream SSE de progression d'un job. */
    PLANILOISIR_JOB_PROGRESS("planiloisir", "jobProgress"),

    // ========== PlaniVente Activation ==========
    /** Activation du module PlaniVente. */
    PLANIVENTE_ACTIVATE("planivente", "activate"),

    /** Récupération du statut d'activation PlaniVente. */
    PLANIVENTE_GET_STATUS("planivente", "getStatus"),

    /** Désactivation du module PlaniVente. */
    PLANIVENTE_DEACTIVATE("planivente", "deactivate"),

    /** Suspension du module PlaniVente. */
    PLANIVENTE_SUSPEND("planivente", "suspend"),

    /** Réactivation du module PlaniVente après suspension. */
    PLANIVENTE_RESUME("planivente", "resume"),

    /** Récupération des employés PlaniVente. */
    PLANIVENTE_GET_EMPLOYEES("planivente", "getEmployees"),

    /** Ajout d'un employé PlaniVente. */
    PLANIVENTE_ADD_EMPLOYEE("planivente", "addEmployee"),

    /** Récupération d'un employé PlaniVente. */
    PLANIVENTE_EMPLOYEE_GET("planivente", "employeeGet"),

    /** Liste des employés PlaniVente. */
    PLANIVENTE_EMPLOYEE_LIST("planivente", "employeeList"),

    /** Mise à jour d'un employé PlaniVente. */
    PLANIVENTE_EMPLOYEE_UPDATE("planivente", "employeeUpdate"),

    /** Suppression/désactivation d'un employé PlaniVente. */
    PLANIVENTE_EMPLOYEE_DELETE("planivente", "employeeDelete"),

    /** Mise à jour des rôles d'un employé PlaniVente. */
    PLANIVENTE_EMPLOYEE_ROLES_UPDATE("planivente", "employeeRolesUpdate"),

    /** Invitation d'un employé PlaniVente. */
    PLANIVENTE_EMPLOYEE_INVITE("planivente", "employeeInvite"),

    /** Acceptation d'une invitation employé PlaniVente. */
    PLANIVENTE_EMPLOYEE_ACCEPT_INVITATION("planivente", "employeeAcceptInvitation"),

    /** Refus d'une invitation employé PlaniVente. */
    PLANIVENTE_EMPLOYEE_DECLINE_INVITATION("planivente", "employeeDeclineInvitation"),

    /** Création d'une filiale/agence PlaniVente. */
    PLANIVENTE_BRANCH_CREATE("planivente", "branchCreate"),

    /** Mise à jour d'une filiale/agence PlaniVente. */
    PLANIVENTE_BRANCH_UPDATE("planivente", "branchUpdate"),

    /** Suppression d'une filiale/agence PlaniVente. */
    PLANIVENTE_BRANCH_DELETE("planivente", "branchDelete"),

    /** Liste des filiales/agences PlaniVente. */
    PLANIVENTE_BRANCH_LIST("planivente", "branchList"),

    /** Récupération d'une filiale/agence PlaniVente. */
    PLANIVENTE_BRANCH_GET("planivente", "branchGet"),

    /** Comptage des filiales/agences PlaniVente. */
    PLANIVENTE_BRANCH_COUNT("planivente", "branchCount"),

    /** Création/mise à jour d'un profil entreprise PlaniVente. */
    PLANIVENTE_COMPANY_UPSERT("planivente", "companyUpsert"),

    /** Récupération d'un profil entreprise PlaniVente. */
    PLANIVENTE_COMPANY_GET("planivente", "companyGet"),

    /** Liste des entreprises accessibles à un compte PlaniVente. */
    PLANIVENTE_COMPANY_LIST("planivente", "companyList"),

    /** Assignation d'un employé à une entreprise PlaniVente. */
    PLANIVENTE_EMPLOYEE_ASSIGN("planivente", "employeeAssign"),

    /** Création d'un groupe PlaniVente. */
    PLANIVENTE_GROUP_CREATE("planivente", "groupCreate"),

    /** Récupération d'un groupe PlaniVente. */
    PLANIVENTE_GROUP_GET("planivente", "groupGet"),

    /** Liste des groupes PlaniVente. */
    PLANIVENTE_GROUP_LIST("planivente", "groupList"),

    /** Liste des invitations PlaniVente. */
    PLANIVENTE_INVITATION_LIST("planivente", "invitationList"),

    /** Annulation d'une invitation PlaniVente. */
    PLANIVENTE_INVITATION_CANCEL("planivente", "invitationCancel"),

    // ========== PlaniVente - Événements ==========
    /** Création d'un événement PlaniVente. */
    PLANIVENTE_EVENT_CREATE("planivente", "eventCreate"),

    /** Liste des événements PlaniVente. */
    PLANIVENTE_EVENT_LIST("planivente", "eventList"),

    /** Récupération d'un événement PlaniVente. */
    PLANIVENTE_EVENT_GET("planivente", "eventGet"),

    /** Mise à jour d'un événement PlaniVente. */
    PLANIVENTE_EVENT_UPDATE("planivente", "eventUpdate"),

    /** Suppression d'un événement PlaniVente. */
    PLANIVENTE_EVENT_DELETE("planivente", "eventDelete"),

    // ========== PlaniVente - Horaires ==========
    /** Création d'un horaire PlaniVente. */
    PLANIVENTE_HORAIRE_CREATE("planivente", "horaireCreate"),

    /** Liste des horaires PlaniVente. */
    PLANIVENTE_HORAIRE_LIST("planivente", "horaireList"),

    /** Récupération d'un horaire PlaniVente. */
    PLANIVENTE_HORAIRE_GET("planivente", "horaireGet"),

    /** Mise à jour d'un horaire PlaniVente. */
    PLANIVENTE_HORAIRE_UPDATE("planivente", "horaireUpdate"),

    /** Suppression d'un horaire PlaniVente. */
    PLANIVENTE_HORAIRE_DELETE("planivente", "horaireDelete"),

    // ========== PlaniVente - Offres Produits ==========
    /** Création d'une offre produit PlaniVente. */
    PLANIVENTE_OFFRE_PRODUIT_CREATE("planivente", "offreProduitCreate"),

    /** Liste des offres produits PlaniVente. */
    PLANIVENTE_OFFRE_PRODUIT_LIST("planivente", "offreProduitList"),

    /** Récupération d'une offre produit PlaniVente. */
    PLANIVENTE_OFFRE_PRODUIT_GET("planivente", "offreProduitGet"),

    /** Mise à jour d'une offre produit PlaniVente. */
    PLANIVENTE_OFFRE_PRODUIT_UPDATE("planivente", "offreProduitUpdate"),

    /** Suppression d'une offre produit PlaniVente. */
    PLANIVENTE_OFFRE_PRODUIT_DELETE("planivente", "offreProduitDelete"),

    // ========== PlaniVente - Offres Services ==========
    /** Création d'une offre service PlaniVente. */
    PLANIVENTE_OFFRE_SERVICE_CREATE("planivente", "offreServiceCreate"),

    /** Liste des offres services PlaniVente. */
    PLANIVENTE_OFFRE_SERVICE_LIST("planivente", "offreServiceList"),

    /** Récupération d'une offre service PlaniVente. */
    PLANIVENTE_OFFRE_SERVICE_GET("planivente", "offreServiceGet"),

    /** Mise à jour d'une offre service PlaniVente. */
    PLANIVENTE_OFFRE_SERVICE_UPDATE("planivente", "offreServiceUpdate"),

    /** Suppression d'une offre service PlaniVente. */
    PLANIVENTE_OFFRE_SERVICE_DELETE("planivente", "offreServiceDelete"),

    // ========== PlaniVente - Parkings ==========
    /** Création d'un parking PlaniVente. */
    PLANIVENTE_PARKING_CREATE("planivente", "parkingCreate"),

    /** Liste des parkings PlaniVente. */
    PLANIVENTE_PARKING_LIST("planivente", "parkingList"),

    /** Récupération d'un parking PlaniVente. */
    PLANIVENTE_PARKING_GET("planivente", "parkingGet"),

    /** Mise à jour d'un parking PlaniVente. */
    PLANIVENTE_PARKING_UPDATE("planivente", "parkingUpdate"),

    /** Suppression d'un parking PlaniVente. */
    PLANIVENTE_PARKING_DELETE("planivente", "parkingDelete"),

    // ========== PlaniCourse Activation ==========
    /** Activation du module PlaniCourse. */
    PLANICOURSE_ACTIVATE("planicourse", "activate"),

    /** Récupération du statut d'activation PlaniCourse. */
    PLANICOURSE_GET_STATUS("planicourse", "getStatus"),

    /** Désactivation du module PlaniCourse. */
    PLANICOURSE_DEACTIVATE("planicourse", "deactivate"),

    /** Suspension du module PlaniCourse. */
    PLANICOURSE_SUSPEND("planicourse", "suspend"),

    /** Réactivation du module PlaniCourse après suspension. */
    PLANICOURSE_RESUME("planicourse", "resume"),

    // ========== PlaniDécouverte Activation ==========
    /** Activation du module PlaniDécouverte. */
    PLANIDECOUVERTE_ACTIVATE("planidecouverte", "activate"),

    /** Récupération du statut d'activation PlaniDécouverte. */
    PLANIDECOUVERTE_GET_STATUS("planidecouverte", "getStatus"),

    /** Désactivation du module PlaniDécouverte. */
    PLANIDECOUVERTE_DEACTIVATE("planidecouverte", "deactivate"),

    /** Suspension du module PlaniDécouverte. */
    PLANIDECOUVERTE_SUSPEND("planidecouverte", "suspend"),

    /** Réactivation du module PlaniDécouverte après suspension. */
    PLANIDECOUVERTE_RESUME("planidecouverte", "resume"),

    /** Récupération des favoris PlaniDécouverte. */
    PLANIDECOUVERTE_GET_FAVORITES("planidecouverte", "getFavorites"),

    /** Ajout d'un favori PlaniDécouverte. */
    PLANIDECOUVERTE_ADD_FAVORITE("planidecouverte", "addFavorite"),

    /** Récupération des informations météo PlaniDécouverte. */
    PLANIDECOUVERTE_METEO_GET("planidecouverte", "meteoGet"),

    // ========== PlaniDécouverte Métier ==========
    /** Recherche simple par besoin (moteur V1 - gratuit). */
    PLANIDECOUVERTE_RECHERCHE_SIMPLE("planidecouverte", "rechercheSimple"),

    /** Recherche vocale par besoin (moteur V1 - gratuit). */
    PLANIDECOUVERTE_RECHERCHE_VOCALE("planidecouverte", "rechercheVocale"),

    /** Recherche d'événements par ville. */
    PLANIDECOUVERTE_EVENEMENTS_VILLE("planidecouverte", "evenementsVille"),

    /** Découverte à proximité - entreprises/services (B2C public). */
    PLANIDECOUVERTE_DISCOVER_NEARBY("planidecouverte", "discoverNearby"),

    /** Découverte à proximité - événements (B2C public). */
    PLANIDECOUVERTE_DISCOVER_EVENTS("planidecouverte", "discoverEvents"),

    /** Suggestions IA personnalisées (quota-gated). */
    PLANIDECOUVERTE_SUGGESTIONS_IA("planidecouverte", "suggestionsIA"),

    /** Suggestions IA vocales (quota-gated). */
    PLANIDECOUVERTE_SUGGESTIONS_IA_VOCAL("planidecouverte", "suggestionsIAVocal"),

    /** Récupération du contexte utilisateur découverte. */
    PLANIDECOUVERTE_CONTEXTE_GET("planidecouverte", "contexteGet"),

    /** Récupération de l'historique découverte. */
    PLANIDECOUVERTE_HISTORIQUE_GET("planidecouverte", "historiqueGet"),

    /** Création de session publique anonyme. */
    PLANIDECOUVERTE_SESSION_CREATE("planidecouverte", "sessionCreate"),

    /** Fusion des modifications dans la session publique. */
    PLANIDECOUVERTE_SESSION_MERGE("planidecouverte", "sessionMerge"),

    /** Migration session publique vers compte utilisateur. */
    PLANIDECOUVERTE_SESSION_MIGRATE("planidecouverte", "sessionMigrate"),

    /** Récupération trafic temps réel (quota-gated). */
    PLANIDECOUVERTE_TRAFIC_GET("planidecouverte", "traficGet"),

    // ========== PlaniDécouverte - Feed / Videos / Banners / Settings ==========
    /** Récupération du feed d'actualités publiques. */
    PLANIDECOUVERTE_FEED_GET("planidecouverte", "feedGet"),

    /** Récupération de la liste de vidéos publiques. */
    PLANIDECOUVERTE_VIDEOS_GET("planidecouverte", "videosGet"),

    /** Récupération des bannières publicitaires publiques. */
    PLANIDECOUVERTE_BANNERS_GET("planidecouverte", "bannersGet"),

    /** Récupération des paramètres utilisateur découverte. */
    PLANIDECOUVERTE_SETTINGS_GET("planidecouverte", "settingsGet"),

    /** Mise à jour des paramètres utilisateur découverte. */
    PLANIDECOUVERTE_SETTINGS_UPDATE("planidecouverte", "settingsUpdate"),

    // ========== PlaniDécouverte - Admin WRITE (Feed / Videos / Banners) ==========
    /** Création d'un article du feed (admin). */
    PLANIDECOUVERTE_FEED_CREATE("planidecouverteAdmin", "feedCreate"),

    /** Mise à jour d'un article du feed (admin). */
    PLANIDECOUVERTE_FEED_UPDATE("planidecouverteAdmin", "feedUpdate"),

    /** Publication / dépublication d'un article du feed (admin). */
    PLANIDECOUVERTE_FEED_PUBLISH("planidecouverteAdmin", "feedPublish"),

    /** Suppression d'un article du feed (admin). */
    PLANIDECOUVERTE_FEED_DELETE("planidecouverteAdmin", "feedDelete"),

    /** Création d'une vidéo (admin). */
    PLANIDECOUVERTE_VIDEO_CREATE("planidecouverteAdmin", "videoCreate"),

    /** Mise à jour d'une vidéo (admin). */
    PLANIDECOUVERTE_VIDEO_UPDATE("planidecouverteAdmin", "videoUpdate"),

    /** Publication / dépublication d'une vidéo (admin). */
    PLANIDECOUVERTE_VIDEO_PUBLISH("planidecouverteAdmin", "videoPublish"),

    /** Suppression d'une vidéo (admin). */
    PLANIDECOUVERTE_VIDEO_DELETE("planidecouverteAdmin", "videoDelete"),

    /** Création d'une bannière publicitaire (admin). */
    PLANIDECOUVERTE_BANNER_CREATE("planidecouverteAdmin", "bannerCreate"),

    /** Mise à jour d'une bannière publicitaire (admin). */
    PLANIDECOUVERTE_BANNER_UPDATE("planidecouverteAdmin", "bannerUpdate"),

    /** Publication / dépublication d'une bannière (admin). */
    PLANIDECOUVERTE_BANNER_PUBLISH("planidecouverteAdmin", "bannerPublish"),

    /** Suppression d'une bannière publicitaire (admin). */
    PLANIDECOUVERTE_BANNER_DELETE("planidecouverteAdmin", "bannerDelete"),

    // ========== PlaniDécouverte - API Keys (B2B/B2G) ==========
    /** Création d'une clé API (B2B/B2G). */
    PLANIDECOUVERTE_API_KEY_CREATE("planidecouverte", "apiKeyCreate"),

    /** Liste des clés API (B2B/B2G). */
    PLANIDECOUVERTE_API_KEY_LIST("planidecouverte", "apiKeyList"),

    /** Révocation d'une clé API (B2B/B2G). */
    PLANIDECOUVERTE_API_KEY_REVOKE("planidecouverte", "apiKeyRevoke"),

    // ========== PlaniDécouverte - Usage Dashboard (B2B/B2G) ==========
    /** Récupération usage journalier (B2B/B2G dashboard). */
    PLANIDECOUVERTE_USAGE_DAILY_GET("planidecouverte", "usageDailyGet"),

    /** Récupération événements d'usage (B2B/B2G dashboard). */
    PLANIDECOUVERTE_USAGE_EVENTS_GET("planidecouverte", "usageEventsGet"),

    /** Récupération état du quota (B2B/B2G dashboard). */
    PLANIDECOUVERTE_QUOTA_STATUS_GET("planidecouverte", "quotaStatusGet"),

    // ========== Moneris (Paiement) ==========
    /** Réception d'un webhook Moneris. */
    MONERIS_WEBHOOK_RECEIVE("moneris", "webhookReceive"),

    /** Traitement d'un paiement Moneris. */
    MONERIS_PAYMENT_PROCESS("moneris", "paymentProcess"),

    /** Création d'un abonnement Moneris. */
    MONERIS_SUBSCRIPTION_CREATE("moneris", "subscriptionCreate"),

    /** Annulation d'un abonnement Moneris. */
    MONERIS_SUBSCRIPTION_CANCEL("moneris", "subscriptionCancel"),

    /** Remboursement Moneris. */
    MONERIS_REFUND_PROCESS("moneris", "refundProcess"),

    // ========== Admin Plateforme - Gestion Entreprises ==========
    /** Liste des entreprises (admin). */
    ADMIN_COMPANY_LIST("admin", "companyList"),

    /** Détail d'une entreprise (admin). */
    ADMIN_COMPANY_GET("admin", "companyGet"),

    /** Revue / approbation d'une entreprise (admin). */
    ADMIN_COMPANY_REVIEW("admin", "companyReview"),

    /** Fusion de deux fiches entreprise (admin). */
    ADMIN_COMPANY_MERGE("admin", "companyMerge"),

    // ========== Admin Plateforme - Campagnes ==========
    /** Liste des campagnes publicitaires (admin). */
    ADMIN_CAMPAIGN_LIST("admin", "campaignList"),

    /** Création d'une campagne (admin). */
    ADMIN_CAMPAIGN_CREATE("admin", "campaignCreate"),

    /** Mise à jour d'une campagne (admin). */
    ADMIN_CAMPAIGN_UPDATE("admin", "campaignUpdate"),

    /** Suppression d'une campagne (admin). */
    ADMIN_CAMPAIGN_DELETE("admin", "campaignDelete"),

    // ========== Admin Plateforme - Billing ==========
    /** Vue d'ensemble billing (admin finance). */
    ADMIN_BILLING_OVERVIEW("admin", "billingOverview"),

    /** Liste des transactions (admin finance). */
    ADMIN_BILLING_TRANSACTIONS("admin", "billingTransactions"),

    /** Remboursement admin (admin finance). */
    ADMIN_BILLING_REFUND("admin", "billingRefund"),

    // ========== Admin Plateforme - Billing Invoices/Payments ==========
    /** Création d'une facture (admin). */
    ADMIN_BILLING_INVOICE_CREATE("adminBilling", "invoiceCreate"),

    /** Récupération d'une facture (admin). */
    ADMIN_BILLING_INVOICE_GET("adminBilling", "invoiceGet"),

    /** Liste des factures (admin). */
    ADMIN_BILLING_INVOICE_LIST("adminBilling", "invoiceList"),

    /** Annulation d'une facture (admin). */
    ADMIN_BILLING_INVOICE_CANCEL("adminBilling", "invoiceCancel"),

    /** Initiation d'un paiement Moneris (admin). */
    ADMIN_BILLING_PAYMENT_INITIATE("adminBilling", "paymentInitiate"),

    /** Callback paiement Moneris (système). */
    ADMIN_BILLING_PAYMENT_CALLBACK("adminBilling", "paymentCallback"),

    /** Bypass de facturation - MP_ADMIN ONLY. */
    ADMIN_BILLING_BYPASS_GRANT("adminBilling", "bypassGrant"),

    /** Audit des bypass de facturation. */
    ADMIN_BILLING_AUDIT_BYPASSES("adminBilling", "auditBypasses"),

    /** Liste des tarifs (admin). */
    ADMIN_BILLING_PRICING_LIST("adminBilling", "pricingList"),

    /** Calcul de prix (admin). */
    ADMIN_BILLING_PRICING_CALCULATE("adminBilling", "pricingCalculate"),

    // ========== Admin Plateforme - Audit ==========
    /** Lecture des logs d'audit (admin). */
    ADMIN_AUDIT_LOG_READ("admin", "auditLogRead"),

    // ========== Admin Plateforme - Gestion Utilisateurs ==========
    /** Liste des utilisateurs (admin). */
    ADMIN_USER_LIST("admin", "userList"),

    /** Détail d'un utilisateur (admin). */
    ADMIN_USER_GET("admin", "userGet"),

    /** Suspension d'un utilisateur (admin). */
    ADMIN_USER_SUSPEND("admin", "userSuspend"),

    /** Changement de rôle d'un utilisateur (admin). */
    ADMIN_USER_ROLE_CHANGE("admin", "userRoleChange"),

    // ========== Admin Plateforme - Revue de contenu ==========
    /** Liste du contenu par statut (admin). */
    ADMIN_CONTENT_LIST("admin", "contentList"),

    /** Transition de statut d'un contenu (admin). */
    ADMIN_CONTENT_TRANSITION("admin", "contentTransition"),

    // ========== Invitations ==========
    /** Récupération des détails d'une invitation. */
    MULTIPLANNER_INVITATION_GET_DETAILS("invitation", "getDetails"),

    /** Acceptation d'une invitation. */
    MULTIPLANNER_INVITATION_ACCEPT("invitation", "accept"),

    /** Refus d'une invitation. */
    MULTIPLANNER_INVITATION_DECLINE("invitation", "decline"),

    // ========== PlaniVente - Activity Profile (V47) ==========

    /** Lecture du profil d'activité entreprise. */
    PLANIVENTE_ACTIVITY_PROFILE_READ("planivente", "activityProfileRead"),

    /** Écriture du profil d'activité entreprise. */
    PLANIVENTE_ACTIVITY_PROFILE_WRITE("planivente", "activityProfileWrite"),

    /** Lecture publique shell métier (B2C). */
    PLANIVENTE_PUBLIC_SHELL_READ("planivente", "publicShellRead"),

    // ========== Pertinence - Télémétrie ==========
    /** Ingestion d'un événement de télémétrie Pertinence (endpoint interne backend-enrichment). */
    PERTINENCE_TELEMETRY_INGEST_EVENT("pertinence", "telemetryIngestEvent"),

    /** Ingestion d'un lot d'événements de télémétrie Pertinence (endpoint interne backend-enrichment). */
    PERTINENCE_TELEMETRY_INGEST_BATCH("pertinence", "telemetryIngestBatch"),

    /** Réception proxy d'un événement de télémétrie Pertinence (gateway backend-api). */
    PERTINENCE_TELEMETRY_PROXY_RECEIVE("pertinence", "telemetryProxyReceive"),

    /** Réception proxy d'un lot d'événements de télémétrie Pertinence (gateway backend-api). */
    PERTINENCE_TELEMETRY_PROXY_BATCH("pertinence", "telemetryProxyBatch");

    // ========== Propriétés dérivées ==========

    /** Nom métrique Micrometer (format: `op.{module}.{action}`).
     *
     * Exemple : `op.planiLoisir.register`
     * Utilisé dans Prometheus/Grafana pour les dashboards.
     */
    val metricName: String get() = "op.$module.$action"

    /** Nom du span pour traçabilité distribuée (format: `{module}/{action}`).
     *
     * Exemple : `planiLoisir/register`
     * Utilisé dans Tempo, Jaeger, Zipkin pour suivre les requêtes.
     */
    val spanName: String get() = "$module/$action"

    /** Nom OpenTelemetry standard (format: `{module}.{action}`).
     *
     * Exemple : `planiLoisir.register`
     * Format adhérent aux conventions OTel.
     */
    val otelName: String get() = "$module.$action"
}
