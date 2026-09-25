import { useState } from 'react';
import { api, ErreurApi, TAILLE_PAGE, type RelectureRecue } from '../api/client';
import { ListeDeroulante } from '../ui/ListeDeroulante';
import { Chargement, Erreur, Succes } from '../ui/Messages';
import { Pagination } from '../ui/Pagination';
import { useEtudiants, usePromotions } from '../ui/useListes';
import type { Section } from '../ui/roles';

/**
 * Écran étudiant — EF1 (marquer sa présence avec le code), EF3 (déposer un exercice), EF7 (consulter
 * sa note sans l'identité du relecteur).
 *
 * Q1 : l'étudiant s'identifie en se choisissant dans une liste ; il n'y a pas de mot de passe. Le
 * choix est conservé en mémoire du composant, pas dans le stockage du navigateur : rien ne justifie de
 * laisser un identifiant traîner sur un téléphone partagé.
 *
 * L'identification reste affichée quelle que soit la fonctionnalité ouverte : on ne marque pas une
 * présence ni ne dépose un exercice avant d'avoir dit qui l'on est, et cacher la liste obligerait à
 * revenir en arrière pour la moindre correction.
 */

type Props = {
  section: Section;
};

export function EcranEtudiant({ section }: Props) {
  const [pagePromotions, setPagePromotions] = useState(1);
  const promotions = usePromotions(pagePromotions);
  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [pageEtudiants, setPageEtudiants] = useState(1);
  const etudiants = useEtudiants(promotionId, pageEtudiants);
  // Aucun nom n'est pré-sélectionné : le premier étudiant de la liste n'est pas forcément l'utilisateur
  // devant son écran, et un clic sur « Marquer ma présence » engageait quelqu'un d'autre.
  const [etudiantId, setEtudiantId] = useState<number | null>(null);

  function choisirPromotion(id: number | null) {
    setPromotionId(id);
    setEtudiantId(null);
    setPageEtudiants(1);
  }

  /**
   * Changer de page change ce que la liste contient : le nom choisi n'y figure plus, alors que
   * l'application croirait encore savoir qui vous êtes. Le choix est donc remis à zéro, jamais
   * conservé en coulisse.
   */
  function choisirPagePromotions(page: number) {
    setPagePromotions(page);
    setPromotionId(null);
    setEtudiantId(null);
  }

  function choisirPageEtudiants(page: number) {
    setPageEtudiants(page);
    setEtudiantId(null);
  }

  const [code, setCode] = useState('');
  const [sessionId, setSessionId] = useState('');
  const [lien, setLien] = useState('');
  const [note, setNote] = useState<RelectureRecue | null>(null);

  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  async function executer(action: () => Promise<string>) {
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      setSucces(await action());
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  function marquerPresence() {
    if (etudiantId === null) {
      setErreur('Choisissez votre nom.');
      return;
    }
    void executer(async () => {
      const presence = await api.marquerPresence(code, etudiantId);
      return `Présence enregistrée (source ${presence.source}) pour la session ${presence.sessionId}.`;
    });
  }

  function deposer() {
    if (etudiantId === null || sessionId.trim() === '') {
      setErreur('Choisissez votre nom et indiquez la session.');
      return;
    }
    void executer(async () => {
      const exercice = await api.deposerExercice(Number(sessionId), etudiantId, lien);
      return `Exercice déposé (statut ${exercice.statut}).`;
    });
  }

  function consulterNote() {
    if (etudiantId === null || sessionId.trim() === '') {
      setErreur('Choisissez votre nom et indiquez la session.');
      return;
    }
    void executer(async () => {
      setNote(await api.relectureRecue(etudiantId, Number(sessionId)));
      return 'Relecture consultée.';
    });
  }

  return (
    <section>
      <h2>Étudiant</h2>
      <Erreur message={erreur} />
      <Succes message={succes} />

      <div className="carte">
        <h3>Qui êtes-vous ?</h3>
        <ListeDeroulante
          libelle="Promotion"
          etat={promotions}
          valeur={promotionId}
          onChange={choisirPromotion}
          invitation="— Choisir une promotion —"
        />
        <Pagination
          page={pagePromotions}
          total={promotions.total}
          taille={TAILLE_PAGE}
          onPage={choisirPagePromotions}
        />
        <ListeDeroulante
          libelle="Mon nom"
          etat={etudiants}
          valeur={etudiantId}
          onChange={setEtudiantId}
          invitation="— Choisir mon nom —"
        />
        <Pagination
          page={pageEtudiants}
          total={etudiants.total}
          taille={TAILLE_PAGE}
          onPage={choisirPageEtudiants}
        />
      </div>

      {section === 'presence' && (
        <div className="carte">
          <h3>Marquer ma présence</h3>
          <label>
            Code annoncé en salle
            <input
              value={code}
              autoComplete="off"
              onChange={(evenement) => setCode(evenement.target.value.toUpperCase())}
            />
          </label>
          <button type="button" onClick={marquerPresence} disabled={chargement}>
            Marquer ma présence
          </button>
          <p className="discret">
            Le code expire 15 minutes après l'ouverture (RG1), et 5 erreurs bloquent 2 minutes (RG8).
          </p>
        </div>
      )}

      {section === 'exercice' && (
        <div className="carte">
          <h3>Mon exercice</h3>
          <label>
            Numéro de session
            <input
              value={sessionId}
              inputMode="numeric"
              onChange={(evenement) => setSessionId(evenement.target.value)}
            />
          </label>
          <label>
            Lien de mon exercice
            <input
              value={lien}
              placeholder="https://…"
              onChange={(evenement) => setLien(evenement.target.value)}
            />
          </label>
          <div className="boutons">
            <button type="button" onClick={deposer} disabled={chargement}>
              Déposer le lien
            </button>
            <button type="button" onClick={consulterNote} disabled={chargement}>
              Voir ma note
            </button>
          </div>

          {chargement && <Chargement texte="Envoi en cours…" />}
          {note !== null && (
            <div className="resultat">
              {note.statut === 'RENDUE' && note.note !== null ? (
                <p>
                  <strong className="note">{note.note}/20</strong>{' '}
                  {/* Le client a demandé qu'une note en attente du second pair soit « marquée comme
                      provisoire ». Le mot est répété à l'écran plutôt que suggéré par une couleur :
                      une note lue dans un couloir ne doit pas dépendre de la nuance d'un fond. */}
                  {note.provisoire ? (
                    <span className="etat-note provisoire">
                      note provisoire — en attente du second relecteur
                    </span>
                  ) : (
                    <span className="etat-note definitive">
                      note définitive — moyenne des deux relecteurs
                    </span>
                  )}
                  {note.commentaire !== null && note.commentaire !== '' && <> — {note.commentaire}</>}
                </p>
              ) : (
                <p>Relecture assignée, pas encore rendue. L'identité des relecteurs reste confidentielle.</p>
              )}
            </div>
          )}
        </div>
      )}
    </section>
  );
}
