# kfokam48-epreuve-157 — Suivi de présence, dépôt d'exercices et relecture entre pairs

Application de suivi de présence en session de cours. Le formateur ouvre une session et obtient un
code de présence ; l'étudiant marque sa présence, dépose le lien de son exercice ; **deux** relecteurs
sont assignés automatiquement parmi les présents, et la note retenue est la moyenne des deux ; le
formateur consulte un tableau récapitulatif.

Le cœur du sujet n'est pas l'interface mais les règles métier : code qui expire, pas d'auto-relecture,
note verrouillée une fois rendue, clôture de session irréversible (cf. `docs/CAHIER_DES_CHARGES.md`).

**Frontend choisi : React (Vite, TypeScript)** — un seul build statique, trois écrans précédés d'un
écran de choix du rôle, qui partagent la même couche d'appels API, et un outillage déjà maîtrisé,
donc du temps investi dans le modèle métier plutôt que dans la configuration.

---

## Démarrage

**Prérequis** : Docker, JDK 17 ou plus (testé sur 21), Node 18 ou plus (testé sur 24). Le wrapper Maven
est fourni, rien d'autre à installer.

```bash
# 1. La base de données de développement (PostgreSQL 16, port hôte 5433)
docker compose up -d

# 2. L'API — les migrations Flyway s'appliquent au démarrage
cd backend && ./mvnw spring-boot:run

# 3. Le frontend, dans un second terminal
cd frontend && npm install && npm run dev
```

L'API écoute sur `http://localhost:8080`, le frontend sur `http://localhost:5173`. Le frontend appelle
`/api/...` sur sa propre origine : Vite proxifie vers le backend (`vite.config.ts`), donc aucune URL
d'API n'est écrite en dur et aucun CORS n'est nécessaire.

### Configuration locale : un seul `.env`

Aucun réglage n'est nécessaire pour démarrer. Pour adapter la configuration à votre poste, copiez le
modèle :

```bash
cp .env.example .env      # `.env` n'est jamais commité (voir .gitignore)
```

Ce fichier unique, à la racine, est lu par les trois outils — Spring Boot
(`spring.config.import`), Vite (`loadEnv`) et Docker Compose (nativement) : un `.env` par outil aurait
fini par les contredire. Les variables d'environnement gardent la priorité dessus, ce qui laisse la
ligne de commande utilisable ponctuellement.

| Variable | Défaut | Rôle |
|---|---|---|
| `DB_PORT` | `5433` | port hôte de PostgreSQL dans `compose.yaml` |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5433/epreuve`, `postgres`, `postgres` | connexion de l'API |
| `SERVER_PORT` | `8080` | port d'écoute de l'API |
| `VITE_API_TARGET` | `http://localhost:8080` | cible du proxy Vite, à faire suivre avec `SERVER_PORT` |

