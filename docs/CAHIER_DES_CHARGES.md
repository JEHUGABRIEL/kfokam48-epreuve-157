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

**Révision de l'étape 3 (enveloppe).** Le client a changé d'avis sur un point qui touchait une règle de gestion entière : **Q6 tombe**, un exercice n'est plus relu par un pair mais **par deux pairs différents**, et la note retenue est la moyenne des deux. Le relecteur reste un étudiant assigné, mais il y en a deux par exercice, **distincts entre eux** et jamais l'auteur. La conséquence sur le modèle de données n'est plus une ligne unique par exercice, mais jusqu'à deux : c'est écrit dans D2, et c'est la migration `V3` qui le porte.

## 3. Périmètre

**Inclus dans cette version :**
- Ouverture de session et génération de code de présence par le formateur
- Marquage de présence par l'étudiant via ce code, avec gestion de l'expiration et des tentatives multiples
- Ajout manuel d'une présence par le formateur
- Dépôt et remplacement du lien d'exercice par l'étudiant, avant démarrage de la relecture
- Assignation automatique de **deux** relecteurs parmi les présents, distincts entre eux et à l'exclusion de l'auteur
- Rendu de la relecture (note + commentaire) par chacun des relecteurs assignés
- Note retenue d'un exercice : la moyenne des deux notes, ou la seule note rendue si une seule l'est — et dans ce cas **provisoire**, annoncée comme telle
- Consultation de la note (définitive ou provisoire) et du commentaire par l'étudiant relu, sans identité des relecteurs
- Clôture de session par le formateur, verrouillant présences, dépôts et relectures
- Tableau récapitulatif par étudiant pour le formateur, découpable en pages

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
| EF5 | Le système assigne automatiquement deux relecteurs au dépôt d'un exercice | Quand un exercice est déposé, deux relecteurs différents de l'auteur, et distincts entre eux, sont assignés parmi les étudiants présents à la session | Must |
| EF6 | Le relecteur rend une note et un commentaire pour l'exercice assigné | Quand je soumets une note entière (0–20) et un commentaire, la relecture passe au statut « rendue » et devient non modifiable | Must |
| EF7 | L'étudiant relu voit sa note et le commentaire | Quand au moins une relecture est rendue, je vois la note retenue et le commentaire, jamais le nom des relecteurs, et je vois si cette note est **provisoire** (une seule relecture rendue) ou définitive (les deux rendues) | Must |
| EF8 | Le formateur clôture une session | Quand je clôture une session, plus aucune présence, dépôt ou relecture ne peut y être créé ou modifié | Must |
| EF9 | Le formateur consulte un tableau récapitulatif par étudiant | Quand je consulte le tableau d'une promotion, je vois pour chaque étudiant : présences, exercices déposés, moyenne, relectures en attente | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'interface de marquage de présence est utilisable sur un téléphone | Test manuel sur viewport mobile (375px), parcours complet code → confirmation sans défilement horizontal |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants | Jeu de données de démonstration à 60 étudiants, mesure du temps de réponse de GET /api/tableau, sans paramètre (promotion entière) **et** avec `page`/`taille` (agrégats restreints à la page) |
| ENF3 | Aucune information sensible (stack trace, requête SQL) n'est jamais renvoyée au client | Revue du @RestControllerAdvice : toute exception est mappée vers le format {code, message} imposé |
| ENF4 | Le système reste cohérent en cas d'accès concurrent (deux étudiants marquant présence au même instant) | Contrainte d'unicité (sessionId, etudiantId) au niveau base, testée par un test d'intégration en écriture concurrente |
| ENF5 | Aucune authentification réelle n'existe dans cette version : l'accès aux rôles repose sur une confiance implicite (contexte présentiel, code d'accès formateur non sécurisé) | Documenté explicitement, aucun test de sécurité applicable |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Un étudiant ne peut pas relire son propre exercice | Q5 |
| RG3 | Une note est un entier compris entre 0 et 20 | Q9 |
| RG4 | Un exercice est relu par deux pairs distincts ; la note retenue est la moyenne des deux, et elle n'est définitive que lorsque les deux ont rendu | Q6, **révisée à l'étape 3** (enveloppe) |
| RG5 | Le relecteur est tiré au sort par le système, au moment du dépôt de l'exercice, parmi les étudiants ayant déjà marqué présence à cette session | Q7 + hypothèse §7 |
| RG6 | L'étudiant relu voit sa note et le commentaire, jamais l'identité du relecteur | Q8 |
| RG7 | Une présence ne peut être marquée qu'avec un code non expiré, et une seule fois par étudiant et par session | Q2, Q3, contrat (409 DEJA_PRESENT) |
| RG8 | Après 5 échecs de saisie de code par le même étudiant, blocage de 2 minutes avant nouvel essai, refusé en `429 TROP_DE_TENTATIVES` | Q4 + hypothèse §7 (compteur, statut 429) |
| RG9 | Un exercice peut être déposé jusqu'à la clôture de la session par le formateur, indépendamment de l'heure de fin théorique | Q12 |
| RG10 | Le lien d'un exercice peut être remplacé tant qu'aucun des relecteurs assignés n'a rendu | Q13 + hypothèse §7 (deux relecteurs) |
| RG11 | Une présence ajoutée manuellement par le formateur doit être marquée comme telle (`source = FORMATEUR`) | Q14 |
| RG12 | Une relecture est définitive et non modifiable dès qu'elle est rendue. La note retenue d'un exercice, elle, ne devient définitive qu'avec la dernière des relectures assignées | Q15 (retenue sur Q10, cf. §7) + étape 3 |
| RG13 | Après clôture d'une session par le formateur : plus aucune présence, aucun dépôt d'exercice ni aucune relecture ne peuvent être créés ou modifiés pour cette session | Q3, Q10, Q11, Q12, Q15 + hypothèse §7 (opération de clôture) |
| RG14 | Le tableau du formateur affiche, par étudiant : présence par session, nombre d'exercices déposés, moyenne des **notes retenues** (la note retenue de chaque exercice, puis leur moyenne), relectures encore en attente | Q16 + étape 3 |

## 7. Zones d'ombre, hypothèses et contradictions

**Changement de besoin de l'étape 3.** L'enveloppe n'ajoute pas une demande à celles du client : elle **en remplace une**. « Finalement, un seul relecteur ça ne marche pas » — Q6 tombe, remplacée par la règle des deux pairs et de la moyenne. Ce n'est donc pas une zone d'ombre de plus, c'est une règle de gestion révisée, et cette révision se propage à la base, au contrat, au front et aux diagrammes. Les sept décisions qu'elle a fallu prendre sont dans le tableau ci-dessous, au même titre que les autres.

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
| Le système ne distingue les rôles (étudiant/relecteur/formateur) par aucun mécanisme d'authentification — Q1 exclut le mot de passe, mais aucune Qx ne couvre le cas du formateur | Q1 (étudiant uniquement) ; silence total sur le formateur | Identification faible par sélection dans une liste, l'`etudiantId` étant transmis explicitement dans les requêtes qui le demandent, sans vérification serveur — risque d'usurpation assumé conformément à Q1. Aucun des trois rôles n'est protégé : il n'existe ni code d'accès, ni route réservée côté front, et aucun identifiant n'est consigné dans le stockage du navigateur (une promotion ou un nom ne survit pas à un rechargement) | Documenté comme limitation connue en ENF5, hors périmètre d'un vrai contrôle d'accès (cf. section 3) |
| RG8 (5 échecs de code ⇒ blocage 2 min) impose un compteur serveur par étudiant, alors qu'ENF5 exclut toute session utilisateur ; le contrat imposé ne prévoit par ailleurs aucun statut pour une tentative bloquée, alors que Q4 attend un refus explicite | Q4 demande le blocage ; aucune Qx ne dit où le compteur vit, ni ce que le client reçoit pendant le blocage | Compteur en mémoire applicative, indexé par `etudiantId` **seul** — jamais `(etudiantId, sessionId)`, car un code inconnu (400 `CODE_INCONNU`) ne permet pas de retrouver la session visée. Il n'entre donc pas dans le schéma versionné par Flyway. Une tentative présentée pendant le blocage est refusée en **429 `TROP_DE_TENTATIVES`**, statut ajouté au contrat sur `POST /api/presences` | État volatil : un redémarrage remet les compteurs à zéro, ce qui est acceptable pour un blocage de 2 minutes et évite une table dont le cycle de vie n'est pas demandé. Ajouter un statut à une opération imposée est une extension assumée du contrat — assumée parce que sans elle RG8 n'est pas observable par un client ; `api/contrat.yaml` le porte avant le premier commit de code de `presence/`, les trois statuts imposés (400/409/410) restant inchangés |
| Le sujet impose une architecture en couches (B3) sans dire où placer le modèle métier ni si l'entité de persistance peut porter les règles | Aucune Qx ne traite d'architecture ; B3 exige seulement séparation contrôleur / service / repository et aucune entité JPA en JSON | Modèle du domaine pur dans `domain/model/` (aucune annotation de persistance), port de persistance dans `domain/` (n'étendant pas `JpaRepository`), et en `infrastructure/persistence/` l'entité JPA, le dépôt Spring Data, le mapper et l'adaptateur | Les règles métier se testent sans base ni contexte Spring, et un service ne peut plus renvoyer par inadvertance une entité persistée : le port ne rend que des modèles. Coût assumé : deux classes à maintenir par table et un mapper écrit à la main |
| Le contrat impose le format d'erreur `{ code, message }` mais ne fixe aucun code pour les erreurs de forme (champ obligatoire manquant, JSON illisible, paramètre absent ou mal typé), ni pour une panne interne | Aucune Qx ; l'Annexe B ne donne les codes que par opération, sous forme de commentaires | Deux codes ajoutés, stables et documentés : `REQUETE_INVALIDE` (400) pour toute erreur de forme, et `ERREUR_INTERNE` (500) en filet de sécurité. Une route inconnue ou un verbe non supporté sortent au même format, avec le statut que Spring leur attribue | Sans ces deux codes, une entrée invalide sortirait en page d'erreur Spring, ce qui vaut zéro sur B4. Aucun code imposé n'est modifié, et une panne interne ne laisse jamais fuir de stack trace ni de requête SQL (ENF3) |
| Le contrat ne nomme pas les erreurs de **référence** (un `etudiantId`, une `sessionId` ou une `exerciceId` qui ne désignent rien) alors que ces identifiants arrivent en entrée de plusieurs opérations | Aucune Qx ; l'Annexe B ne donne que `PROMOTION_INCONNUE` (404) sur `GET /api/tableau` | Quatre codes ajoutés, tous en **404** : `SESSION_INCONNUE`, `ETUDIANT_INCONNU`, `EXERCICE_INCONNU`, `RELECTURE_INCONNUE` ; et un **409** `SESSION_DEJA_CLOTUREE` pour une écriture tentée après clôture (RG13) | 404 plutôt que 400 parce que l'Annexe B applique déjà cette règle à une donnée de référence : un identifiant qui ne désigne rien n'est pas une erreur de forme, c'est une référence inexistante. Aucun code imposé n'est renommé ni détourné de son sens |
| Le contrat exige `NOTE_INVALIDE` pour une note « hors 0–20 **ou non entière** », mais la liaison JSON refuse un décimal dans un champ `integer` avant que la logique métier ne s'exécute | Aucune Qx ; précision technique absente du contrat | Une note `12.5` sort en `400 REQUETE_INVALIDE` (erreur de forme), une note `25` en `400 NOTE_INVALIDE` (règle de gestion RG3) | Le statut imposé est le même (400) ; seul l'identifiant diffère, et il reste stable. Le contrôle de borne est volontairement laissé au domaine : le placer en `@Min/@Max` aurait produit `REQUETE_INVALIDE` pour une note de 25, ce qui aurait contredit le contrat sur le cas le plus courant |
| Aucune `Qx` ne parle de pagination, alors que la promotion de démonstration compte 60 étudiants et que le tableau comme les listes d'identification deviennent longs à parcourir | Aucune `Qx` ; l'Annexe B ne décrit aucune pagination, et le contrat précise que la réponse de `GET /api/tableau` est un tableau JSON | `page` et `taille` ajoutés en paramètres **optionnels** sur `GET /api/tableau`, `GET /api/etudiants`, `GET /api/promotions` et `GET /api/relectures/assignees`. La réponse reste un tableau JSON, jamais une enveloppe : le total avant découpage part dans l'en-tête `X-Total-Count` | Sans paramètre, l'opération imposée rend exactement ce qu'elle rendait — la collection entière : un défaut de 20 lignes appliqué à un appel qui n'a rien demandé aurait rompu le contrat imposé sans qu'aucun de ses codes de statut ne change. `page` ou `taille` hors bornes sortent en `400 REQUETE_INVALIDE`, code déjà appliqué à toute erreur de forme et déjà atteignable sur cette opération par un `promotionId` non numérique : **aucun code de statut nouveau n'apparaît**. Les agrégats du tableau ne portent que sur la page demandée, ce qui laisse ENF2 tenu page par page |
| L'Annexe B ne prévoit aucune opération pour l'ajout manuel d'une présence, alors que Q14 le demande explicitement et que le contrat lui-même rappelle l'existence du champ `source` (`ETUDIANT` ou `FORMATEUR`) | Q14, et la note de l'Annexe B qui renvoie à Q14 pour justifier ce champ | Ajout de `POST /api/presences/formateur`, **après le gel du contrat**, sans code à saisir, et marquant la présence `source = FORMATEUR` | C'est l'unique extension faite à un contrat déjà gelé, et elle est écrite ici avec sa raison : sans cette opération, RG11 n'était pas implémentable et le champ `source` n'avait aucun cas d'usage observable. Le contrat imposé n'est ni modifié ni détourné : les cinq opérations exigées gardent leurs chemins, codes et format d'erreur |
| Aucune `Qx` ne décrit comment on **entre** dans une interface : Q1 ne fait qu'écarter le mot de passe, et ENF5 parle d'une « confiance implicite » sans dire à quoi elle ressemble. F2 exige trois écrans sans dire ce qui les sépare | Q1 (identification d'un étudiant par une liste) ; silence total sur le formateur et sur l'écran d'entrée | Un écran d'accueil « Qui êtes-vous ? » fait choisir le rôle — formateur, étudiant, relecteur — **avant** toute interface ; la barre latérale devient ensuite celle des **fonctionnalités du rôle**, avec un retour possible au choix du rôle. Le rôle et la fonctionnalité affichée vivent en mémoire de la page : ni jeton, ni stockage du navigateur | Aucun contrôle d'accès n'est ajouté pour autant : ce choix dit où l'on va, il ne prouve pas qui l'on est — l'usurpation reste possible et assumée (ENF5). Le coût de ce qui n'est pas fait est explicite : ni routeur, ni URL par rôle, donc un rechargement ramène à l'accueil |
| Le client change d'avis à l'étape 3 : Q6 (« un exercice n'a qu'un seul relecteur ») est retirée | Enveloppe de l'étape 3, qui **remplace** Q6 au lieu de s'y ajouter | Un exercice est relu par **deux pairs distincts**, jamais l'auteur, et la note retenue est la moyenne des deux. Q6 n'est pas contournée : elle est révisée, et la révision est écrite ici, dans D2 et dans la migration `V3` | Un Must qui arrive tard fait grossir le périmètre : il faut donc que quelque chose en sorte, et c'est écrit à la dernière ligne de ce tableau. Toute la chaîne suit : base, modèle, contrat, front |
| Aucune Qx ne dit comment arrondir la moyenne des deux notes | Silence ; Q9 ne parle que de la note d'un relecteur (entier de 0 à 20) | Moyenne **exacte, à deux décimales** : 13,5 reste 13,5 | Deux notes entières peuvent donner une note non entière. La note d'un relecteur reste un entier — c'est le contrat imposé —, mais la note *retenue* est un nombre : arrondir aurait fait disparaître la moyenne au nom de la lisibilité, alors que c'est précisément ce que le client demande de retenir |
| Aucune Qx ne dit ce qu'on fait quand il n'y a pas deux étudiants présents éligibles | Q7 ne décrit le tirage qu'en supposant des candidats ; ni Q5 ni Q6 ne prévoient la disette | On assigne **ceux qui sont là**, jusqu'à deux. Avec un seul pair, l'exercice est relu une fois et sa note **reste provisoire** | La note existe — mieux que pas de note du tout — et l'étudiant sait qu'elle peut encore bouger. Ne rien assigner aurait laissé l'exercice hors circuit ; déclarer définitive une note unique aurait contredit Q15 relue à l'étape 3 |
| Le client demande une note « marquée comme provisoire » sans dire où | Enveloppe : « on affiche sa note en attendant, mais marquée comme provisoire » | Un champ booléen `provisoire` sur la réponse de `GET /api/relectures/recues` ; `statut` inchangé | Cette opération est un **ajout** au contrat imposé, sa forme nous appartient donc. Un booléen se lit sans interprétation, là où un statut obligerait chaque client à deviner ce que « RENDUE » signifie quand un second relecteur est encore attendu. Les cinq opérations imposées ne bougent pas d'un caractère |
| Deux relecteurs peuvent commenter, mais la réponse n'a qu'un champ `commentaire` | Aucune Qx ; cette forme vient de l'ajout fait avant le changement de besoin | Quand les deux ont rendu, les deux commentaires sont **concaténés**, sans nom, du premier rendu au second | Garder un seul champ évite de casser la forme existante et de faire diverger le front ; concaténer sans nom tient RG6, et l'ordre (premier rendu, puis second) évite de laisser croire qu'un seul relecteur s'est exprimé |
| Avec deux relectures, quand l'exercice passe-t-il `RELU`, et quand le lien se ferme-t-il ? | Aucune Qx : Q13 et Q15 ont été écrites pour un relecteur unique | `RELU` seulement quand **toutes** les relectures assignées sont rendues ; le remplacement du lien se ferme dès la **première** | Les deux règles ne répondent pas à la même question : l'état de l'exercice dit si le travail est corrigé — il faut les deux notes pour que la note retenue existe —, tandis que le lien doit se fermer dès qu'un relecteur a noté. Laisser remplacer le lien sous les pieds d'un relecteur qui vient de rendre serait exactement le défaut que Q13 voulait éviter |
| Le Must qui arrive à l'étape 3 grossit le périmètre d'un changement qui touche la base, le contrat et le front à la fois | L'enveloppe demande explicitement d'écrire ce qui sort du périmètre, et pourquoi | Le ticket `#79` (le même `500` que `#78`, sur le dépôt d'exercice et l'assignation) **sort du périmètre** : l'issue reste ouverte et porte la raison | Coût assumé et localisé : deux dépôts simultanés du même étudiant rendront encore `500 ERREUR_INTERNE` au lieu de `409 EXERCICE_DEJA_DEPOSE`. `#78` a laissé la marche à suivre derrière lui — la correction sera une copie, pas une enquête. Un périmètre réduit et annoncé vaut mieux qu'un périmètre tenu à moitié |

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
- `docs/diagrammes/D1-cas-utilisation.md`, `D2-modele-donnees.md`, `D3-sequence-presence.md` (Mermaid en bloc dans un fichier Markdown, affiché directement par GitHub) et `D4-etats-transitions-exercice.md` (bonus)
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
| 1.10 | 25.09.2026 | **Changement de besoin de l'étape 3** (enveloppe) : Q6 tombe, un exercice est relu par **deux** pairs et la note retenue est la moyenne des deux. Révisés : §2 (acteurs), §3 (périmètre), `EF5` et `EF7`, `RG4`, `RG10`, `RG12` et `RG14`, plus sept décisions nouvelles en §7 — arrondi de la moyenne, pairs disponibles, marque du provisoire, commentaires concaténés, état `RELU`, fermeture du lien, et le sacrifice du ticket `#79`. D2 et D4 suivent : l'unicité passe de `(exercice_id)` à `(exercice_id, relecteur_id)`, et `RELU` attend la dernière relecture. Une analyse qu'on ne corrige pas quand le besoin change n'est plus une analyse, c'est un souvenir. |
| 1.1 | 25.09.2026 | Convention de branches et de PR rendue explicite : nommage (`feat/<n°issue>-<slug>`, `fix/<n°issue>-<slug>`), une branche par ticket sans exception, exception des commits `[JALON]` posés sur `main`, `main` tenue à jour. L'analyse ne fixait le principe que par une demi-phrase (« une branche par ticket ») sans nommage ni cycle de vie — insuffisant pour être appliqué sans interprétation. |
| 1.2 | 25.09.2026 | Section 7 complétée après relecture des 16 `Qx` contre `CLIENT.md` : le trou du sujet est nommé (la présence est-elle requise pour déposer un exercice ?), le compteur de RG8 est situé (`etudiantId` seul) et le statut `429 TROP_DE_TENTATIVES` ajouté à `api/contrat.yaml` pour rendre la règle observable, et Q13 est tranchée contre RG10 — la condition « relecture commencée » n'étant pas observable dans le modèle retenu. |
| 1.3 | 25.09.2026 | Deux codes ajoutés pour les erreurs que le contrat ne nomme pas : `REQUETE_INVALIDE` (400, erreurs de forme) et `ERREUR_INTERNE` (500). Le format imposé couvre désormais aussi les erreurs que Spring MVC lève avant d'entrer dans un contrôleur (route inconnue, verbe non supporté, corps illisible). |
| 1.4 | 25.09.2026 | Le modèle du domaine est séparé de l'entité JPA sur le module de référence `session/`. Le domaine ne connaît plus `jakarta.persistence` : les règles métier se testent seules, et plus rien ne peut renvoyer une entité persistée en JSON puisque le dépôt du domaine rend un modèle. |
| 1.5 | 25.09.2026 | §7 complétée une seconde fois, à la fin de l'étape 2, en confrontant le document au code livré : les quatre codes 404 de référence (`SESSION_INCONNUE`, `ETUDIANT_INCONNU`, `EXERCICE_INCONNU`, `RELECTURE_INCONNUE`), le 409 `SESSION_DEJA_CLOTUREE` (RG13) et le cas de la note non entière y sont écrits. §9 corrigé : les diagrammes sont des fichiers `.md` contenant un bloc Mermaid, pas des `.puml`/`.mmd` — la liste des livrables décrivait un état antérieur au renommage. Une section 7 qui n'est juste qu'au moment où on l'écrit est une section 7 morte. |
| 1.6 | 25.09.2026 | `RG11` (Q14) livré en fin de parcours : `POST /api/presences/formateur` ajouté au contrat **après son gel**, et la raison de cette entorse écrite en §7. Le champ `source` de D2 et de `V1` avait été prévu dès l'analyse sans qu'aucune opération ne le renseigne — c'est le seul écart entre l'analyse et le produit que la relecture finale a révélé. |
| 1.7 | 25.09.2026 | Pagination des lectures livrée : `page` et `taille` en paramètres optionnels sur `GET /api/tableau`, `GET /api/etudiants`, `GET /api/promotions` et `GET /api/relectures/assignees`, réponse inchangée (tableau JSON) et total dans `X-Total-Count`. §7 porte la décision et sa raison principale : sans paramètre, l'opération imposée doit rendre exactement ce qu'elle rendait, donc aucun défaut ne s'applique à un appel qui n'a rien demandé. ENF2 précisé : la mesure vaut avec et sans pagination. §3 mentionne le découpage. |
| 1.9 | 25.09.2026 | Relecture de fin de parcours : le journal des révisions est remis dans l'ordre (`1.4` → `1.8`), où deux entrées s'étaient glissées à contretemps. Aucune décision du document n'est modifiée — une relecture qui ne change que la forme n'a pas à se dater comme un arbitrage —, mais l'ordre est ce qui rend une trace lisible, et une trace illisible ne vaut pas mieux qu'une trace absente. |
| 1.8 | 25.09.2026 | Écran d'accueil ajouté : le rôle se choisit **avant** d'entrer dans une interface, et la barre latérale porte désormais les fonctionnalités de ce rôle au lieu du choix du rôle lui-même. §7 porte la décision (aucune `Qx` ne décrit l'entrée dans l'application), avec ce qu'elle n'apporte pas : ce choix ne protège rien, et l'absence de routeur signifie qu'un rechargement ramène à l'accueil. La ligne de §7 sur l'accès aux rôles est corrigée au passage : elle décrivait encore un `localStorage` et un code d'accès front qui n'existent plus. |

*L'étape 3 rendra une partie de ce document faux. Reviens le corriger et note-le ici — un cahier des charges périmé est un cahier des charges mort.*
