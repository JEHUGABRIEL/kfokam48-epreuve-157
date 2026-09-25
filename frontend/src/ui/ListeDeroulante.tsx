import type { EtatListe } from './useListes';

/**
 * Le seul `<select>` de l'application.
 *
 * Un select dont la liste n'a pas pu être chargée ne montrait rien : pas de ligne, pas de raison,
 * pas de recours — on cliquait et il ne s'ouvrait rien. Ici, il annonce toujours son état
 * (« Chargement… », « Liste indisponible », « aucune donnée »), reste désactivé tant qu'il n'y a rien
 * à choisir, et affiche la raison de l'échec **sous le champ concerné**, pas en tête de page.
 */

type Element = { id: number; nom: string };

type Props = {
  libelle: string;
  etat: EtatListe<Element>;
  valeur: number | null;
  onChange: (valeur: number | null) => void;
  /** Texte de l'option neutre quand la liste est chargée mais qu'aucun choix n'est encore fait. */
  invitation: string;
};

export function ListeDeroulante({ libelle, etat, valeur, onChange, invitation }: Props) {
  const vide = etat.donnees.length === 0;
  const inutilisable = etat.chargement || vide;

  const texteNeutre = etat.chargement
    ? 'Chargement…'
    : etat.erreur !== null
      ? 'Liste indisponible'
      : vide
        ? 'Aucune donnée'
        : invitation;

  return (
    <div className="champ">
      <label>
        {libelle}
        <select
          value={valeur ?? ''}
          disabled={inutilisable}
          onChange={(evenement) =>
            onChange(evenement.target.value === '' ? null : Number(evenement.target.value))
          }
        >
          <option value="">{texteNeutre}</option>
          {etat.donnees.map((element) => (
            <option key={element.id} value={element.id}>
              {element.nom}
            </option>
          ))}
        </select>
      </label>

      {etat.erreur !== null && (
        <p className="erreur discret">
          {etat.erreur}{' '}
          <button type="button" className="lien" onClick={etat.recharger}>
            Réessayer
          </button>
        </p>
      )}
    </div>
  );
}
