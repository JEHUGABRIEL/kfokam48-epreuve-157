# Soumission — Épreuve finale fullstack KFOKAM48

## Candidat

| | |
|---|---|
| Nom et prénom(s) | Binga Jehu Gabriel |
| Matricule | KF48-___-157 |
| Centre | Yaoundé / Douala / Bafoussam |
| Compte GitHub | JEHUGABRIEL |

## Projet

| | |
|---|---|
| Dépôt (public) | `https://github.com/JEHUGABRIEL/kfokam48-epreuve-157` |
| Commit final — hash complet, 40 caractères | *à relever sur `main` après le dernier push* |
| Branche | `main` |

## Épreuve Git — étape 5

| | |
|---|---|
| Dépôt (public) | `https://github.com/JEHUGABRIEL/kfokam48-gitlab-157` |
| Commit final — hash complet, 40 caractères | *à relever après le push de l'épreuve Git* |

## Technique

| | |
|---|---|
| Frontend utilisé | React (Vite, TypeScript) |
| Base de données | PostgreSQL 16, schéma versionné par Flyway |
| Commandes de démarrage | `docker compose up -d` puis `cd backend && ./mvnw spring-boot:run` |

## Ce que j'ai livré

**Ce qui fonctionne** : l'API du suivi de présence — ouverture de session avec code à durée de vie
limitée, clôture verrouillante, marquage de présence avec ses quatre refus (code inconnu, expiré, déjà
présent, tentative bloquée), dépôt et remplacement du lien d'exercice, assignation automatique d'un
relecteur parmi les présents, rendu d'une note verrouillée une fois envoyée, consultation de la note
sans l'identité du relecteur, et tableau récapitulatif. Schéma versionné par Flyway, données de
démonstration au démarrage, erreurs au format `{ code, message }` pour toutes les erreurs sans
exception. Les tests tournent sur un poste vierge, sans Docker ni PostgreSQL.

**Ce qui ne fonctionne pas** : **le frontend n'est pas livré** — `frontend/` est vide, donc les trois
écrans (formateur, étudiant, relecteur) et la couche d'appels API restent à faire (F2, F3). L'API
s'utilise au `curl` seulement.

**Ce que j'ai volontairement laissé de côté** : l'authentification (Q1, ENF5 — hors périmètre) ;
l'envoi du code de présence (acte humain en salle, cf. §7) ; les notifications ; l'export du tableau.
`README.md` et `CHANGELOG.md` ont été rédigés en fin de parcours : ils décrivent l'historique réel
mais n'ont pas été tenus au fil de l'eau comme le journal.

---

## Avant de téléverser, vérifie

- [ ] Mes deux dépôts sont **publics** et s'ouvrent en navigation privée
- [ ] Les deux hash font bien **40 caractères** et existent sur GitHub
- [ ] Tout mon travail est **poussé** — `git status` est propre sur les deux dépôts
- [ ] Mon `README` a été testé depuis un clone vierge, dans un dossier vide
- [ ] Mon `JOURNAL.md` et mon cahier des charges sont dans `docs/`
- [ ] Les trois commits `[JALON]` sont poussés et dans le bon ordre

---

**Déclaration.** J'ai réalisé ce travail seul. Les outils d'IA étaient autorisés sans restriction et
je les ai utilisés ; mon journal indique où et comment j'ai vérifié leurs réponses. Mes dépôts
resteront publics et inchangés jusqu'à la publication des résultats.

Signature : ______________________  Date : __________
