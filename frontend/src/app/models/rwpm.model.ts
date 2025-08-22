// Basic RWPM (Readium Web Publication Manifest) TypeScript types used by the reader

export interface RwpmLink {
  href: string;
  type?: string;
  title?: string;
  rel?: string | string[];
  properties?: Record<string, any>;
  duration?: number;
  children?: RwpmLink[]; // nested TOC items
}

export interface RwpmMetadata {
  title?: string | { value: string; [lang: string]: any };
  author?: string | string[];
  language?: string | string[];
  modified?: string;
  published?: string;
  [key: string]: any;
}

export interface RwpmManifest {
  '@context'?: string | string[];
  metadata?: RwpmMetadata;
  readingOrder?: RwpmLink[];
  resources?: RwpmLink[];
  toc?: RwpmLink[];
  links?: RwpmLink[];
  [key: string]: any;
}

export type RwpmTocItem = RwpmLink;
