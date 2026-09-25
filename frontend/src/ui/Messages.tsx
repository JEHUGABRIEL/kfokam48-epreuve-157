/**
 * F3 : les états de chargement et d'erreur sont gérés partout de la même façon, dans un seul endroit.
 * Chaque écran les réutilise plutôt que de réinventer son message.
 */

export function Chargement({ texte }: { texte: string }) {
  return <p className="chargement">{texte}</p>;
}

export function Erreur({ message }: { message: string | null }) {
  if (message === null) {
    return null;
  }
  return <p className="erreur" role="alert">{message}</p>;
}

export function Succes({ message }: { message: string | null }) {
  if (message === null) {
    return null;
  }
  return <p className="succes" role="status">{message}</p>;
}
