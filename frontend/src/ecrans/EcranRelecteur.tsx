import { useState } from 'react';
import { api, ErreurApi, type RelectureAssignee } from '../api/client';
import { ListeDeroulante } from '../ui/ListeDeroulante';
import { Chargement, Erreur, Succes } from '../ui/Messages';
import { useEtudiants, usePromotions } from '../ui/useListes';

/**
 * Écran relecteur — EF6 (rendre une note et un commentaire).
 *
 * L'identifiant de la relecture se lit ici, et nulle part ailleurs : c'est le seul moyen d'atteindre
 * `POST /api/relectures/{id}`. L'auteur de l'exercice n'est jamais affiché (RG6).
 */
export function EcranRelecteur() {
  const promotions = usePromotions();
  const [promotionId, setPromotionId] = useState<number | null>(null);
  const etudiants = useEtudiants(promotionId);
  const [relecteurId, setRelecteurId] = useState<number | null>(null);
  const [assignees, setAssignees] = useState<RelectureAssignee[] | null>(null);
  const [notes, setNotes] = useState<Record<number, string>>({});
  const [commentaires, setCommentaires] = useState<Record<number, string>>({});
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  async function chargerAssignees() {
    if (relecteurId === null) {
      setErreur('Choisissez votre nom.');
      return;
    }
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      setAssignees(await api.relecturesAssignees(relecteurId));
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  async function rendre(relectureId: number) {
    const noteSaisie = Number(notes[relectureId]);
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      await api.rendreRelecture(relectureId, noteSaisie, commentaires[relectureId] ?? '');
      setSucces('Relecture rendue : elle est définitive (RG12).');
      if (relecteurId !== null) {
        setAssignees(await api.relecturesAssignees(relecteurId));
      }
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  return (
    <section>
      <h2>Relecteur</h2>
      <Erreur message={erreur} />
      <Succes message={succes} />

      <div className="carte">
        <h3>Mes relectures à rendre</h3>
        <ListeDeroulante
          libelle="Promotion"
          etat={promotions}
          valeur={promotionId}
          onChange={setPromotionId}
          invitation="— Choisir une promotion —"
        />
        <ListeDeroulante
          libelle="Mon nom"
          etat={etudiants}
          valeur={relecteurId}
          onChange={setRelecteurId}
          invitation="— Choisir mon nom —"
        />
        <button type="button" onClick={chargerAssignees} disabled={chargement}>
          Afficher mes relectures
        </button>
        {chargement && <Chargement texte="Chargement…" />}

        {assignees !== null && assignees.length === 0 && (
          <p className="discret">Aucune relecture en attente pour vous.</p>
        )}

        {assignees !== null &&
          assignees.map((relecture) => (
            <div className="carte interne" key={relecture.id}>
              <p>
                Relecture n° <strong>{relecture.id}</strong> — exercice {relecture.exerciceId}, session{' '}
                {relecture.sessionId}
              </p>
              <p>
                <a href={relecture.lien} target="_blank" rel="noreferrer">
                  Ouvrir le travail à relire
                </a>
              </p>
              <div className="ligne-champs">
                <label>
                  Note (0 à 20)
                  <input
                    type="number"
                    min={0}
                    max={20}
                    value={notes[relecture.id] ?? ''}
                    onChange={(evenement) =>
                      setNotes({ ...notes, [relecture.id]: evenement.target.value })
                    }
                  />
                </label>
                <label>
                  Commentaire
                  <input
                    value={commentaires[relecture.id] ?? ''}
                    onChange={(evenement) =>
                      setCommentaires({ ...commentaires, [relecture.id]: evenement.target.value })
                    }
                  />
                </label>
              </div>
              <button type="button" onClick={() => rendre(relecture.id)} disabled={chargement}>
                Rendre la relecture
              </button>
            </div>
          ))}
      </div>
    </section>
  );
}
