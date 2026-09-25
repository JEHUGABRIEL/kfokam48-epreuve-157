import { useState } from 'react';
import { api, ErreurApi, TAILLE_PAGE, type LigneTableau, type SessionOuverte } from '../api/client';
import { ListeDeroulante } from '../ui/ListeDeroulante';
import { Chargement, Erreur, Succes } from '../ui/Messages';
import { Pagination } from '../ui/Pagination';
import { usePromotions } from '../ui/useListes';
import type { Section } from '../ui/roles';

/**
 * Écran formateur — EF2 (ouvrir une séance et obtenir un code), EF8 (clôturer), EF9 (tableau).
 *
 * La fonctionnalité affichée est celle choisie dans la barre latérale : la séance d'un côté, le
 * tableau de l'autre. Le choix de la promotion, lui, reste visible dans les deux cas — c'est le
 * contexte commun des deux fonctionnalités, et un tableau sans promotion choisie ne serait qu'un
 * bouton qui refuse d'agir.
 *
 * La moyenne affichée vient de l'API : elle n'est jamais recalculée ici (F3).
 */

type Props = {
  section: Section;
};

export function EcranFormateur({ section }: Props) {
  const [pagePromotions, setPagePromotions] = useState(1);
  const promotions = usePromotions(pagePromotions);
  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [titre, setTitre] = useState('Séance du jour');
  const [session, setSession] = useState<SessionOuverte | null>(null);
  const [tableau, setTableau] = useState<LigneTableau[] | null>(null);
  const [totalTableau, setTotalTableau] = useState(0);
  const [pageTableau, setPageTableau] = useState(1);

  /** Changer de promotion rend le tableau affiché caduc : celui de l'autre promotion n'a rien à faire ici. */
  function choisirPromotion(id: number | null) {
    setPromotionId(id);
    setTableau(null);
    setTotalTableau(0);
    setPageTableau(1);
  }

  /** Changer de page change ce que la liste contient : un choix fait sur une autre page n'y est plus. */
  function choisirPagePromotions(page: number) {
    setPagePromotions(page);
    setPromotionId(null);
    setTableau(null);
    setTotalTableau(0);
  }
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  async function ouvrir() {
    if (promotionId === null) {
      setErreur('Choisissez une promotion.');
      return;
    }
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      const ouverte = await api.ouvrirSession(titre, promotionId);
      setSession(ouverte);
      setSucces(`Session ouverte. Code à annoncer en salle : ${ouverte.code}`);
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  async function cloturer() {
    if (session === null) {
      return;
    }
    setChargement(true);
    setErreur(null);
    try {
      await api.cloturerSession(session.id);
      setSucces('Session clôturée : plus aucune présence, dépôt ni relecture ne peut y être écrit.');
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  async function chargerTableau(page: number) {
    if (promotionId === null) {
      setErreur('Choisissez une promotion.');
      return;
    }
    setChargement(true);
    setErreur(null);
    try {
      const resultat = await api.tableau(promotionId, page);
      setTableau(resultat.elements);
      setTotalTableau(resultat.total);
      setPageTableau(page);
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  return (
    <section>
      <h2>Formateur</h2>
      <Erreur message={erreur} />
      <Succes message={succes} />

      <div className="carte">
        <h3>Promotion suivie</h3>
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
      </div>

      {section === 'seance' && (
        <div className="carte">
          <h3>Ouvrir une séance</h3>
          <label>
            Titre de la séance
            <input value={titre} onChange={(evenement) => setTitre(evenement.target.value)} />
          </label>
          <button type="button" onClick={ouvrir} disabled={chargement}>
            Ouvrir et obtenir le code
          </button>

          {session !== null && (
            <div className="resultat">
              <p>
                Code : <strong className="code">{session.code}</strong>
              </p>
              <p className="discret">
                Valable jusqu'à {new Date(session.expirationAt).toLocaleTimeString()} (RG1 : 15
                minutes).
              </p>
              <button type="button" onClick={cloturer} disabled={chargement}>
                Clôturer la session
              </button>
            </div>
          )}
        </div>
      )}

      {section === 'tableau' && (
        <div className="carte">
          <h3>Tableau récapitulatif</h3>
          <button type="button" onClick={() => void chargerTableau(1)} disabled={chargement}>
            Afficher le tableau de la promotion
          </button>
          {chargement && <Chargement texte="Chargement…" />}
          {tableau !== null && (
            <table>
              <thead>
                <tr>
                  <th>Étudiant</th>
                  <th>Présences</th>
                  <th>Exercices déposés</th>
                  <th>Moyenne</th>
                  <th>Relectures en attente</th>
                </tr>
              </thead>
              <tbody>
                {tableau.map((ligne) => (
                  <tr key={ligne.etudiantId}>
                    <td>{ligne.nom}</td>
                    <td>{ligne.presences}</td>
                    <td>{ligne.exercicesDeposes}</td>
                    {/* La moyenne vient de l'API ; « — » signifie « aucune note reçue », pas « 0 ». */}
                    <td>{ligne.moyenne === null ? '—' : ligne.moyenne}</td>
                    <td>{ligne.relecturesEnAttente}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          <Pagination
            page={pageTableau}
            total={totalTableau}
            taille={TAILLE_PAGE}
            onPage={(page) => void chargerTableau(page)}
          />
        </div>
      )}
    </section>
  );
}
