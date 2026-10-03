# 09 — Licence & publication

> Résumé pratique, pas un avis juridique.

## Notre licence : All Rights Reserved (ARR) ✅ décidé

**Code et assets** du mod sont sous **All Rights Reserved** : © THEFricadelle, seul titulaire (projet perso). Personne ne peut copier, modifier ou redistribuer le mod sans autorisation.

### Ce qu'il faut mettre en place

| Où | Quoi |
|---|---|
| `gradle.properties` | `mod_license=All Rights Reserved` |
| `neoforge.mods.toml` | `license="All Rights Reserved"` (vient de `mod_license`) |
| `LICENSE` à la racine | Licence propriétaire à code fermé v3.0 (kit ARR), `LicenseRef-Create-Belgian-Snacks-ARR` : permission modpack §3(b), envoi aux joueurs d'un serveur §2.2, accès au code réservé aux contributeurs invités §5.1, droit français. Résumé : `NOTICE.md` |
| En-tête des fichiers Java | En-tête SPDX obligatoire sur chaque fichier source (`LicenseRef-Create-Belgian-Snacks-ARR`), à recopier sur tout nouveau fichier |
| Jar | `LICENSE`, `NOTICE.md` et `CONTRIBUTORS.md` copiés dans `META-INF/` |
| Page CurseForge/Modrinth | Licence « All Rights Reserved » + texte d'autorisation pour les modpacks |

### Points d'attention

1. **Projet perso** : THEFricadelle est le seul auteur, donc le seul titulaire. Si un jour quelqu'un d'autre fournit une texture ou du code, demander par écrit son accord pour l'inclure sous ARR.
2. **Intégration dans Arcadia** : en tant que titulaire, THEFricadelle autorise le pack à inclure le mod ; l'écrire simplement (ex. dans la description du mod ou un message à la team). **Autres modpacks** : ARR bloque par défaut la redistribution. Sur CurseForge, un modpack référence les mods par ID (il ne ré-héberge pas le jar), donc c'est compatible ; laisser activée l'option « distribution dans les modpacks » du projet. Si le mod est aussi sur Modrinth, écrire clairement sur la page si les autres modpacks peuvent l'inclure ou non.
3. **Repo GitHub** : un repo public ARR reste visible mais pas réutilisable. Si on veut que le code reste secret, repo **privé**.
4. **Code copié de Create** : Create est sous MIT pour le code. Si on en copie un morceau, on **doit** garder leur notice MIT (dans le fichier + `THIRD_PARTY_NOTICES.md`). Notre licence ARR ne s'applique pas à ces morceaux. → Le plus simple : **ne pas copier**, hériter/utiliser leur API.
5. **Assets de Create** : ARR aussi chez eux → interdit de les copier ou de les modifier.

## Et le nom « Create: … » ?

- La licence de Create n'impose rien sur le nom ; beaucoup d'addons utilisent le préfixe « Create: ». Aucune règle officielle de nommage trouvée.
- Bonnes pratiques : indiquer que c'est un **addon non officiel**, non affilié à la Create Team ; ne pas utiliser leur logo ; modid à nous (`create_belgian_snacks`).

## Publication

- ✅ Décidé (T4, 26/09/2026) : **public** sur CurseForge, Modrinth et GitHub Releases. Repo GitHub **public** (T5).
- Révisé (03/10/2026) : publication sur CurseForge et Modrinth seulement ; le repo passe en **privé** et la licence en code fermé (v3.0). Le texte de la licence est publié dans `THEFricadelle/mc-mods-issues` (`licenses/create-belgian-snacks/`), les bugs et demandes d'autorisation passent par ce même repo.
- Si public : catégories « Food », « Technology », « Create addon » ; dépendances NeoForge 21.1.x + Create (même version que le pack) ; compat optionnelle listée (JEI, Jade, KubeJS, Farmer's Delight…).
