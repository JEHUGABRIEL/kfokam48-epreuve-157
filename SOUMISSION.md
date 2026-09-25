# Soumission — Épreuve finale fullstack KFOKAM48


## Candidat

| | |
|---|---|
| Nom et prénom(s) | Binga Jehu Gabriel |
| Matricule |157
| Compte GitHub | JEHUGABRIEL |

## Projet

| | |
|---|---|
| Dépôt (public) | `https://github.com/JEHUGABRIEL/kfokam48-epreuve-157` |
| Commit final — hash complet, 40 caractères | `950f102ca99638404dc31dd62deec01a7b2f18bd` |
| Branche | `main` |

Le hash déclaré est celui du commit de merge qui précède immédiatement l'écriture de ces lignes. Il
contient l'intégralité du travail : les six modules backend, le frontend — écran de choix du rôle, barre
latérale de fonctionnalités, pagination —, l'analyse corrigée, le journal, et les tickets livrés après le
premier relevé : le correctif des listes déroulantes (`#64`), la barre latérale des rôles (`#66`), la
pagination des lectures (`#70`), l'écran d'accueil (`#72`), **puis l'étape 3 entière** — le correctif
des présences simultanées (`#78`), le passage à **deux relecteurs** avec note retenue (`#81`–`#83`) et
la configuration locale par `.env` (`#87`).

**Aucun changement n'est postérieur à l'inscription de ce hash-ci** — un fichier ne peut pas contenir le
hash du commit qui le porte. Le commit qui écrit ces lignes vient donc après, forcément : il ne touche
que ce dossier, et c'est le seul.

## Épreuve Git — étape 5

| | |
|---|---|
| Dépôt (public) | `https://github.com/JEHUGABRIEL/kfokam48-gitlab-157` |
| Commit final — hash complet | **non applicable** : `git-lab.bundle` ne m'a pas été remis |

Le dépôt `kfokam48-gitlab-157` n'a **pas été créé** et l'épreuve Git n'a pas été traitée : le bundle
`git-lab.bundle` est l'un des deux éléments « remis pendant l'épreuve » — l'autre, l'`enveloppe` de
l'étape 3, m'a été remise tardivement, et cette étape-là a donc été traitée. C'est écrit dans
`docs/JOURNAL.md` plutôt que passé sous silence.

## Technique

| | |
|---|---|
| Frontend utilisé | React 18 + Vite + TypeScript |
| Base de données | PostgreSQL 16, schéma versionné par Flyway (`V1`, `V2`, `V3`), `ddl-auto: validate` |
| Configuration locale | un seul `.env` à la racine, non commité, décrit par `.env.example` (`#87`) |
| Commandes de démarrage | `docker compose up -d` · `cd backend && ./mvnw spring-boot:run` · `cd frontend && npm install && npm run dev` |

---

## Ce que j'ai livré

**Ce qui fonctionne** — les **cinq opérations imposées** du contrat, avec leurs codes de statut exacts :

- `POST /api/sessions` → `201` avec un code valable 15 minutes (EF2, RG1) ;
- `POST /api/presences` → `201`, `400 CODE_INCONNU`, `409 DEJA_PRESENT`, `410 CODE_EXPIRE`, plus `429`
  `TROP_DE_TENTATIVES` (EF1, RG7, RG8, extension documentée en §7) ;
- `POST /api/exercices` → `201`, `400 LIEN_INVALIDE`, `409 EXERCICE_DEJA_DEPOSE` (EF3, RG9) ;
- `POST /api/relectures/{id}` → `200`, `400 NOTE_INVALIDE`, `403 AUTO_RELECTURE`, `409
  RELECTURE_DEJA_RENDUE` (EF6, RG3, RG12) ;
- `GET /api/tableau?promotionId=` → `200`, `404 PROMOTION_INCONNUE` (EF9, RG14, ENF2).

