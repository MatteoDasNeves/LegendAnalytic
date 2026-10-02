# LegendAnalytics

Application Android (Kotlin, Jetpack Compose) pour consulter les parties récentes d'un joueur de
League of Legends, dans l'esprit d'op.gg / dpm.lol, à partir de l'API Riot et de Data Dragon.

- **Recherche** : Riot ID (`Pseudo#TAG`) + région, avec historique des recherches.
- **Profil** : icône, niveau, rangs Solo/Duo et Flex, dernières parties, pull-to-refresh et « Charger plus ».
- **Détail d'une partie** : les 10 joueurs, objectifs par équipe, accès au profil de chaque joueur.

Architecture MVVM + Repository (domain / data / ui), avec Ktor, Koin, Coil et Navigation 3.

**Lancer** : ajoutez votre clé de développement Riot (<https://developer.riotgames.com>, expire
toutes les 24 h) dans `local.properties` sous la forme `RIOT_API_KEY=RGAPI-...`, resynchronisez
Gradle puis lancez l'application depuis Android Studio (minSdk 26).
