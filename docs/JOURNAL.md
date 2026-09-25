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

**Fait :** cahier des charges complet, dix sections, avec 9 exigences fonctionnelles (`EF1`–`EF9`), 5 exigences non fonctionnelles (`ENF1`–`ENF5`) et 14 règles de gestion (`RG1`–`RG14`), chacune sourcée sur une question `Qx` ou sur une hypothèse de la section 7. Trois diagrammes versionnés en texte : `D1-cas-utilisation.puml`, `D2-modele-donnees.mmd`, `D3-sequence-presence.mmd` (avec le cas nominal **et** les deux chemins d'erreur code expiré / déjà présent, alignés sur les codes HTTP du contrat). Backlog de 14 issues créées par script `gh` (9 Must, 3 Should, 2 Could), chacune avec un critère d'acceptation vérifiable et le renvoi à ses `EFx`/`RGx`. Contrat d'API complété : les 5 opérations imposées, plus 6 ajoutées. Commit `[JALON] analyse` poussé **avant** la moindre ligne de code.

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

**Fait (en cours — à compléter au `[JALON] v0.1`) :** les Must « socle » sont posés. Wrapper Maven 3.9.16 en type `only-script`, `pom.xml` en Spring Boot 4.1.1 / Java 17, `compose.yaml` PostgreSQL 16 sur le **port hôte 5433** (un PostgreSQL local occupe déjà 5432), profils dev (PostgreSQL, `ddl-auto: validate`) et test (H2 en mode PostgreSQL, Flyway désactivé — donc `./mvnw test` ne dépend ni de Docker ni d'une base locale). Schéma versionné : `V1__schema_initial.sql` (les 6 tables de D2, index sur chaque clé étrangère, et les contraintes d'unicité poussées en base plutôt que seulement dans les services) puis `V2__donnees_de_demonstration.sql` (1 promotion, 60 étudiants pour `ENF2`, une session ouverte `DEMO24`). Module `session/` complet en trois couches. Restent avant le jalon : `presence`, `exercice`, `relecture`, `tableau`.

**Bloqué :** ~1 h sur `./mvnw test`, qui échoue — 2 erreurs sur 4. Le contexte Spring ne démarre pas : `PresenceRepository`, `ExerciceRepository` et `RelectureRepository` étendent `JpaRepository` sur des classes encore vides et **non annotées `@Entity`**. Le premier bean en échec est `relectureRepository` (`Not a managed type: ...relecture.domain.Relecture`). Conséquence : `SessionTest` (règle métier pure, sans contexte) passe, mais `SessionControllerTest` tombe au chargement du contexte. Je le laisse ainsi pour l'instant — cela se résout en implémentant les modules concernés, et le message d'erreur restera un bon garde-fou tant qu'ils manquent.

**IA :** Claude, pour l'arborescence cible du backend et les fichiers du module `session/`.

**Comment j'ai vérifié.** Je n'ai pas pris les fichiers générés pour argent comptant : je les ai **diffés un par un contre ce que j'avais commité**, et trois choses ne collaient pas :

- `com.fasterxml.jackson.databind.ObjectMapper` → sur Spring Boot 4 c'est **Jackson 3**, donc `tools.jackson.databind.ObjectMapper`. Les annotations, elles, restent en `com.fasterxml.jackson.annotation`.
- `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` → en Boot 4 cette classe vit dans **`org.springframework.boot.webmvc.test.autoconfigure`**.
- Une coquille de méthode : `estClotureee()` → `estCloturee()`.

Les deux premières auraient été des erreurs de compilation que l'IA avait présentées comme du code fonctionnel. J'ai aussi corrigé trois de ses choix de nommage contre mon propre modèle : `RelectureDejaRenduException` → `RelectureDejaRendueException`, `ExerciceResponse` → `ExerciceDeposeResponse`, et `PromotionInconnueException` déplacée sous `common/referentiel/domain/` plutôt que dans `tableau/` (c'est une donnée partagée, pas un concept du tableau).

Enfin, j'ai fait tourner `./mvnw test` moi-même pour **observer** l'échec au lieu de me contenter du fait que `./mvnw compile` passait.

---

## Étape 3 — Enveloppe

**Fait :**

**Bloqué :**

**IA :**

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :**

---

## Étape 4 — Version finale

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 5 — Épreuve Git

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 6 — Soumission

**Fait :**

**Ce que je referais autrement avec une journée de plus :**
