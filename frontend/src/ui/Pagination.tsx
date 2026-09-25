/**
 * Le seul contrôle de pagination de l'application.
 *
 * L'API découpe les lectures (`page`, `taille`) et annonce le total dans `X-Total-Count` ; ce composant
 * ne découpe rien lui-même, il demande une autre page. Il ne s'affiche pas quand il n'y a qu'une seule
 * page : un « Précédent / Suivant » inerte sur une liste d'un élément est du bruit.
 */

type Props = {
  page: number;
  total: number;
  taille: number;
  onPage: (page: number) => void;
};

export function Pagination({ page, total, taille, onPage }: Props) {
  const pages = Math.max(1, Math.ceil(total / taille));
  if (pages <= 1) {
    return null;
  }

  return (
    <nav className="pagination" aria-label="Pagination">
      <button type="button" onClick={() => onPage(page - 1)} disabled={page <= 1}>
        ‹ Précédent
      </button>
      <p className="discret">
        Page {page} sur {pages} — {total} au total
      </p>
      <button type="button" onClick={() => onPage(page + 1)} disabled={page >= pages}>
        Suivant ›
      </button>
    </nav>
  );
}
