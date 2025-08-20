import { Author } from '../models/author.model';

export function getInitials(name?: string): string {
  if (!name) return '';
  return name
    .split(' ')
    .map(part => (part || '').charAt(0))
    .join('')
    .toUpperCase()
    .substring(0, 2);
}

export function getShortBio(bio?: string, maxLength = 120): string {
  if (!bio) return '';
  return bio.length > maxLength ? bio.substring(0, maxLength) + '...' : bio;
}

export function formatDates(birthDate?: string, deathDate?: string): string {
  const birth = birthDate ? new Date(birthDate).getFullYear() : '?';
  const death = deathDate ? new Date(deathDate).getFullYear() : '';
  if (death) {
    return `${birth} - ${death}`;
  } else {
    return `Born ${birth}`;
  }
}

export function getShortTitle(title?: string, max = 30): string {
  if (!title) return '';
  return title.length > max ? title.substring(0, max) + '...' : title;
}

export function formatDate(dateString: string, locale = 'default'): string {
  if (!dateString) return '';
  const d = new Date(dateString);
  try {
    return d.toLocaleDateString(locale, { year: 'numeric', month: 'long', day: 'numeric' });
  } catch {
    return d.toLocaleDateString();
  }
}
