/**
 * Couche d'appels API — F3 : aucun `fetch` ailleurs dans l'application.
 *
 * Toute réponse d'erreur du backend respecte le format imposé `{ code, message }` (B4). Elle est
 * traduite ici en `ErreurApi`, pour que les écrans n'aient jamais à interpréter un statut HTTP : ils
 * affichent `erreur.message`, qui est déjà une phrase lisible en français.
 */

export type Promotion = { id: number; nom: string };
export type Etudiant = { id: number; nom: string };
export type SourcePresence = 'ETUDIANT' | 'FORMATEUR';

export type SessionOuverte = {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
};

export type Presence = {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: SourcePresence;
};

export type ExerciceDepose = { id: number; statut: string };

export type RelectureAssignee = {
  id: number;
  exerciceId: number;
  sessionId: number;
  lien: string;
  statut: string;
};

export type RelectureRecue = {
  statut: string;
  note: number | null;
  commentaire: string | null;
};

export type LigneTableau = {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  moyenne: number | null;
  relecturesEnAttente: number;
};

export class ErreurApi extends Error {
  readonly code: string;
  readonly statut: number;

  constructor(code: string, message: string, statut: number) {
    super(message);
    this.name = 'ErreurApi';
    this.code = code;
    this.statut = statut;
  }
}

async function requete<T>(chemin: string, init?: RequestInit): Promise<T> {
  const reponse = await fetch(`/api${chemin}`, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });

  if (!reponse.ok) {
    let corps: { code?: string; message?: string } = {};
    try {
      corps = await reponse.json();
    } catch {
      // Corps vide ou non JSON : on garde un message de repli plutôt que de laisser remonter une
      // exception de parsing, qui n'apprendrait rien à l'utilisateur.
    }
    throw new ErreurApi(
      corps.code ?? 'ERREUR_INCONNUE',
      corps.message ?? `Erreur inattendue (${reponse.status}).`,
      reponse.status,
    );
  }

  const texte = await reponse.text();
  return (texte === '' ? undefined : JSON.parse(texte)) as T;
}

export const api = {
  promotions: () => requete<Promotion[]>('/promotions'),

  etudiants: (promotionId: number) => requete<Etudiant[]>(`/etudiants?promotionId=${promotionId}`),

  ouvrirSession: (titre: string, promotionId: number) =>
    requete<SessionOuverte>('/sessions', {
      method: 'POST',
      body: JSON.stringify({ titre, promotionId }),
    }),

  cloturerSession: (id: number) =>
    requete<{ id: number; clotureAt: string }>(`/sessions/${id}/cloture`, { method: 'POST' }),

  marquerPresence: (code: string, etudiantId: number) =>
    requete<Presence>('/presences', {
      method: 'POST',
      body: JSON.stringify({ code, etudiantId }),
    }),

  deposerExercice: (sessionId: number, etudiantId: number, lien: string) =>
    requete<ExerciceDepose>('/exercices', {
      method: 'POST',
      body: JSON.stringify({ sessionId, etudiantId, lien }),
    }),

  relecturesAssignees: (relecteurId: number) =>
    requete<RelectureAssignee[]>(`/relectures/assignees?relecteurId=${relecteurId}`),

  rendreRelecture: (id: number, note: number, commentaire: string) =>
    requete<RelectureRecue>(`/relectures/${id}`, {
      method: 'POST',
      body: JSON.stringify({ note, commentaire }),
    }),

  relectureRecue: (etudiantId: number, sessionId: number) =>
    requete<RelectureRecue>(`/relectures/recues?etudiantId=${etudiantId}&sessionId=${sessionId}`),

  tableau: (promotionId: number) => requete<LigneTableau[]>(`/tableau?promotionId=${promotionId}`),
};
