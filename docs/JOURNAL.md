# Journal de bord — Binga Jehu Gabriel · 157

> Une entrée **par étape**, écrite **au moment où je la termine**, pas à la fin.
> Trois lignes suffisent. Un journal rédigé d'un bloc juste avant de soumettre se repère
> immédiatement dans l'historique Git et ne compte pas.

Chaque entrée répond aux trois mêmes questions :

- **Fait** — ce que je viens de terminer
- **Bloqué** — ce qui m'a coûté du temps, et combien
- **IA** — ce que je lui ai demandé, et **comment j'ai vérifié sa réponse**

---

## Étape 1 — Analyse et conception

**Fait :** cahier des charges complet, dix sections, avec 9 exigences fonctionnelles (`EF1`–`EF9`), 5 exigences non fonctionnelles (`ENF1`–`ENF5`) et 14 règles de gestion (`RG1`–`RG14`), chacune sourcée sur une question `Qx` ou sur une hypothèse de la section 7. Trois diagrammes versionnés en texte : `D1-cas-utilisation.puml`, `D2-modele-donnees.mmd`, `D3-sequence-presence.mmd` (avec le cas nominal **et** les deux chemins d'erreur code expiré / déjà présent, alignés sur les codes HTTP du contrat) — renommés en `.md` à l'étape 4 ; les noms d'origine restent ici parce que c'est ce qui a été produit ce jour-là. Backlog de 14 issues créées par script `gh` (9 Must, 3 Should, 2 Could), chacune avec un critère d'acceptation vérifiable et le renvoi à ses `EFx`/`RGx`. Contrat d'API complété : les 5 opérations imposées, plus 6 ajoutées. Commit `[JALON] analyse` poussé **avant** la moindre ligne de code.

**Bloqué :** ~40 min sur la section 7. Deux difficultés distinctes. D'abord la contradiction `Q10` / `Q15` : deux réponses à la même question, l'une permissive, l'autre définitive. Ensuite, une fois la section 7 écrite, sa relecture contre `CLIENT.md` a montré qu'elle laissait passer deux décisions non écrites (voir la ligne IA). J'ai aussi perdu du temps à vouloir modéliser « une relecture commencée » avant de constater qu'aucun état ne le permet dans D2.

**IA :** Claude, en trois demandes successives.

1. *Une ligne directrice pour la journée.* Il a proposé un budget par étape, signalé la contradiction Q10/Q15 en recommandant Q15, et désigné comme « trou » du sujet l'absence de réponse à : **faut-il avoir marqué sa présence pour déposer un exercice ?**
2. *La rédaction de la section 7.* Il a proposé cinq zones d'ombre et un tableau de contradictions.
3. *L'arborescence backend.* Première proposition **à plat** (entité/repository/service/controller), que j'ai refusée — ce n'est pas de la clean architecture. Deuxième proposition en trois couches `domain/` / `application/` / `infrastructure/`, conforme à ce que je voulais.

**Comment j'ai vérifié.** C'est le point qui a le plus servi :

- **Sur Q10/Q15 :** je suis retourné à `CLIENT.md` plutôt que de croire la recommandation. Q10 est une réponse technique ponctuelle, Q15 est formulée comme un principe assumé et justifié par le client lui-même (« c'est plus honnête pour tout le monde »). J'ai donc retenu Q15, et j'ai **exigé que la citation du client figure dans la justification** — un argument sans preuve ne se discute pas.
- **Sur la section 7 :** j'ai relu les 16 `Qx` une par une contre le tableau de l'IA. Sa version était incomplète sur trois points, et je les ai ajoutés :
  - **`Q13` contre `RG10`.** Q13 dit « tant que personne n'a commencé à le relire » ; `RG10` et le `PUT /api/exercices/{id}` disent « tant que le relecteur n'a pas rendu ». J'ai ouvert `StatutRelecture` pour trancher : il ne contient que `ASSIGNEE` et `RENDUE`, donc **la condition littérale de Q13 n'est pas observable** dans mon modèle. J'ai assoupli au rendu et je l'ai écrit au lieu de le laisser implicite.
  - **`RG8`.** C'est la seule règle qui exige un état serveur par étudiant (5 échecs ⇒ 2 min), alors qu'`ENF5` exclut toute session utilisateur. Ni le client ni l'IA ne disaient où ce compteur vit. J'ai tranché : en mémoire applicative, indexé par `etudiantId` **seul**, parce qu'un code inconnu (400 `CODE_INCONNU`) ne permet pas de retrouver la session visée — une clé `(etudiantId, sessionId)` était donc impossible.
  - **Le trou lui-même.** La section 7 listait sept zones d'ombre sans dire laquelle était *le* trou reproché par le sujet. Il est maintenant nommé en tête de section.
- **Conséquence sur le contrat (décision B).** Trancher `RG8` a fait apparaître que l'Annexe B ne prévoit **aucun statut** pour une tentative bloquée sur `POST /api/presences` (400/409/410 seulement). Sans statut, `RG8` n'est observable ni par le client ni par un test. J'ai ajouté `429 TROP_DE_TENTATIVES`, et j'ai **vérifié par comparaison automatique du contrat imposé et du mien** que c'est le seul écart : les quatre autres opérations imposées ont exactement les mêmes ensembles de statuts. L'écart est localisé, commenté à l'endroit exact, et documenté en section 7.
- **Sur l'arborescence :** j'ai confronté la liste de fichiers proposée à mes 14 issues pour vérifier que chaque critère d'acceptation tombait bien dans un module.

---

## Étape 2 — Première version

**Fait :** le socle d'abord — wrapper Maven 3.9.16 commité, `pom.xml` en Spring Boot 4.1.1 / Java 17, `compose.yaml` PostgreSQL 16 sur le **port hôte 5433** (un PostgreSQL local occupe 5432), profil dev (`ddl-auto: validate`) et profil test (H2 en mode PostgreSQL, Flyway désactivé — donc `./mvnw test` ne dépend ni de Docker ni d'une base locale). Puis le schéma versionné : `V1__schema_initial.sql` (les 6 tables de D2, index sur chaque clé étrangère, unicité `(session_id, etudiant_id)` portée par la base pour `ENF4`) et `V2__donnees_de_demonstration.sql` (1 promotion, 60 étudiants pour `ENF2`, une session ouverte `DEMO24`). Ensuite les tickets Must, un par branche, une par PR, une par issue fermée : `session/` (ouvrir, clôturer), `presence/` (les quatre refus, dont le blocage `429`), `referentiel/`, `exercice/` (déposer, remplacer le lien), `relecture/` (tirer le relecteur au sort, rendre une note verrouillée, la lire sans nommer le relecteur) et `tableau/` (quatre requêtes agrégées) — tous dans la découpe port/adapter posée à l'étape 1. Enfin le frontend React, à la place du `frontend/` vide : trois écrans et une couche d'appels API unique. Les **cinq opérations imposées** du contrat sont livrées, plus six ajoutées et documentées. `./mvnw test` passe en entier, **37 tests, contexte Spring compris** — ce qui n'était encore jamais arrivé : les quatre modules squelettes dont les dépôts Spring Data pointaient sur des classes non annotées `@Entity` bloquaient le démarrage. Commit `[JALON] v0.1` poussé dès le socle en place, ce qui a débloqué l'enveloppe.

**Bloqué :** ~50 min sur deux casses de `main`, que je n'ai pas vues venir. D'abord une classe référencée mais jamais commitée : mes vérifications locales passaient parce que l'arbre de travail contenait le fichier absent du dépôt, donc le défaut n'existait que sur la branche livrée. Ensuite un import manquant, dont l'échec a été masqué par ma propre commande : le `./mvnw` était enchaîné dans un pipe avec `grep`, donc le code de sortie retenu était celui de `grep`, pas celui de Maven. Un contrôle qui ne peut pas échouer n'est pas un contrôle. Les deux règles sont maintenant écrites dans `AGENTS.md` — vérifier la branche **telle qu'elle arrivera sur `main`**, et ne jamais laisser un `mvn` dans un pipe. S'y ajoute un coût que j'assume : le retrait du trailer `Codebuff` demandé a exigé une réécriture de l'historique et un `--force-with-lease` sur `main`, que le barème sanctionne (−5). Les arbres sont identiques avant et après — seuls les messages ont changé — mais le malus est consommé, et il est écrit ici plutôt que dissimulé.

**IA :** Claude, pour l'arborescence cible du backend, le code de chaque module, le frontend et la documentation.

1. *L'arborescence backend.* Deuxième proposition après un refus : la première était **à plat** (entité/repository/service/controller), la seconde en trois couches `domain/` / `application/` / `infrastructure/`. J'ai ensuite poussé la séparation plus loin que lui, en détachant le modèle du domaine de l'entité JPA.
2. *Le code des modules.* Il a produit les cas d'usage ; j'ai refusé trois de ses choix de conception — son `Random` en dur, remplacé par une interface `TirageAuSort` sans laquelle `RG2` restait invérifiable ; sa validation du lien d'exercice dans le service, descendue dans le domaine ; et l'ajout de `@Min`/`@Max` sur la note, qui aurait produit `REQUETE_INVALIDE` là où le contrat impose `NOTE_INVALIDE`.
3. *Le frontend et la documentation.* Les trois écrans, la couche d'appels API, et les premiers jetons du `README` et du `CHANGELOG`.

**Comment j'ai vérifié.** Je n'ai pas pris les fichiers générés pour argent comptant, je les ai **diffés un par un contre ce que je commitais**, et trois choses ne collaient pas :

- `com.fasterxml.jackson.databind.ObjectMapper` → sur Spring Boot 4 c'est **Jackson 3**, donc `tools.jackson.databind.ObjectMapper`. Les annotations, elles, restent en `com.fasterxml.jackson.annotation`.
- `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` → en Boot 4 cette classe vit dans **`org.springframework.boot.webmvc.test.autoconfigure`**.
- Une coquille de méthode : `estClotureee()` → `estCloturee()`.

Les deux premières auraient été des erreurs de compilation qu'il présentait comme du code fonctionnel. J'ai aussi corrigé trois de ses choix de nommage contre mon propre modèle : `RelectureDejaRenduException` → `RelectureDejaRendueException`, `ExerciceResponse` → `ExerciceDeposeResponse`, et `PromotionInconnueException` déplacée sous `common/referentiel/domain/` plutôt que dans `tableau/` — c'est une donnée partagée, pas un concept du tableau.

Enfin, j'ai fait tourner `./mvnw test` moi-même à chaque ticket pour **observer** le résultat, au lieu de me contenter du fait que `./mvnw compile` passait : c'est exactement le contrôle qui m'a manqué les deux fois où j'ai cassé `main`.

---

## Étape 3 — Enveloppe

> L'`enveloppe` m'a été remise **tardivement**, alors que le jalon `[JALON] v0.1` était poussé depuis
> longtemps et que les six modules tournaient. L'entrée ci-dessous est écrite **au moment où j'ai
> terminé l'étape**, pas avant : les deux sujets qu'elle porte sont traités, dans l'ordre qu'elle fixe.

**Fait :** les deux sujets de l'enveloppe, séparés comme elle l'exige — **deux branches, deux PR**.

*Le bug d'abord.* La phrase du client (« deux étudiants côte à côte, ils tapent le code presque en
même temps, un seul apparaît dans ma liste ; j'ai réessayé, cette fois les deux sont passés ») se
traduit par une **course entre la lecture et l'écriture** dans `PresenceService.marquer` : la
vérification « ce n'est pas déjà présent » et l'insertion ne sont pas atomiques. Reproduit par une
rafale de six envois simultanés du **même** `(code, etudiantId)` contre PostgreSQL : un `201`, cinq
`500 ERREUR_INTERNE`, et au journal `duplicate key value violates unique constraint
"uq_presence_session_etudiant"`. La base portait déjà le bon garde-fou (`ENF4`) ; ce qui manquait
était de **traduire son refus** en `409 DEJA_PRESENT`, le code que le contrat impose, au lieu de le
laisser remonter en erreur de stockage. Ordre tenu et lisible dans l'historique : issue `#78`
**avant** le premier commit de correction, puis commit du **test qui échoue** (deux tests), puis
commit du correctif — branche `fix/78-presence-simultanee`, PR `#80`.

*Le changement ensuite.* « Un seul relecteur ça ne marche pas » **retire** Q6 au lieu de s'y ajouter :
un exercice est désormais relu par **deux pairs distincts** tirés parmi les présents (jamais l'auteur,
jamais deux fois le même), et la note retenue est la **moyenne** des deux — celle du seul pair qui a
rendu si l'autre n'a pas rendu, marquée **provisoire**. L'analyse a été corrigée **d'abord et dans un
commit qui le dit** (`#81`, PR `#84`) : §2, §3, `EF5`, `EF7`, `RG4`, `RG10`, `RG12`, `RG14`, plus les
diagrammes devenus faux (`D2` : unicité `(exercice_id, relecteur_id)` et cardinalité `1..2` ; `D4` :
`RELU` à la **dernière** relecture). Puis la base et le code : migration **`V3` ajoutée, jamais
modifiée en place** (`uq_relecture_exercice` → `uq_relecture_exercice_relecteur`), le domaine (note
retenue calculée dans `NoteRetenue`, pas chez ses trois appelants), le contrat (`provisoire`), l'API et
le frontend (`#82` PR `#85`, `#83` PR `#86`).

**Sacrifice de périmètre, écrit et assumé :** le changement est un Must arrivé tard, donc quelque
chose sort. Le ticket `#79` (« les `500` ailleurs » — remplacer les erreurs de stockage restantes par
des codes du contrat) est **sacrifié** : commenté et expliqué sur l'issue, pas simplement oublié. Ce
qui ne sort **jamais**, dans l'ordre de sacrifice fixé depuis l'étape 1 : la conformité au contrat
(dont les cinq opérations imposées, intactes après le changement) et la clôture de session.

**Bloqué :** ~1 h 30 au total, sur trois choses distinctes. D'abord la **lecture** du bug : ma première
reproduction — deux étudiants **différents** en parallèle, ce que la phrase du client suggère — passait
sans broncher, y compris en rafale. C'est le double envoi du **même** étudiant qui casse, et il a fallu
comprendre que « côte à côte » n'était pas l'essentiel du symptôme : la course existe entre deux
requêtes, pas entre deux personnes. Ensuite, le backend : `spring-boot:run` **forke une JVM**, donc mon
`pkill` sur le motif de la commande Maven n'a tué que le parent et j'ai interrogé un processus périmé
— cinq minutes à croire que la correction ne marchait pas. Enfin un test : il « passait » pour de
mauvaises raisons, Mockito conservant une **référence** à la liste mutable passée au tirage, si bien
que les deux tirages se relisaient identiques après coup. C'est le test qui a fait corriger le code
(passer une copie au tirage), et non l'inverse.

**IA :** Claude, pour traduire le symptôme en cause, écrire le test qui échoue, la migration et le
domaine.

1. *Le diagnostic.* Il a d'abord écarté le module `presence/` (« rien de racy ici ») avant de
désigner la séquence lire-puis-écrire. Je ne l'ai pas cru : je l'ai fait reproduire par une rafale
   concurrente contre PostgreSQL, et c'est le journal de la base, pas son raisonnement, qui a nommé la
   contrainte violée.
2. *Le test et la correction.* Deux tests (le même étudiant deux fois ; l'exercice encore en attente
tant qu'une note manque), puis la traduction du refus de la base en `409`.
3. *Les deux relecteurs.* Le tirage du second, la note retenue, `provisoire`. Il a produit une première
   version qui confrontait l'exercice à `RELU` dès la **première** note rendue — refusée : `D4` révisé
   dit dernière relecture, et la note doit être disponible avant.

**Comment j'ai vérifié.** Le sujet dit que le jury ne cherche pas la correction mais la **preuve** :

- **Le bug.** Reproduction réelle avant correction (`201` × 1, `500` × 5, contrainte violée au
  journal), puis après (`201` × 1, `409` × 5, une seule ligne en base, aucune erreur interne). Un test
  qui échoue d'abord, deux tests au vert ensuite — et 49 tests dans le projet.
- **La migration.** Appliquée sur la base **déjà remplie** de ce poste, pas sur une base neuve : Flyway
  passe en v3, la relecture existante (`RENDUE`, note 15) est intacte, et la contrainte est bien
  devenue `(exercice_id, relecteur_id)`. Une migration qu'on n'essaie que sur du vide ne prouve rien.
- **Le changement, de bout en bout.** Session à quatre présents, dépôt de l'étudiant 1 : deux
  relectures assignées (relecteurs 2 et 3, l'auteur exclu). Première note `12` → note retenue `12.0`,
  `provisoire: true` ; seconde note `17` → **`14.5`**, `provisoire: false`, exercice `RELU` ; re-rendu →
  `409 RELECTURE_DEJA_RENDUE`. Et le cas du seul pair disponible : une seule relecture, `14.0`,
  **provisoire pour toujours** — décision écrite en §7 plutôt que subie.
- **Sur mes quatre arbitrages** (moyenne à deux décimales, un seul pair assigné, marque `provisoire`,
  sacrifice `#79`), je ne me suis pas contenté d'une réponse du modèle : ce sont des trous que les 16
  `Qx` de `CLIENT.md` ne couvrent pas. Une hypothèse silencieuse étant une faute, les quatre sont
  écrites en §7 du cahier des charges **avant** le code, et rappelées dans les PR.
- **Le parcours à l'écran**, enfin, piloté dans Chrome sans interface : la note s'affiche « 14/20 note
  provisoire — en attente du second relecteur » puis « 14.5/20 note définitive — moyenne des deux
  relecteurs », et rien n'est écrit dans le stockage du navigateur (Q1, ENF5).

**Après l'enveloppe :** la configuration locale est passée par un `.env` unique (ticket `#87`, PR
`#88`) — variables listées dans un `.env.example` commité, jamais de secret dans l'historique.

---

## Étape 4 — Version finale

**Fait :** jalon `[JALON] v1.0` posé sur `main` (le quatrième et dernier, après `depart`, `analyse` et `v0.1`), `README` réécrit **et suivi à la lettre** : `docker compose up -d`, `./mvnw spring-boot:run`, migrations `V1` et `V2` appliquées par Flyway, schéma validé par Hibernate (`ddl-auto: validate`), puis le parcours complet au `curl` — ouvrir une session, marquer deux présences, déposer un exercice, constater le relecteur tiré au sort, refuser une note de 25, rendre 15, la relire, afficher le tableau, clôturer, puis constater les refus après clôture. `CHANGELOG.md` aligné sur l'historique réel et `SOUMISSION.md` complété avec un hash relevé par `git rev-parse`. Une dernière règle de gestion a été livrée **après le jalon** : l'ajout manuel d'une présence par le formateur (`RG11`, Q14), avec l'opération correspondante ajoutée au contrat et sa raison écrite en §7.

**Bloqué :** ~30 min sur trois pièges, dont je sors trois règles. D'abord un démarrage impossible : le port 8080 de cette machine est tenu par un Keycloak déjà lancé, et Spring échouait sur un `BindException: Adresse déjà utilisée` enterré au quatrième `Caused by` — pas un message qu'on lit. J'ai vérifié sur `8081` et documenté la sortie dans le `README`, avec `VITE_API_TARGET` pour que le frontend suive sans que le code change. Ensuite mon premier essai de parcours complet a rendu quinze `000` : le backend lancé en arrière-plan appartenait à la commande précédente et avait été tué avec elle — une vérification de bout en bout ne vaut que si le processus vit pendant qu'on interroge. Enfin, j'ai inscrit dans `SOUMISSION.md` un hash **reconstitué de mémoire** au lieu d'être relevé ; je m'en suis aperçu avant de pousser, et un hash invalide rend la partie non corrigible.

**IA :** Claude, pour le `README`, le `CHANGELOG`, le frontend et une partie du code.

1. *Le `README` et le `CHANGELOG`.* Rédigés d'après l'historique réel ; je les ai ensuite confrontés à l'application, ce qui les a rendus faux sur deux points (voir ci-dessous).
2. *Les trois écrans.* La couche d'appels API, les états de chargement et d'erreur ; j'ai fait supprimer côté front tout calcul métier, pour que la moyenne du tableau vienne de l'API et non d'un recalcul local (`F3`).
3. *Rien pour cette étape sur les règles métier.* Le contrôle utile n'a pas porté sur ce qu'il produit mais sur ce qu'il **affirme** — et c'est là que j'ai trouvé l'écart.

**Comment j'ai vérifié.** Cette fois, c'est le code livré qui a été confronté aux documents, et pas l'inverse :

- **Sur le `README` :** je l'ai suivi à la lettre, comme le ferait un tiers sur un poste vierge. Sa section « ce qui fonctionne » annonçait `PUT /api/exercices/{id}` alors que l'opération était déclarée au contrat depuis le premier commit de code et **jamais implémentée** ; trois autres opérations annoncées étaient dans le même cas. Le contrat et le `README` mentaient — exactement ce qu'un correcteur trouve en trois minutes. Toutes sont livrées depuis.
- **Sur le contrat :** j'ai comparé le contrat imposé et le mien opération par opération, puis vérifié au `curl` que chaque code annoncé est bien celui qui sort : `400 CODE_INCONNU`, `409 DEJA_PRESENT`, `400 LIEN_INVALIDE`, `409 EXERCICE_DEJA_DEPOSE`, `400 NOTE_INVALIDE`, `409 RELECTURE_DEJA_RENDUE`, `404 PROMOTION_INCONNUE`, `409 SESSION_DEJA_CLOTUREE`. Le relecteur tiré au sort n'est jamais l'auteur, et la moyenne vaut `15.0` pour l'auteur relu contre `null` pour un étudiant sans note.
- **Sur les règles de gestion :** j'ai relu les 14 `RGx` une par une contre le code livré, en cherchant celles qui restaient « documentées mais absentes ». Une seule l'était : `RG11` (Q14), dont le champ `source` existait depuis `V1` sans qu'aucune opération ne le renseigne. C'est ce contrôle qui l'a fait sortir, et c'est lui qui a justifié la seule entorse du projet au contrat gelé.
- **Sur les documents :** cahier des charges, journal et `knowledge.md` décrivaient encore l'état d'avant — liste des livrables citant des `.puml`, section 7 sans les codes ajoutés en route. Corrigés dans un commit dédié, avec une version 1.5 puis 1.6 au journal des révisions.

---

## Étape 5 — Épreuve Git

**Fait :** rien : `git-lab.bundle` ne m'a pas été remis — cette fois ce n'est pas un retard, le bundle n'est jamais arrivé, contrairement à l'`enveloppe` de l'étape 3 qui a fini par m'être remise. Le dépôt `kfokam48-gitlab-157` n'a donc jamais été créé, et aucun exercice Git de l'épreuve n'a été exécuté.

**Bloqué :** l'étape entière, et les 17 points qui vont avec — c'est le plus gros poste perdu de la journée. Mes 12 points de discipline Git (une branche par ticket, une PR par branche, `Closes #<n°>`, branche supprimée, `main` remis à jour avant d'ouvrir la suivante) restent dans l'historique de mon propre dépôt, mais ils ne remplacent pas l'épreuve qui les notait sur le dépôt remis.

**IA :** rien : faire produire des commandes Git sans dépôt à interroger n'aurait rien vérifié du tout — et c'est justement le genre de livrable qui a l'air juste parce qu'il est bien écrit.

---

## Étape 6 — Soumission

**Fait :** le dossier de soumission est recomposé contre l'état **réel** de `main`, et non contre un état de mémoire : hash relevé par `git rev-parse`, puis vérifié par appel à l'API du dépôt — un hash que GitHub ne connaît pas rend la partie non corrigible, c'est la seule erreur de la journée qui ne se rattrape pas. Le fichier dit aussi ce qui est livré — les tickets de la fin (`#64`, `#66`, `#70`, `#72`), puis toute l'étape 3 (`#78`, `#81`–`#83`, `#87`) — et ce qui ne l'est pas. La relecture qui a suivi l'étape 3 a corrigé une affirmation devenue fausse : le dossier déclarait encore que l'enveloppe n'avait pas eu lieu. Restent les deux gestes qui ne sont pas les miens : le centre d'examen, et le téléversement.

**Ce que je referais autrement avec une journée de plus :**

- **Relire les documents après chaque livraison, pas le soir.** Deux relectures ont trouvé la même chose à des heures différentes : le `README` annonçait des opérations `PUT`/`GET` jamais implémentées (fin d'étape 4), et `knowledge.md` décrivait encore un projet d'avant sa première compilation, avec un `localStorage` et une route protégée qui n'ont jamais existé (relecture finale). Une phrase écrite le matin devient fausse sans prévenir, et c'est la seule dette qui ne se voit pas dans `git status`.
- **Réclamer les deux scripts tôt, et par écrit.** L'`enveloppe` de l'étape 3 a fini par arriver, **après** l'heure limite de téléversement : j'ai pu traiter le bug et le changement de besoin, mais dans l'ordre inverse de l'épreuve — le code était déjà écrit quand l'analyse a été corrigée. Les 17 points de l'épreuve Git, eux, ne sont pas partis du tout : le bundle n'est jamais arrivé. C'est la leçon la plus chère de la journée.
- **Appeler l'API comme un tiers, plus tôt.** L'écart le plus grave — des opérations annoncées au contrat et jamais implémentées — ne se voyait ni dans les tests, ni dans le build, ni dans une relecture du code : il ne se voyait qu'en passant les appels du `README` dans l'ordre, comme le ferait un correcteur pressé.
- **Ne pas réécrire l'historique Git.** Un `--force-with-lease` pour retirer le trailer `Codebuff` a consommé 5 points pour rien : les arbres étaient identiques avant et après, seuls les messages changeaient. Sur `main`, un historique imparfait vaut mieux qu'un historique réécrit.
