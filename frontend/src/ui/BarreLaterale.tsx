import { catalogueDe, type Role, type Section } from './roles';

/**
 * F2 : la barre latérale liste les **fonctionnalités du rôle choisi** — une entrée par fonctionnalité,
 * et la fonctionnalité affichée est marquée (`aria-current`). Le choix du rôle lui-même est en amont,
 * sur l'écran d'accueil ; il reste atteignable par « Changer de rôle », pour ne pas enfermer.
 *
 * Le détail de ce que le rôle recouvre est porté par le catalogue (`roles.ts`), pas ici : cette barre
 * ne fait que l'afficher.
 */

type Props = {
  role: Role;
  section: Section;
  onChoisirSection: (section: Section) => void;
  onChangerRole: () => void;
};

export function BarreLaterale({ role, section, onChoisirSection, onChangerRole }: Props) {
  const catalogue = catalogueDe(role);

  return (
    <aside className="barre-laterale">
      <div className="marque">
        <h1>Suivi de présence</h1>
        <p className="sous-titre">
          {catalogue.libelle} — Promotion 2026
        </p>
      </div>

      <nav className="navigation" aria-label={`Fonctionnalités ${catalogue.libelle}`}>
        <p className="etiquette">Fonctionnalités</p>
        {catalogue.sections.map((entree) => (
          <button
            key={entree.cle}
            type="button"
            className={entree.cle === section ? 'lien-section actif' : 'lien-section'}
            aria-current={entree.cle === section ? 'page' : undefined}
            onClick={() => onChoisirSection(entree.cle)}
          >
            {entree.libelle}
          </button>
        ))}
      </nav>

      <button type="button" className="changer-role" onClick={onChangerRole}>
        Changer de rôle
      </button>

      <footer className="note-bas">
        <p>
          Il n'y a pas de mot de passe (Q1, ENF5) : le rôle remplace la connexion, et l'identité se
          choisit dans l'écran. Les règles métier — expiration du code, tirage du relecteur,
          verrouillage de la note, moyenne — sont appliquées par l'API. Cet écran ne les recalcule
          jamais (F3).
        </p>
      </footer>
    </aside>
  );
}
