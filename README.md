# LegendAnalytics

Application Android (Kotlin, Jetpack Compose) pour consulter les parties récentes d'un joueur de
League of Legends, dans l'esprit d'op.gg / dpm.lol.

- **Recherche** : Riot ID (`Pseudo#TAG`) + région, historique des recherches (DataStore).
- **Profil** : icône, niveau, rangs Solo/Duo et Flex, 20 dernières parties, pull-to-refresh, « Charger plus ».
- **Détail d'une partie** : les 10 joueurs, objectifs par équipe, clic sur un joueur pour ouvrir son profil.

## 1. Obtenir une clé API Riot

1. Connectez-vous sur <https://developer.riotgames.com> avec votre compte Riot.
2. Sur le tableau de bord, copiez la **Development API Key** (`RGAPI-xxxxxxxx-...`).
3. ⚠️ Une clé de développement **expire toutes les 24 h** : cliquez sur *Regenerate API Key* chaque jour.
   Elle est limitée à **20 requêtes/s et 100 requêtes/2 min**.
4. Pour une clé permanente (application publique), il faut faire une demande de *Personal* ou
   *Production API Key* depuis le même portail.

## 2. Configurer la clé dans le projet

La clé n'est **jamais** écrite dans le code versionné. Ajoutez-la dans `local.properties`, à la racine
du projet (ce fichier est déjà ignoré par Git) :

```properties
sdk.dir=C\:\\Users\\vous\\AppData\\Local\\Android\\Sdk
RIOT_API_KEY=RGAPI-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

Puis **resynchronisez Gradle** et relancez l'application : la clé est injectée à la compilation dans
`BuildConfig.RIOT_API_KEY` (voir `app/build.gradle.kts`). En CI, la variable d'environnement
`RIOT_API_KEY` est utilisée si la propriété est absente.

Messages d'erreur dans l'application :

| Message | Cause |
|---|---|
| « Aucune clé API configurée » | `RIOT_API_KEY` absente de `local.properties` |
| « Clé API Riot invalide ou expirée » | HTTP 401/403 : régénérez la clé puis recompilez |
| « Trop de requêtes » | HTTP 429 : limite de débit atteinte (le délai `Retry-After` est affiché) |

> Une clé embarquée dans un APK peut être extraite. C'est acceptable pour une clé de développement
> personnelle ; pour une application publiée, passez par un backend qui relaie les appels à Riot.

## 3. Lancer

- Android Studio (JDK 17+ ; le JBR fourni convient), SDK 37 installé.
- `./gradlew :app:assembleDebug` pour construire, `./gradlew :app:testDebugUnitTest` pour les tests.
- minSdk 26.

## Architecture

MVVM + Repository, en trois couches :

```
app/src/main/java/com/example/legendanalytics/
├── LegendApp.kt              # Application : Koin + ImageLoader Coil (client Ktor Data Dragon)
├── MainActivity.kt           # Edge-to-edge + thème + navigation
├── di/AppModule.kt           # Modules Koin (réseau, data, ViewModels)
├── domain/
│   ├── model/                # Region (routage), RiotId, Match, PlayerProfile, AppResult/AppError...
│   ├── repository/           # Interfaces des repositories
│   └── util/Stats.kt         # KDA, winrate, CS/min, ancienneté
├── data/
│   ├── remote/               # RiotApi, DataDragonApi (Ktor), DTO, gestion 429/erreurs
│   ├── local/                # Cache mémoire LRU des parties, historique DataStore
│   ├── mapper/DtoMappers.kt  # DTO -> modèles domaine
│   └── repository/           # Implémentations (semaphore pour les détails de parties)
└── ui/
    ├── navigation/           # Clés Navigation 3 (@Serializable NavKey) + NavDisplay
    ├── model/                # Modèles UI + mappers domaine -> UI (URLs Data Dragon)
    ├── common/               # UiState, composants, formatage, messages d'erreur
    ├── theme/                # Thème sombre, bleu victoire / rouge défaite
    ├── search/  profile/  match/   # Les 3 écrans et leurs ViewModels
```

### Routage Riot

| Région | Plateforme (summoner-v4, league-v4) | match-v5 | account-v1 |
|---|---|---|---|
| EUW, EUNE, TR, RU, ME | euw1, eun1, tr1, ru, me1 | europe | europe |
| NA, BR, LAN, LAS | na1, br1, la1, la2 | americas | americas |
| KR, JP | kr, jp1 | asia | asia |
| OCE, SG, TW, VN | oc1, sg2, tw2, vn2 | sea | **asia** (account-v1 n'est pas servi sur sea) |

### Respect des limites de débit

- Les détails de parties sont chargés en parallèle, limités à 4 requêtes simultanées (`Semaphore`).
- Les parties chargées sont gardées dans un cache LRU mémoire (300 parties) : ouvrir le détail d'une
  partie ou rafraîchir le profil ne recharge que les nouvelles.
- Un 429 est réessayé automatiquement (2 fois max) si `Retry-After` ≤ 10 s ; sinon l'erreur est affichée.
- Si seules quelques parties échouent, la liste s'affiche et un message indique combien manquent.

### Stack

Kotlin 2.4 · Compose BOM 2026.09 (Material 3) · Navigation 3 1.2 · Lifecycle 2.11 · Ktor 3.6 (OkHttp) ·
kotlinx.serialization 1.11 · Koin 4.2 · Coil 3.6 (`coil-network-ktor3`) · DataStore 1.2 · AGP 9.4.
