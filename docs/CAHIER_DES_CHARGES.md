# Cahier des charges — Épreuve finale fullstack KFOKAM48

**Auteur :** Binga Jehu Gabriel · 157
**Version :** 1.4 · **Date :** 25.09.2026
**Frontend choisi :** React (Vite, TypeScript), pour la cohérence avec l'écosystème du candidat et la rapidité de mise en place d'une SPA sur trois écrans distincts.

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 gère des sessions de cours où les étudiants doivent, à chaque séance, prouver leur présence, déposer un exercice pratique, et évaluer le travail d'un pair. Aujourd'hui ce suivi n'existe qu'au travers d'échanges informels et de tableurs tenus à la main par le formateur, ce qui rend difficile de savoir, à tout moment, qui a été présent, qui a rendu son exercice, et qui doit encore relire celui d'un camarade. L'application répond à ce besoin de traçabilité : elle donne au formateur un code de présence à distribuer en session, permet à l'étudiant de marquer sa présence et déposer son exercice depuis son téléphone, assigne automatiquement un relecteur parmi les étudiants présents, et restitue au formateur un tableau de bord fiable par étudiant. L'enjeu n'est pas la beauté de l'interface mais la solidité des règles métier : un code qui expire vraiment, une relecture qui ne peut être faite par l'auteur, une note qui se verrouille une fois rendue.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| Formateur | Ouvrir une session, obtenir un code, clôturer une session, ajouter une présence manuellement, consulter le tableau récapitulatif | Relire un exercice, déposer un exercice, voir l'identité des relecteurs assignés |
| Étudiant | Marquer sa présence, déposer/remplacer le lien de son exercice, consulter sa propre note et le commentaire reçu | Relire son propre exercice, voir qui l'a relu, modifier une relecture rendue |
| Relecteur | Rendre une note et un commentaire sur l'exercice qui lui est assigné | Choisir l'exercice qu'il relit, relire son propre exercice, modifier sa relecture une fois rendue |

Le relecteur **n'est pas un acteur distinct** : c'est un étudiant assigné dynamiquement à ce rôle sur un exercice donné, tiré au sort par le système au moment du dépôt de l'exercice, parmi les étudiants déjà présents à la session (RG5). Cette décision a des conséquences directes sur le modèle de données : il n'existe pas d'entité `Relecteur`, mais une entité `Relecture` qui porte une référence `relecteurId` vers un `Etudiant`.

## 3. Périmètre

**Inclus dans cette version :**
- Ouverture de session et génération de code de présence par le formateur
- Marquage de présence par l'étudiant via ce code, avec gestion de l'expiration et des tentatives multiples
- Ajout manuel d'une présence par le formateur
- Dépôt et remplacement du lien d'exercice par l'étudiant, avant démarrage de la relecture
- Assignation automatique d'un relecteur parmi les présents, à l'exclusion de l'auteur
- Rendu de la relecture (note + commentaire) par le relecteur assigné
- Consultation de la note et du commentaire par l'étudiant relu, sans identité du relecteur
- Clôture de session par le formateur, verrouillant présences, dépôts et relectures
- Tableau récapitulatif par étudiant pour le formateur

