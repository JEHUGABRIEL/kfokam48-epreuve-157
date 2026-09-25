/**
 * Couche d'appels API — F3 : aucun `fetch` ailleurs dans l'application.
 *
 * Toute réponse d'erreur du backend respecte le format imposé `{ code, message }` (B4). Elle est
 * traduite ici en `ErreurApi`, pour que les écrans n'aient jamais à interpréter un statut HTTP : ils
 * affichent `erreur.message`, qui est déjà une phrase lisible en français.
 *
 * Les lectures paginées sont signalées par l'en-tête `X-Total-Count`, et non par le corps : la réponse
 * de `GET /api/tableau` est un tableau JSON imposé par le contrat, elle ne peut pas devenir une
 * enveloppe. Le total ne quitte donc jamais ce module sous forme d'en-tête — les écrans reçoivent des
 * éléments et un nombre.
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

/** Une page d'éléments, et le nombre total d'éléments disponibles avant découpage. */
export type PageDe<T> = { elements: T[]; total: number };

/** Taille demandée à l'API. Le plafond côté serveur est de 100 (400 REQUETE_INVALIDE au-delà). */
export const TAILLE_PAGE = 20;

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

async function envoyer(chemin: string, init?: RequestInit): Promise<Response> {
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

  return reponse;
}

async function lire<T>(reponse: Response): Promise<T> {
  const texte = await reponse.text();
  return (texte === '' ? undefined : JSON.parse(texte)) as T;
}

async function requete<T>(chemin: string, init?: RequestInit): Promise<T> {
  return lire<T>(await envoyer(chemin, init));
}

async function requetePaginee<T>(chemin: string, init?: RequestInit): Promise<PageDe<T>> {
  const reponse = await envoyer(chemin, init);
  const total = reponse.headers.get('X-Total-Count');
  return {
    elements: await lire<T[]>(reponse),
    total: total === null ? 0 : Number(total),
  };
}

export const api = {
  promotions: (page = 1) =>
    requetePaginee<Promotion>(`/promotions?page=${page}&taille=${TAILLE_PAGE}`),

  etudiants: (promotionId: number, page = 1) =>
    requetePaginee<Etudiant>(
      `/etudiants?promotionId=${promotionId}&page=${page}&taille=${TAILLE_PAGE}`,
    ),

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

  relecturesAssignees: (relecteurId: number, page = 1) =>
    requetePaginee<RelectureAssignee>(
      `/relectures/assignees?relecteurId=${relecteurId}&page=${page}&taille=${TAILLE_PAGE}`,
    ),

  rendreRelecture: (id: number, note: number, commentaire: string) =>
    requete<RelectureRecue>(`/relectures/${id}`, {
      method: 'POST',
      body: JSON.stringify({ note, commentaire }),
    }),

  relectureRecue: (etudiantId: number, sessionId: number) =>
    requete<RelectureRecue>(`/relectures/recues?etudiantId=${etudiantId}&sessionId=${sessionId}`),

  tableau: (promotionId: number, page = 1) =>
    requetePaginee<LigneTableau>(
      `/tableau?promotionId=${promotionId}&page=${page}&taille=${TAILLE_PAGE}`,
    ),
};
