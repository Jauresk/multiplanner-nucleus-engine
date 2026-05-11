# Architecture — Nucleus Frontend SDK

## Décision

Le Nucleus frontend est un **projet séparé** du Nucleus backend.

```text
nucleus/
  nucleus-core/
  nucleus-observability/

nucleus-frontend-sdk/
  packages/
    nucleus-frontend-core/
    nucleus-angular/
    nucleus-react-native/
```

## Pourquoi trois packages

### `nucleus-frontend-core`
Le cœur ne dépend ni d’Angular ni de React Native.  
Il contient les règles communes de propagation, de corrélation et de mesure.

### `nucleus-angular`
Le desktop utilise Angular 21.  
L’adaptateur y apporte :
- un interceptor HTTP ;
- un ErrorHandler global ;
- un service injectable ;
- des providers Angular.

### `nucleus-react-native`
Le mobile utilise React Native avec Expo.  
L’adaptateur y apporte :
- un provider React ;
- des hooks ;
- un wrapper `fetch` ;
- une remontée d’erreurs adaptée au mobile.

## Ce que ce projet fait

- Générer ou propager le `X-Correlation-Id`.
- Maintenir le `X-Session-Id`.
- Propager `X-Consent-Version`.
- Lire `X-Request-Timing` si le backend le renvoie.
- Mesurer la latence client des appels.
- Remonter des erreurs techniques frontend.

## Ce que ce projet ne fait pas

- Il ne calcule pas les recommandations.
- Il ne remplace pas `pertinence-frontend-sdk`.
- Il ne contient pas les signaux comportementaux métier comme `CLICK`, `SAVE`, `PLAN`.
- Il ne contient pas l’observabilité backend.