**Explicitement exclu :**
- Authentification et gestion de comptes (mot de passe, session utilisateur sécurisée) — hors périmètre par Q1
- Notification (email, SMS, push) des étudiants ou du formateur, y compris la transmission du code de présence (acte humain hors application, cf. section 7)
- Gestion de plusieurs formateurs ou de droits différenciés entre formateurs
- Consultation par l'étudiant de l'historique détaillé de toutes ses sessions passées au-delà du tableau formateur
- Export du tableau (PDF, CSV) ou reporting avancé
- Modification a posteriori d'une relecture déjà rendue, même par le formateur (RG12)

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | L'étudiant marque sa présence à l'aide d'un code | Quand je saisis un code valide et non expiré, ma présence apparaît dans le tableau du formateur | Must |
| EF2 | Le formateur ouvre une session et obtient un code de présence | Quand je crée une session avec titre et promotion, je reçois un code, une heure d'ouverture et d'expiration | Must |
| EF3 | L'étudiant dépose le lien de son exercice pour une session | Quand je soumets un lien valide pour une session, l'exercice apparaît avec le statut « déposé » | Must |
| EF4 | L'étudiant peut remplacer le lien de son exercice | Quand aucun relecteur n'a commencé la relecture, je peux soumettre un nouveau lien qui remplace l'ancien | Should |
| EF5 | Le système assigne automatiquement un relecteur au dépôt d'un exercice | Quand un exercice est déposé, un relecteur différent de l'auteur est assigné parmi les étudiants présents à la session | Must |
| EF6 | Le relecteur rend une note et un commentaire pour l'exercice assigné | Quand je soumets une note entière (0–20) et un commentaire, la relecture passe au statut « rendue » et devient non modifiable | Must |
| EF7 | L'étudiant relu voit sa note et le commentaire | Quand ma relecture est rendue, je vois la note et le commentaire, jamais le nom du relecteur | Must |
| EF8 | Le formateur clôture une session | Quand je clôture une session, plus aucune présence, dépôt ou relecture ne peut y être créé ou modifié | Must |
| EF9 | Le formateur consulte un tableau récapitulatif par étudiant | Quand je consulte le tableau d'une promotion, je vois pour chaque étudiant : présences, exercices déposés, moyenne, relectures en attente | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'interface de marquage de présence est utilisable sur un téléphone | Test manuel sur viewport mobile (375px), parcours complet code → confirmation sans défilement horizontal |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants | Jeu de données de démonstration à 60 étudiants, mesure du temps de réponse de GET /api/tableau |
| ENF3 | Aucune information sensible (stack trace, requête SQL) n'est jamais renvoyée au client | Revue du @RestControllerAdvice : toute exception est mappée vers le format {code, message} imposé |
| ENF4 | Le système reste cohérent en cas d'accès concurrent (deux étudiants marquant présence au même instant) | Contrainte d'unicité (sessionId, etudiantId) au niveau base, testée par un test d'intégration en écriture concurrente |
| ENF5 | Aucune authentification réelle n'existe dans cette version : l'accès aux rôles repose sur une confiance implicite (contexte présentiel, code d'accès formateur non sécurisé) | Documenté explicitement, aucun test de sécurité applicable |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Un étudiant ne peut pas relire son propre exercice | Q5 |
| RG3 | Une note est un entier compris entre 0 et 20 | Q9 |
| RG4 | Un exercice n'a qu'un seul relecteur | Q6 |
| RG5 | Le relecteur est tiré au sort par le système, au moment du dépôt de l'exercice, parmi les étudiants ayant déjà marqué présence à cette session | Q7 + hypothèse §7 |
| RG6 | L'étudiant relu voit sa note et le commentaire, jamais l'identité du relecteur | Q8 |
| RG7 | Une présence ne peut être marquée qu'avec un code non expiré, et une seule fois par étudiant et par session | Q2, Q3, contrat (409 DEJA_PRESENT) |
| RG8 | Après 5 échecs de saisie de code par le même étudiant, blocage de 2 minutes avant nouvel essai, refusé en `429 TROP_DE_TENTATIVES` | Q4 + hypothèse §7 (compteur, statut 429) |
| RG9 | Un exercice peut être déposé jusqu'à la clôture de la session par le formateur, indépendamment de l'heure de fin théorique | Q12 |
| RG10 | Le lien d'un exercice peut être remplacé tant que le relecteur assigné n'a pas rendu sa relecture | Q13 |
| RG11 | Une présence ajoutée manuellement par le formateur doit être marquée comme telle (`source = FORMATEUR`) | Q14 |
| RG12 | Une relecture est définitive et non modifiable dès qu'elle est rendue | Q15 (retenue sur Q10, cf. §7) |
| RG13 | Après clôture d'une session par le formateur : plus aucune présence, aucun dépôt d'exercice ni aucune relecture ne peuvent être créés ou modifiés pour cette session | Q3, Q10, Q11, Q12, Q15 + hypothèse §7 (opération de clôture) |
| RG14 | Le tableau du formateur affiche, par étudiant : présence par session, nombre d'exercices déposés, moyenne des notes reçues, relectures encore en attente | Q16 |

## 7. Zones d'ombre, hypothèses et contradictions

**Points que la demande ne tranche pas :**

> **Le trou non comblé par les 16 questions.** Aucune Qx ne dit si un étudiant doit avoir marqué sa présence à la session pour pouvoir y déposer un exercice. Q12 tolère le dépôt jusqu'à la clôture (« certains n'ont pas de connexion le soir même »), ce qui dessine une grande tolérance, mais ne tranche pas le cas de l'absent du jour. C'est ce point qui est retenu comme le trou que ni le client ni les questions posées n'ont comblé — il est tranché en première ligne du tableau ci-dessous.

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| Faut-il avoir marqué sa présence pour déposer un exercice ? | Aucune Qx directe. Q12 tolère un dépôt tardif, hors ligne de présence | La présence n'est pas une condition du dépôt : le dépôt est rattaché à `sessionId`, pas à une présence enregistrée | Un étudiant absent peut déposer un exercice de la session — compromis assumé |
| Le contrat imposé ne prévoit aucune opération de clôture de session, alors que Q3, Q10, Q11, Q12, Q15 en dépendent toutes | Absente de l'Annexe B ; le sujet autorise l'ajout d'opérations | Ajout de `POST /api/sessions/{id}/cloture`, hors contrat imposé | Prérequis technique indispensable pour implémenter Q3/Q10/Q11/Q12/Q15 |
| Identification de l'appelant sans mot de passe (Q1) | Q1 : sélection dans une liste, rien de plus | `etudiantId` transmis explicitement dans les requêtes qui le demandent ; pour `/api/relectures/{id}`, `{id}` référence une attribution déjà liée à un relecteur précis (issue de Q7) | Sécurité minimale assumée, documentée en ENF5 comme hors périmètre de cette version |
| Moment du tirage au sort du relecteur (Q7) | Q7 ne précise pas le déclencheur | Le tirage a lieu au moment du dépôt de l'exercice, parmi les présences déjà enregistrées à cet instant | Une présence ajoutée après coup (Q14) n'entre pas dans le pool déjà tiré — limite connue |
| Aucun étudiant présent éligible comme relecteur | Non couvert par les Qx | L'exercice reste « en attente d'assignation », visible dans le tableau au même titre qu'une relecture non rendue (Q11) | Le formateur peut voir des relectures bloquées sans faute du relecteur |
| Q13 autorise le remplacement du lien « tant que personne n'a commencé à le relire », mais aucun état « relecture commencée » n'est défini par une Qx | Q13, confrontée à RG10 ; aucune Qx ne définit « commencé » | J'assimile « commencé » à « relecteur assigné » : le lien reste remplaçable jusqu'au **rendu** de la relecture, pas jusqu'à son début | `StatutRelecture` ne vaut que `ASSIGNEE` ou `RENDUE` : la condition littérale de Q13 n'est pas observable dans le modèle retenu. La fenêtre de remplacement est donc plus large que ce que le client a dit — élargissement assumé et écrit ici plutôt que subi |
| La demande ne précise pas comment le code de présence est transmis aux étudiants | Aucune Qx directe, mais Q4 et Q14 suggèrent un contexte présentiel (code deviné entre étudiants, souci de téléphone en salle) | Hypothèse : la distribution du code est un acte humain hors périmètre applicatif — le formateur l'affiche/l'annonce en salle, l'étudiant le saisit manuellement. Aucune fonctionnalité d'envoi (SMS, email, QR code) n'est développée | Confirme l'exclusion déjà actée en section 3 (pas de notification) |
| Le système ne distingue les rôles (étudiant/relecteur/formateur) par aucun mécanisme d'authentification — Q1 exclut le mot de passe, mais aucune Qx ne couvre le cas du formateur | Q1 (étudiant uniquement) ; silence total sur le formateur | Étudiant : identification faible par sélection dans une liste, etudiantId propagé côté client (localStorage), sans vérification serveur — risque d'usurpation assumé conformément à Q1. Formateur : route /formateur protégée uniquement par un code d'accès statique côté front (variable d'environnement), non lié au backend, à seule fin d'éviter l'accès accidentel — ce n'est pas une authentification réelle | Documenté comme limitation connue en ENF5, hors périmètre d'un vrai contrôle d'accès (cf. section 3) |
| RG8 (5 échecs de code ⇒ blocage 2 min) impose un compteur serveur par étudiant, alors qu'ENF5 exclut toute session utilisateur ; le contrat imposé ne prévoit par ailleurs aucun statut pour une tentative bloquée, alors que Q4 attend un refus explicite | Q4 demande le blocage ; aucune Qx ne dit où le compteur vit, ni ce que le client reçoit pendant le blocage | Compteur en mémoire applicative, indexé par `etudiantId` **seul** — jamais `(etudiantId, sessionId)`, car un code inconnu (400 `CODE_INCONNU`) ne permet pas de retrouver la session visée. Il n'entre donc pas dans le schéma versionné par Flyway. Une tentative présentée pendant le blocage est refusée en **429 `TROP_DE_TENTATIVES`**, statut ajouté au contrat sur `POST /api/presences` | État volatil : un redémarrage remet les compteurs à zéro, ce qui est acceptable pour un blocage de 2 minutes et évite une table dont le cycle de vie n'est pas demandé. Ajouter un statut à une opération imposée est une extension assumée du contrat — assumée parce que sans elle RG8 n'est pas observable par un client ; `api/contrat.yaml` le porte avant le premier commit de code de `presence/`, les trois statuts imposés (400/409/410) restant inchangés |
| Le sujet impose une architecture en couches (B3) sans dire où placer le modèle métier ni si l'entité de persistance peut porter les règles | Aucune Qx ne traite d'architecture ; B3 exige seulement séparation contrôleur / service / repository et aucune entité JPA en JSON | Modèle du domaine pur dans `domain/model/` (aucune annotation de persistance), port de persistance dans `domain/` (n'étendant pas `JpaRepository`), et en `infrastructure/persistence/` l'entité JPA, le dépôt Spring Data, le mapper et l'adaptateur | Les règles métier se testent sans base ni contexte Spring, et un service ne peut plus renvoyer par inadvertance une entité persistée : le port ne rend que des modèles. Coût assumé : deux classes à maintenir par table et un mapper écrit à la main |
| Le contrat impose le format d'erreur `{ code, message }` mais ne fixe aucun code pour les erreurs de forme (champ obligatoire manquant, JSON illisible, paramètre absent ou mal typé), ni pour une panne interne | Aucune Qx ; l'Annexe B ne donne les codes que par opération, sous forme de commentaires | Deux codes ajoutés, stables et documentés : `REQUETE_INVALIDE` (400) pour toute erreur de forme, et `ERREUR_INTERNE` (500) en filet de sécurité. Une route inconnue ou un verbe non supporté sortent au même format, avec le statut que Spring leur attribue | Sans ces deux codes, une entrée invalide sortirait en page d'erreur Spring, ce qui vaut zéro sur B4. Aucun code imposé n'est modifié, et une panne interne ne laisse jamais fuir de stack trace ni de requête SQL (ENF3) |

**Contradictions relevées :**

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| Q10 (correction possible avant clôture) vs Q15 (note définitive dès l'envoi) | Q15 retenue : verrouillage immédiat dès validation | Q15 est formulée comme un principe assumé et justifié sur le fond — « une fois que le relecteur a validé, c'est fini, il ne peut plus y revenir. C'est plus honnête pour tout le monde » — et non comme une réponse technique ponctuelle. Je la traite comme la position finale du client, qui prévaut sur Q10 |

## 8. Contraintes techniques

**Imposées par le sujet :**
- B1 — Java 17+, Maven, wrapper `mvnw` commité
- B2 — Respect strict de `api/contrat.yaml` (chemins, verbes, codes de statut, format d'erreur)
- B3 — Séparation contrôleur / service / repository, aucune entité JPA exposée en JSON (DTO systématiques)
- B4 — Validation des entrées + `@RestControllerAdvice` centralisé, jamais de stack trace au client
- B5 — Schéma versionné par Flyway, migrations commitées, `ddl-auto=update` interdit hors tests
- B6 — Un test unitaire sur une règle métier réelle (ex. RG12), un test d'intégration sur un endpoint
- F1 — Framework front déclaré et justifié dans le README, build qui passe
- F2 — Trois écrans : formateur, étudiant, relecteur
- F3 — Couche d'appel API dédiée, aucun calcul métier dupliqué côté front (la moyenne vient de l'API)

**Contraintes que je m'impose :**
- PostgreSQL comme base de données, cohérente avec Flyway
- Architecture backend en clean architecture par module, code transverse regroupé dans `common/`. Le modèle du domaine est distinct de l'entité JPA : le domaine porte les règles sans aucune dépendance de persistance, le dépôt du domaine est un port, et l'entité, l'adaptateur et le mapper vivent en infrastructure. Pas de sur-découpage pour autant : ni interface par cas d'usage, ni découpage port/in-port/out
- React + Vite + TypeScript côté front
- Jeu de données de démonstration chargé au démarrage (migration Flyway dédiée `V_seed`), pour que le correcteur ne parte jamais d'une base vide

## 9. Livrables

- `docs/CAHIER_DES_CHARGES.md` complet
- `docs/diagrammes/D1-cas-utilisation.puml`, `D2-modele-donnees.mmd`, `D3-sequence-presence.mmd`
- `api/contrat.yaml` complété
- Backlog en issues GitHub, priorisé Must/Should/Could
- `/backend` Spring Boot fonctionnel, conforme B1 à B6
- `/frontend` React fonctionnel, conforme F1 à F3
- `README.md` d'installation testé depuis un clone vierge
- `CHANGELOG.md` cohérent avec l'historique Git
- `JOURNAL.md` renseigné à chaque étape
- Trois commits `[JALON]` (`analyse`, `v0.1`, `v1.0`) poussés, dans l'ordre

## 10. Démarche prévue

Je mène les six étapes dans l'ordre imposé. L'analyse (cette étape) se clôture par le commit `[JALON] analyse`, avant tout `spring init`. Pour l'étape 2, je ne code que les tickets Must du backlog, une branche par ticket et une PR par branche selon la convention fixée ci-dessous, migrations Flyway dès le premier modèle pour ne pas être pris au dépourvu à l'étape 3. À l'ouverture de l'enveloppe, je traite en priorité le bug signalé (issue dédiée, reproduction avant correctif), puis le changement de besoin, en séparant clairement correctif et évolution dans des commits distincts, et je mets à jour le cahier des charges et les diagrammes dans la foulée plutôt qu'à la fin. Si je prends du retard, je sacrifie en priorité les tickets Should/Could et le bonus diagramme états-transitions, jamais la conformité au contrat d'API ni la clôture de session — ce sont les points qui pèsent le plus au barème. Je pousse après chaque ticket terminé, jamais en fin de journée.

**Convention de branches et de pull requests :**

| Point | Règle |
|---|---|
| Nommage | `feat/<n°issue>-<slug-court>` pour une fonctionnalité, `fix/<n°issue>-<slug-court>` pour un correctif. Exemple : `feat/2-marquer-presence` |
| Origine | Toujours créée depuis `main` à jour — jamais depuis la branche d'un autre ticket |
| Portée | **Une branche = un ticket.** Une tâche technique isolée (outillage, migration, documentation) est un ticket comme un autre : sa propre branche |
| Publication | Branche poussée dès le premier commit, puis à chaque commit |
| PR | Une PR par branche, vers `main`, liée à l'issue par `Closes #<n°>`, qui la ferme au merge |
| Après merge | Branche supprimée, puis retour sur `main` mis à jour avant d'ouvrir la suivante |
| `main` | Toujours à jour et saine : jamais plus d'un ticket d'avance sur les branches ouvertes. Aucun `push --force` destructeur dessus |

Les commits `[JALON]` font exception : ils se posent directement sur `main`, vides de code, et sont poussés aussitôt. Les noms `chore/<sujet>` sans numéro d'issue sont proscrits — ils regroupent plusieurs tickets et ne permettent donc pas de lire le découpage dans l'historique.

**Definition of Done — un ticket est terminé quand :**
- Le code respecte B3 (couches séparées, DTO, pas d'entité exposée)
- Les critères d'acceptation de l'issue sont vérifiés manuellement ou par test
- Le contrat d'API est respecté à la lettre pour l'endpoint concerné
- Une branche dédiée a été poussée, une PR vers `main` a été ouverte et liée à l'issue, et celle-ci est fermée par le merge
- `JOURNAL.md` est mis à jour pour l'étape correspondante

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 25.09.2026 | Version initiale |
| 1.1 | 25.09.2026 | Convention de branches et de PR rendue explicite : nommage (`feat/<n°issue>-<slug>`, `fix/<n°issue>-<slug>`), une branche par ticket sans exception, exception des commits `[JALON]` posés sur `main`, `main` tenue à jour. L'analyse ne fixait le principe que par une demi-phrase (« une branche par ticket ») sans nommage ni cycle de vie — insuffisant pour être appliqué sans interprétation. |
| 1.4 | 25.09.2026 | Le modèle du domaine est séparé de l'entité JPA sur le module de référence `session/`. Le domaine ne connaît plus `jakarta.persistence` : les règles métier se testent seules, et plus rien ne peut renvoyer une entité persistée en JSON puisque le dépôt du domaine rend un modèle. |
| 1.3 | 25.09.2026 | Deux codes ajoutés pour les erreurs que le contrat ne nomme pas : `REQUETE_INVALIDE` (400, erreurs de forme) et `ERREUR_INTERNE` (500). Le format imposé couvre désormais aussi les erreurs que Spring MVC lève avant d'entrer dans un contrôleur (route inconnue, verbe non supporté, corps illisible). |
| 1.2 | 25.09.2026 | Section 7 complétée après relecture des 16 `Qx` contre `CLIENT.md` : le trou du sujet est nommé (la présence est-elle requise pour déposer un exercice ?), le compteur de RG8 est situé (`etudiantId` seul) et le statut `429 TROP_DE_TENTATIVES` ajouté à `api/contrat.yaml` pour rendre la règle observable, et Q13 est tranchée contre RG10 — la condition « relecture commencée » n'étant pas observable dans le modèle retenu. |

*L'étape 3 rendra une partie de ce document faux. Reviens le corriger et note-le ici — un cahier des charges périmé est un cahier des charges mort.*
