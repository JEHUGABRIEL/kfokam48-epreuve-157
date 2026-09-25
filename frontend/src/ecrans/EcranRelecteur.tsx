import { useState } from 'react';
import { api, ErreurApi, TAILLE_PAGE, type RelectureAssignee } from '../api/client';
import { ListeDeroulante } from '../ui/ListeDeroulante';
import { Chargement, Erreur, Succes } from '../ui/Messages';
import { Pagination } from '../ui/Pagination';
import { useEtudiants, usePromotions } from '../ui/useListes';

/**
 * Écran relecteur — EF6 (rendre une note et un commentaire).
 *
 * L'identifiant de la relecture se lit ici, et nulle part ailleurs : c'est le seul moyen d'atteindre
 * `POST /api/relectures/{id}`. L'auteur de l'exercice n'est jamais affiché (RG6).
 */
export function EcranRelecteur() {
  const [pagePromotions, setPagePromotions] = useState(1);
  const promotions = usePromotions(pagePromotions);
  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [pageEtudiants, setPageEtudiants] = useState(1);
  const etudiants = useEtudiants(promotionId, pageEtudiants);
  const [relecteurId, setRelecteurId] = useState<number | null>(null);
  const [assignees, setAssignees] = useState<RelectureAssignee[] | null>(null);
  const [totalAssignees, setTotalAssignees] = useState(0);
  const [pageAssignees, setPageAssignees] = useState(1);

  /** L'identité change : ce qui était affiché appartenait à quelqu'un d'autre. */
  function choisirRelecteur(id: number | null) {
    setRelecteurId(id);
    setAssignees(null);
    setTotalAssignees(0);
    setPageAssignees(1);
  }

  function choisirPromotion(id: number | null) {
    setPromotionId(id);
    choisirRelecteur(null);
    setPageEtudiants(1);
  }

  function choisirPagePromotions(page: number) {
    setPagePromotions(page);
    setPromotionId(null);
    choisirRelecteur(null);
  }

  /** Le nom choisi n'existe pas sur l'autre page : le choix repart de zéro plutôt que de rester invisible. */
  function choisirPageEtudiants(page: number) {
    setPageEtudiants(page);
    choisirRelecteur(null);
  }
  const [notes, setNotes] = useState<Record<number, string>>({});
  const [commentaires, setCommentaires] = useState<Record<number, string>>({});
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  async function chargerAssignees(page: number) {
    if (relecteurId === null) {
      setErreur('Choisissez votre nom.');
      return;
    }
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      const resultat = await api.relecturesAssignees(relecteurId, page);
      setAssignees(resultat.elements);
      setTotalAssignees(resultat.total);
      setPageAssignees(page);
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
        const resultat = await api.relecturesAssignees(relecteurId, pageAssignees);
        setAssignees(resultat.elements);
        setTotalAssignees(resultat.total);
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
          valeur={relecteurId}
          onChange={choisirRelecteur}
          invitation="— Choisir mon nom —"
        />
        <Pagination
          page={pageEtudiants}
          total={etudiants.total}
          taille={TAILLE_PAGE}
          onPage={choisirPageEtudiants}
        />
        <button type="button" onClick={() => void chargerAssignees(1)} disabled={chargement}>
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

        <Pagination
          page={pageAssignees}
          total={totalAssignees}
          taille={TAILLE_PAGE}
          onPage={(page) => void chargerAssignees(page)}
        />
      </div>
    </section>
  );
}