Plus l'assignation automatique de **deux** relecteurs par tirage au sort parmi les présents, jamais
l'auteur, jamais deux fois le même, et la note retenue — la moyenne des deux, ou celle du seul pair qui
a rendu, alors marquée **provisoire** (`#81`–`#83`) ; la clôture de session qui verrouille toute écriture (EF8, RG13) ; la note reçue
sans l'identité du relecteur (EF7, RG6) ; le remplacement du lien (EF4, RG10) ; l'ajout manuel d'une
présence par le formateur, marqué `source = FORMATEUR` (RG11, Q14) ; les listes d'identification (Q1) ;
et la pagination (`#70`) — `page` et `taille` en paramètres **optionnels** sur les quatre lectures, la
réponse restant un tableau JSON et le total partant dans l'en-tête `X-Total-Count`. Un appel sans
paramètre rend toujours la collection entière : c'est le contrat imposé, et il n'a pas bougé. Le tout
est vérifié sur PostgreSQL réel, pas seulement en test : les quinze appels du parcours complet rendent
exactement les statuts imposés.

**Frontend** : un écran d'accueil qui fait choisir son rôle **avant** d'ouvrir une interface, puis une
barre latérale qui liste les fonctionnalités de ce rôle (`#72`) ; trois écrans (formateur, étudiant,
relecteur) ; une couche d'appels API unique ; un seul composant de liste déroulante, qui annonce
toujours son état plutôt que de rester vide ; un seul contrôle de pagination, masqué tant qu'il n'y a
qu'une page ; chargements et erreurs gérés au même endroit ; aucun calcul métier dupliqué (F3).

**Ce qui ne fonctionne pas / n'est pas livré, et pourquoi :**

- Aucune authentification (Q1, ENF5) : identification par liste, sans vérification serveur.
- Aucun test automatisé sur le frontend : les écrans ont été vérifiés en les construisant
  (`npm run build`), en pilotant **Chrome sans interface** sur le parcours réel (accueil → clic sur un
  rôle → barre latérale → fonctionnalité → retour au choix du rôle), et en exerçant l'API au `curl` à
  travers le proxy de Vite. Ce n'est pas un test d'interface, c'est une vérification manuelle — la
  différence compte, et elle est écrite ici plutôt que sous-entendue.
- L'étape 3 (enveloppe) a été traitée **tardivement**, après l'heure limite de téléversement : le bug
  et le changement de besoin sont livrés, mais le code existait déjà quand l'analyse a été corrigée —
  l'ordre prévu (analyse revue avant le code) n'a pas pu être tenu.
- **Périmètre sacrifié** pour absorber le changement de besoin : le ticket `#79` (remplacer les
  dernières erreurs de stockage par des codes du contrat). Écrit et commenté sur l'issue. Ce qui n'a
  jamais été sacrifié : la conformité au contrat imposé et la clôture de session.
- L'étape 5 (épreuve Git) n'a pas eu lieu : bundle non remis.
- Pas d'export du tableau, pas de notification.

---

## Avant de téléverser, vérifie

- [x] Le dépôt est **public** et s'ouvre en navigation privée (vérifié : `visibility: PUBLIC`)
- [x] Le hash fait bien **40 caractères** et existe sur GitHub (`950f102ca99638404dc31dd62deec01a7b2f18bd`, relevé par `git rev-parse` **et** vérifié par appel à l'API du dépôt)
- [x] Tout le travail est **poussé** — `git status` propre, `main` = `origin/main`
- [x] Le `README` a été suivi à la lettre depuis le dépôt (parcours complet rejoué au `curl`)
- [x] `JOURNAL.md` et le cahier des charges sont dans `docs/`
- [x] Les **quatre** commits `[JALON]` sont poussés et dans l'ordre (`depart`, `analyse`, `v0.1`, `v1.0`)
- [ ] **Indiquer le centre d'examen ci-dessus** (le matricule est renseigné : `157`)
- [ ] **Téléverser ce fichier sur la plateforme avant 18h00**

---

**Déclaration.** J'ai réalisé ce travail seul. Les outils d'IA étaient autorisés sans restriction et je
les ai utilisés ; mon journal indique où et comment j'ai vérifié leurs réponses. Mes dépôts resteront
publics et inchangés jusqu'à la publication des résultats.

Signature : ______________________  Date : 25.09.2026
