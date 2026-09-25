/**
 * Le catalogue des rôles et de leurs fonctionnalités.
 *
 * Source unique, partagée par l'écran d'accueil (que choisit-on ?) et par la barre latérale (où
 * va-t-on dans ce rôle ?) : deux listes séparées auraient fini par se contredire, et une
 * fonctionnalité livrée mais absente de la barre latérale est une fonctionnalité introuvable.
 *
 * Le rôle n'est pas un compte (Q1, ENF5) : il n'y a ni mot de passe ni session. Ce catalogue décrit
 * seulement ce que l'interface propose, pas des droits — les règles métier sont appliquées par l'API.
 */

export type Role = 'FORMATEUR' | 'ETUDIANT' | 'RELECTEUR';

/** Les fonctionnalités atteignables depuis la barre latérale, tous rôles confondus. */
export type Section = 'seance' | 'tableau' | 'presence' | 'exercice' | 'relectures';

export type EntreeSection = {
  cle: Section;
  libelle: string;
};

export type EntreeRole = {
  role: Role;
  libelle: string;
  resume: string;
  sections: EntreeSection[];
};

export const ROLES: EntreeRole[] = [
  {
    role: 'FORMATEUR',
    libelle: 'Formateur',
    resume: 'Ouvrir une séance et annoncer son code, la clôturer, afficher le tableau.',
    sections: [
      { cle: 'seance', libelle: 'Ouvrir une séance' },
      { cle: 'tableau', libelle: 'Tableau récapitulatif' },
    ],
  },
  {
    role: 'ETUDIANT',
    libelle: 'Étudiant',
    resume: 'Marquer sa présence, déposer un exercice, consulter sa note.',
    sections: [
      { cle: 'presence', libelle: 'Marquer ma présence' },
      { cle: 'exercice', libelle: 'Mon exercice' },
    ],
  },
  {
    role: 'RELECTEUR',
    libelle: 'Relecteur',
    resume: 'Rendre une note et un commentaire sur les travaux qui lui sont assignés.',
    sections: [{ cle: 'relectures', libelle: 'Mes relectures à rendre' }],
  },
];

/** Un rôle absent du catalogue est une erreur de programmation, pas un cas à présenter à l'utilisateur. */
export function catalogueDe(role: Role): EntreeRole {
  const entree = ROLES.find((candidat) => candidat.role === role);
  if (entree === undefined) {
    throw new Error(`Rôle inconnu : ${role}`);
  }
  return entree;
}

/** À l'entrée dans un rôle, on ouvre sa première fonctionnalité : jamais un écran vide à droite. */
export function premiereSection(role: Role): Section {
  return catalogueDe(role).sections[0].cle;
}
