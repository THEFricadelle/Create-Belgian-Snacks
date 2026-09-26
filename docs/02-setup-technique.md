# 02 — Setup technique

## Prérequis sur le poste

| Outil | Version | Note |
|---|---|---|
| JDK | **21** (Eclipse Temurin) | `java -version` doit afficher 21 |
| Git | récent | |
| VSCode | récent | |
| Extensions VSCode | *Extension Pack for Java* (Microsoft), *Gradle for Java* | Optionnel : *Mermaid preview* pour les schémas des docs |

## Projet (en place depuis M0)

Le projet part du MDK officiel **ModDevGradle 1.21.1** (<https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle>), à jour au 26/09/2026 : ModDevGradle **2.0.147**, Gradle **9.2.1**, Parchment `2024.11.17`.

- **Wrapper** : `gradle-wrapper.jar` régénéré depuis la distribution 9.2.1, hash vérifié contre gradle.org (`423cb469…`), `distributionSha256Sum` épinglé. Le jar fourni par le MDK était celui de la 8.14.x : ne pas le reprendre.
- **Versions** : toutes dans `gradle.properties`. `create_version=6.0.10-280` est le build dont le `Git-Hash` (`ac0c444`, tag `mc1.21.1-6.0.10`) correspond au jar livré dans Arcadia ; le build 281 est un commit plus récent. Registrate, Ponder et Flywheel sont les versions embarquées par Create 6.0.10 (`META-INF/jarjar` du jar du pack).
- **Jars JEI et Jade** : sha1 identiques entre le Maven et les jars du pack (vérifié le 26/09/2026).

## Dépendances Gradle (`build.gradle`)

Chaque dépôt tiers est restreint aux groupes qu'il sert (`exclusiveContent`) :

| Dépôt | Groupes |
|---|---|
| `maven.createmod.net` | `com.simibubi.create`, `net.createmod.ponder`, `dev.engine-room.flywheel` |
| `maven.ithundxr.dev/snapshots` | `com.tterrag.registrate` (version figée, pas de `-SNAPSHOT`) |
| `maven.blamejared.com` | `mezz.jei`, `net.mezzdev.config` (dépendance de JEI 19.5x) |
| `api.modrinth.com/maven` | `maven.modrinth` |

| Dépendance | Configuration | Remarque |
|---|---|---|
| Create `:slim` | `implementation`, non transitif | pas de jar-in-jar : Create installé à côté fournit déjà ses libs |
| Ponder, Registrate | `implementation` | |
| Flywheel | API en `compileOnly`, mod en `runtimeOnly` | |
| JEI | API en `compileOnly`, mod en `localRuntime` | `localRuntime` = runtime de dev, jamais publié comme dépendance |
| Jade | `localRuntime` (`maven.modrinth:jade:15.10.6+neoforge`) | passera en `compileOnly` pour l'API au M3 |
| KubeJS, FD, S&D, Central Kitchen | aucune en M0 | à ajouter en `localRuntime` seulement, jamais en `implementation` |

Ajouter un groupe à un dépôt : vérifier d'abord que l'artefact y est réellement publié, puis l'ajouter au filtre.

> Arcadia utilise **JEI** : c'est le seul recipe viewer intégré en v1.

## Tester dans l'environnement Arcadia

Le runtime de dev ne contiendra jamais les 464 mods. Pour les tests de compat réels :
1. `./gradlew build` → copier `build/libs/create_belgian_snacks-x.y.z.jar` dans le dossier `mods/` d'une **copie** de l'instance Arcadia V2 (CurseForge App → Profil → *Open folder*).
2. Lancer l'instance, puis `/belgiansnacks foods export`.
3. Pour un serveur : même chose sur une copie du serveur Arcadia.

## `neoforge.mods.toml` (extrait)

Le modèle vit dans `src/main/templates/META-INF/neoforge.mods.toml`, rempli par la tâche `generateModMetadata` depuis `gradle.properties`.

```toml
[[dependencies.${mod_id}]]
    modId="create"
    type="required"
    versionRange="${create_version_range}"
    ordering="AFTER"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="farmersdelight"
    type="optional"
    versionRange="*"
    ordering="AFTER"
    side="BOTH"
```

Même bloc `optional` pour `jei`, `jade`, `kubejs`, `sliceanddice` (modid vérifiés dans les jars du pack le 26/09/2026).

## Datagen

Dans `build.gradle`, le run `data` (dossier de jeu `run/data`) a :

```groovy
programArguments.addAll '--mod', project.mod_id, '--all',
    '--output', file('src/generated/resources/').getAbsolutePath(),
    '--existing', file('src/main/resources/').getAbsolutePath(),
    '--existing-mod', 'create'
```

Et `sourceSets.main.resources { srcDir 'src/generated/resources' }` (le dossier `.cache` est exclu du jar et de git).

- `en_us` : généré par Registrate. Tout appel à `REGISTRATE.addRawLang` se fait **à la construction du mod**, jamais dans un listener `GatherDataEvent` (voir `ERROR_LOG.md`).
- `fr_fr` : `data/BSFrenchLangProvider` (Registrate ne gère qu'une langue).
- Registrate génère aussi `en_ud` (anglais à l'envers) : fichier attendu, à laisser.

## Debug dans VSCode

Le plus fiable : lancer `./gradlew runClient --debug-jvm` dans le terminal, puis attacher le debugger. `.vscode/launch.json` :

```json
{
  "version": "0.2.0",
  "configurations": [
    { "type": "java", "name": "Attach to Minecraft", "request": "attach", "hostName": "localhost", "port": 5005 }
  ]
}
```

## Référence locale des sources de Create

Pour vérifier les API de Create avant de les utiliser :

```bash
mkdir -p ../refs && git clone --depth 1 -b mc1.21.1/dev https://github.com/Creators-of-Create/Create ../refs/Create
```

Idéalement, faire un checkout du tag/commit correspondant à 6.0.10.

## Recommandé côté repo

- `.gitignore` du MDK + `run/`, `../refs/`.
- CI GitHub Actions : `.github/workflows/build.yml`, `./gradlew build` sur JDK 21, actions épinglées par SHA de commit (M0).
