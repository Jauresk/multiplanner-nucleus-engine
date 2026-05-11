# Nucleus Frontend SDK

Projet de librairies frontend **séparé du Nucleus backend**.

## Structure

```text
nucleus-frontend-sdk/
  packages/
    nucleus-frontend-core/
    nucleus-angular/
    nucleus-react-native/
```

## Rôle

```text
Nucleus backend
  - nucleus-core
  - nucleus-observability

Nucleus frontend
  - nucleus-frontend-sdk
```

Le SDK frontend ne remplace pas le backend Nucleus.  
Il propage côté client les informations que le backend sait déjà exploiter :

- `X-Correlation-Id`
- `X-Session-Id`
- `X-Consent-Version`
- lecture de `X-Request-Timing`
- latence API côté client
- erreurs techniques frontend

## Packages

### `@multiplanner/nucleus-frontend-core`
Coeur TypeScript indépendant :
- constantes de headers ;
- contexte de requête ;
- session store ;
- factories d’identifiants ;
- événements techniques ;
- client d’observation ;
- transport abstrait.

### `@multiplanner/nucleus-angular`
Adaptateur pour **Angular 21 desktop** :
- provider ;
- service ;
- interceptor HTTP ;
- global error handler.

### `@multiplanner/nucleus-react-native`
Adaptateur pour **React Native + Expo mobile** :
- provider ;
- hooks ;
- wrapper `nucleusFetch` ;
- remontée des erreurs techniques.

## Règle d’architecture

```text
Nucleus frontend propage et observe côté client.
Nucleus backend corrèle, enrichit et expose l’observabilité serveur.
```

## Installation future dans les applications

### Desktop Angular
```ts
providers: [
  provideHttpClient(withInterceptors([nucleusHttpInterceptor])),
  provideNucleusFrontend({
    applicationCode: "multiplanner-desktop",
    consentVersion: "v1"
  })
]
```

### Mobile React Native / Expo
```tsx
<NucleusProvider config={{ applicationCode: "multiplanner-mobile", consentVersion: "v1" }}>
  <App />
</NucleusProvider>
```