> Le port hôte de PostgreSQL est **5433**, pas 5432 : un PostgreSQL local occupe souvent 5432.
>
> Si le port **8080** est déjà pris sur votre machine (c'est fréquent : Keycloak, un autre Tomcat…),
l'API échoue au démarrage avec `BindException: Adresse déjà utilisée`. L'API est lancée depuis
`backend/`, où `../.env` désigne la racine du dépôt — il suffit donc d'écrire `SERVER_PORT=8081` et
`VITE_API_TARGET=http://localhost:8081` dans `.env`. La variable exportée à la main reste possible
pour un essai ponctuel :
>
> ```bash
> cd backend && SERVER_PORT=8081 ./mvnw spring-boot:run
> cd frontend && VITE_API_TARGET=http://localhost:8081 npm run dev
> ```

### Données de démonstration

Chargées automatiquement par la migration `V2__donnees_de_demonstration.sql` : une promotion
(identifiant **1**), 60 étudiants (ENF2), et une session ouverte dont le code est `DEMO24`.

Le parcours complet se fait donc sans rien créer à la main :

1. **Étudiant** — choisir un nom dans la liste, saisir un code de session pour marquer sa présence.
2. **Formateur** — ouvrir une session (le code s'affiche), puis consulter le tableau de la promotion.
3. **Relecteur** — choisir un nom, afficher ses relectures assignées, rendre une note de 0 à 20.

Pour vérifier l'API sans interface :

```bash
curl -X POST http://localhost:8080/api/sessions \
     -H 'Content-Type: application/json' \
     -d '{"titre":"Session démo","promotionId":1}'
# → 201 {"id":2,"code":"K7QM3P","ouvertureAt":"...","expirationAt":"..."}
```

### Vérifier l'installation

```bash
cd backend && ./mvnw test     # 58 tests, sans Docker ni PostgreSQL (H2 en mémoire)
cd frontend && npm run build  # vérification de types + build de production
```

Les tests backend tournent sur H2 avec Flyway désactivé : **`./mvnw test` ne demande ni Docker ni base
locale**, ce qui permet à un correcteur de valider le projet sur un poste vierge.

### Vérification de bout en bout (PostgreSQL réel)

Le parcours complet a été exécuté sur la vraie base, et non seulement en test — c'est ce qui valide
`V1`/`V2` contre les entités (`ddl-auto: validate`) et les codes de statut du contrat :

| Étape | Résultat observé |
|---|---|
| `POST /api/sessions` | `201 { id, code, ouvertureAt, expirationAt }` |
| `POST /api/presences` | `201` ; puis `409 DEJA_PRESENT` ; puis `400 CODE_INCONNU` |
| `POST /api/exercices` | `400 LIEN_INVALIDE`, puis `201 { statut: "EN_ATTENTE_RELECTURE" }` et `409 EXERCICE_DEJA_DEPOSE` |
| `GET /api/relectures/assignees` | le relecteur tiré au sort est un autre étudiant que l'auteur (RG2, RG5) |
| `POST /api/relectures/{id}` | `400 NOTE_INVALIDE` pour 25, `200` pour 15, puis `409 RELECTURE_DEJA_RENDUE` (RG3, RG12) |
| `GET /api/relectures/recues` | `200 { statut: "RENDUE", note: 15, commentaire: … }` — sans le nom du relecteur (RG6) |
| `GET /api/tableau` | `moyenne: 15.0` pour l'auteur relu, `moyenne: null` pour un étudiant sans note |
| `POST /api/sessions/{id}/cloture` | `200` ; puis `409 SESSION_DEJA_CLOTUREE` sur toute présence ou dépôt (RG13) |
| `POST /api/presences` × 6, simultanés, même étudiant | `201` × 1 et `409 DEJA_PRESENT` × 5 — jamais `500` (correctif `#78`) |
| Dépôt avec quatre présents | deux relectures assignées, l'auteur exclu, deux relecteurs distincts (RG4 révisée) |
| Première note rendue `12` | `GET /api/relectures/recues` → `{ note: 12.0, provisoire: true }` |
| Seconde note rendue `17` | `{ note: 14.5, provisoire: false }` et l'exercice passe `RELU` |
| Migration `V3` | appliquée sur une base **déjà remplie** : relecture existante intacte, contrainte devenue `(exercice_id, relecteur_id)` |

---

## Ce qui fonctionne

L'API impose cinq opérations (`api/contrat.yaml`) ; les cinq sont implémentées et respectent les
chemins, verbes, codes de statut et le format d'erreur `{ code, message }`.

| Opération | Rôle | Règles |
|---|---|---|
| `POST /api/sessions` | ouvrir une session, obtenir un code | EF2, RG1 |
| `POST /api/presences` | marquer sa présence avec le code | EF1, RG1, RG7, RG8 |
| `POST /api/exercices` | déposer le lien de son exercice | EF3, RG9, RG13 |
| `POST /api/relectures/{id}` | rendre note et commentaire | EF6, RG3, RG12 |
| `GET /api/tableau?promotionId=` | tableau récapitulatif du formateur | EF9, RG14, ENF2 |

Opérations ajoutées, documentées dans le contrat et en §7 du cahier des charges :

| Opération | Pourquoi elle est nécessaire |
|---|---|
| `POST /api/sessions/{id}/cloture` | sans clôture, RG13 / Q3 / Q10 / Q11 / Q12 / Q15 sont inapplicables — aucune opération ne la prévoyait |
| `PUT /api/exercices/{id}` | EF4 / RG10 : remplacer le lien tant que la relecture n'est pas rendue |
| `GET /api/promotions`, `GET /api/etudiants` | le formateur doit choisir une promotion (EF2), l'étudiant s'identifie en se choisissant dans une liste (Q1) |
| `GET /api/relectures/assignees` | c'est la seule façon pour le relecteur de connaître l'identifiant à passer au `POST` imposé |
| `GET /api/relectures/recues` | EF7 / RG6 : le contrat permettait d'écrire une note, pas de la lire |
| `POST /api/presences/formateur` | RG11 / Q14 : le formateur ajoute une présence à la main (souci de téléphone), marquée `source = FORMATEUR` pour que l'ajout se voie |

**Deux relecteurs par exercice** (`#81`–`#83`) : le client a retiré Q6 (« un seul relecteur ça ne marche
pas : quand il ne rend rien, l'étudiant n'a aucune note »). Chaque exercice est relu par deux pairs
distincts tirés parmi les présents, jamais l'auteur, et la **note retenue** est la moyenne des deux ;
celle du seul pair qui a rendu reste affichée, marquée **provisoire** (`provisoire: true` sur
`GET /api/relectures/recues`, et l'écran étudiant écrit le mot). L'exercice n'est `RELU` qu'à la
dernière note. La migration `V3` est **ajoutée**, jamais substituée à `V2`. Détail et décisions en §7.

**Pagination** (`#70`) : `page` et `taille` sont acceptés en paramètres **optionnels** sur les quatre
lectures (`/api/tableau`, `/api/etudiants`, `/api/promotions`, `/api/relectures/assignees`). Un appel
sans paramètre rend la **collection entière** — c'est le comportement imposé par le contrat, `GET
/api/tableau` comprise ; le total avant découpage part dans l'en-tête **`X-Total-Count`**, jamais dans
une enveloppe de réponse, puisque le contrat impose un tableau JSON. Hors bornes (`page < 1`, `taille`
hors de 1 à 100) : `400 REQUETE_INVALIDE`.

Le frontend (F2) s'ouvre sur un **écran d'accueil qui fait choisir son rôle** — formateur, étudiant
ou relecteur — puis une **barre latérale qui liste les fonctionnalités de ce rôle**, avec un retour au
choix du rôle (`#66`, `#72`). Le rôle vit en mémoire de la page : rien n'est écrit dans le stockage du
navigateur (Q1, ENF5). Les trois écrans ensuite : formateur (ouvrir, clôturer, tableau), étudiant
(présence, dépôt, note reçue), relecteur (relectures à rendre). Toutes les listes longues sont
découpées par un contrôle unique, masqué tant qu'il n'y a qu'une page. Tous les `fetch` sont dans
`frontend/src/api/client.ts` et aucun calcul métier n'est dupliqué côté front : la moyenne affichée
vient de `GET /api/tableau` (F3).

**N'est pas livré** — annoncé plutôt que découvert à la correction :

- **Aucune authentification** (Q1, ENF5) : l'identification se fait en choisissant un nom dans une
  liste, et un `etudiantId` transmis par le client n'est pas vérifié côté serveur. Hors périmètre
  assumé.
- Pas de notification, pas d'export du tableau, pas de gestion de plusieurs formateurs (cf. §3 du
  cahier des charges).

---

## Tests

| Test | Ce qu'il prouve |
|---|---|
| `SessionTest` | RG1 et RG13 : fenêtre de validité et état de clôture, **sans base ni contexte Spring** |
| `ExerciceTest` | la validité du lien d'exercice (400 `LIEN_INVALIDE`) |
| `RelectureTest` | RG3 (note entière de 0 à 20) et RG12 (une relecture rendue ne se modifie plus) |
| `RelectureServiceTest` | RG2 et RG5 : l'auteur n'est jamais candidat, **deux pairs distincts** sont tirés parmi les présents, et l'exercice reste en attente tant qu'une note manque |
| `NoteRetenueTest` | RG4 révisée : la note retenue est la moyenne des deux, `provisoire` tant que le second n'a pas rendu, et aucune note n'est inventée quand rien n'est rendu |
| `RelectureResponseTest` | RG6 : le DTO de sortie ne peut pas transporter l'identité du relecteur |
| `PresenceServiceTest` | EF1 / RG7 / RG8 : les refus de la présence, dont le blocage après cinq échecs |
| `ReferentielServiceTest` | Q1 : les listes d'identification, découpées par promotion |
| `TableauServiceTest` | EF9 / ENF2 : requêtes agrégées, moyenne absente plutôt que nulle, et agrégats restreints à la page demandée |
| `PageDemandeeTest` | `#70` : un appel sans paramètre ne rend **pas** une page de 20 lignes — le contrat imposé que ce test protège |
| `SessionControllerTest` | l'endpoint imposé, du HTTP jusqu'à la base, format d'erreur compris |
| `GlobalExceptionHandlerTest` | B4 : `{ code, message }` pour toute erreur, sans fuite technique (ENF3) |
| `SessionMapperTest`, `SessionRepositoryAdapterTest` | l'aller-retour modèle ↔ entité ne perd aucun champ, et la persistance tient à travers le port |

**14 classes, 58 tests.** B6 est couvert au-delà du minimum : les règles métier se testent **sans
contexte Spring**, et un test part du contrôleur pour aller jusqu'à la base.

---

## Architecture

```
backend/src/main/java/com/kfokam48/epreuve/
├── common/error/          erreurs centralisées, format { code, message } imposé (B4)
├── common/referentiel/    Promotion, Étudiant — données partagées
├── session/               ouvrir / clôturer une session
├── presence/              marquer sa présence
├── exercice/              déposer, remplacer le lien
├── relecture/             assigner (RG5), rendre une note (RG12), la lire
└── tableau/               lecture agrégée pour le formateur
```

Chaque module suit la même découpe, et c'est le gabarit à répliquer :

- `domain/model/` — le modèle métier, **sans aucune annotation de persistance** ; les règles y vivent
  en méthodes (`estExpiree()`, `rendre(note)`, `verifierLien(lien)`) et se testent sans base ;
- `domain/` — le **port** de persistance, qui n'étend pas `JpaRepository` ;
- `application/` — les cas d'usage, avec leurs DTO ;
- `infrastructure/` — le contrôleur, et `infrastructure/persistence/` avec l'entité JPA, le dépôt
  Spring Data, le mapper et l'adaptateur.

Deux conséquences directes de ce découpage : **le port ne rend que des modèles**, donc renvoyer une
entité JPA en JSON (interdit par B3) est impossible par construction ; et les règles métier se testent
sans base, ce qui explique le nombre de tests unitaires ci-dessus.

Dépendances entre modules : `application/` peut appeler l'`application/` ou le `domain/` d'un autre
module (`ExerciceService` demande le tirage au sort à `RelectureService`), mais jamais son
`infrastructure/`.

Le schéma est versionné par Flyway (`V1__schema_initial.sql`, `V2__donnees_de_demonstration.sql`,
`V3__deux_relecteurs_par_exercice.sql`) ; `ddl-auto` vaut `validate` hors tests, et `create-drop` dans
le profil `test`. Une migration déjà appliquée n'est jamais modifiée en place : le changement de besoin
de l'étape 3 en a ajouté une, et la base existante y a survécu.

---

## Documentation

| Fichier | Contenu |
|---|---|
| `docs/CAHIER_DES_CHARGES.md` | le besoin, les exigences `EF`/`ENF`, les règles `RG`, et la section 7 des zones d'ombre |
| `docs/diagrammes/D1` à `D4` | cas d'utilisation, modèle de données, séquence de présence, états-transitions d'un exercice (Mermaid) |
| `docs/JOURNAL.md` | le journal de bord, une entrée par étape |
| `api/contrat.yaml` | le contrat d'API, les 5 opérations imposées plus les ajouts |
| `CHANGELOG.md` | ce qui a été livré, étape par étape |
| `SOUMISSION.md` | le dossier de soumission (candidat, dépôts, hash) |
| `knowledge.md` | le contexte technique du dépôt : conventions, pièges, état réel |
