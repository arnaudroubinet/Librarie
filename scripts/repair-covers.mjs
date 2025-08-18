#!/usr/bin/env node
/*
  Repair and enrich CSV metadata using OpenLibrary.
  - Reads CSVs from backend/data if present; else from data/.
  - Writes updated CSVs into backend/data (creates it if missing), preserving semicolon-delimited format.
  - Repairs book cover_url when the current URL serves a GIF (or tiny GIF by magic bytes).
  - Enriches book rows: if synopsis/year/isbn missing, fetch from the selected OpenLibrary "product page" (work/edition JSON) chosen via search.
  - Enriches series rows: if description/cover missing, fetch from best-matching OL work series grouping where possible.
  - Enriches authors: if biography/picture missing or GIF, fetch from author JSON.

  Flags:
    --search=title|author  Biases book search scoring toward title or author (default balanced).

  Requirements: Node.js 18+ (global fetch). No external dependencies.
*/

import fs from 'node:fs';
import path from 'node:path';
import { setTimeout as delay } from 'node:timers/promises';

const ROOT = process.cwd();
const BACKEND_DATA = path.resolve(ROOT, 'backend', 'data');
const ROOT_DATA = path.resolve(ROOT, 'data');
const OUT_DIR = BACKEND_DATA; // always write here

const FILES = {
  authors: 'authors.csv',
  series: 'series.csv',
  books: 'books.csv',
};

const ARGS = Object.fromEntries(process.argv.slice(2).map(kv => {
  const [k, v] = kv.includes('=') ? kv.split('=') : [kv, true];
  return [k.replace(/^--/, ''), v === undefined ? true : v];
}));
const SEARCH_BIAS = (ARGS.search === 'title' || ARGS.search === 'author') ? ARGS.search : 'balanced';
// Networking controls
const REQ_TIMEOUT = Number.isFinite(Number(ARGS.timeout)) ? Math.max(5000, Number(ARGS.timeout)) : 15000;
const REQ_RETRIES = Number.isFinite(Number(ARGS.retries)) ? Math.max(0, Math.min(5, Number(ARGS.retries))) : 2;

async function main() {
  // Ensure at least one data directory exists
  if (!fs.existsSync(BACKEND_DATA) && !fs.existsSync(ROOT_DATA)) {
    console.error(`No data directory found. Expected one of: ${BACKEND_DATA} or ${ROOT_DATA}`);
    process.exit(1);
  }
  if (!fs.existsSync(OUT_DIR)) fs.mkdirSync(OUT_DIR, { recursive: true });

  console.log(`Reading CSVs from: ${BACKEND_DATA} (preferred) or ${ROOT_DATA}`);
  console.log(`Writing updated CSVs to: ${OUT_DIR}`);

  // First, repair/enrich authors
  await repairAuthors(resolveInputPath(FILES.authors));

  // Series: report GIFs and enrich if needed
  await enrichSeries(resolveInputPath(FILES.series));

  // Process books: update cover_url if GIF and enrich fields if missing
  const booksPath = resolveInputPath(FILES.books);
  if (!fs.existsSync(booksPath)) {
    console.warn(`Missing ${FILES.books}; nothing to do.`);
    return;
  }
  const original = fs.readFileSync(booksPath, 'utf8');
  const rows = parseCsvSemicolon(original);
  if (rows.length === 0) {
    console.warn('Empty books.csv; nothing to do.');
    return;
  }
  const header = rows[0];
  const idx = indexMap(header);
  const outRows = [header.slice()];
  let changes = 0;

  for (let i = 1; i < rows.length; i++) {
    const row = rows[i].slice();
    if (row.length === 0) continue;
    try {
      const title = safe(row, idx.title);
      const author = safe(row, idx.author_name);
      const isbn = safe(row, idx.isbn);
      const url = safe(row, idx.cover_url);
      if (!url) {
        const enriched = await enrichBookRow({ row, idx, bias: SEARCH_BIAS });
        outRows.push(enriched);
        continue;
      }

      const isGif = await isGifUrl(url);
      if (!isGif) {
        const enriched = await enrichBookRow({ row, idx, bias: SEARCH_BIAS });
        outRows.push(enriched);
        continue;
      }

  const replacement = await findOpenLibraryCover({ title, author, isbn, bias: SEARCH_BIAS });
      if (replacement) {
        row[idx.cover_url] = replacement;
        changes++;
        console.log(`Updated: "${title}" by ${author} -> ${replacement}`);
      } else {
        console.warn(`No OL cover found for: "${title}" by ${author} (ISBN: ${isbn}). Keeping original URL.`);
      }
  const enriched = await enrichBookRow({ row, idx, bias: SEARCH_BIAS });
  outRows.push(enriched);

      // Be polite to OL API
      await delay(150);
    } catch (e) {
      console.warn(`Row ${i + 1} processing error: ${e.message}`);
      outRows.push(rows[i]);
    }
  }

  const outCsv = stringifyCsvSemicolon(outRows);
  const outPath = path.join(OUT_DIR, FILES.books);
  // backup if target exists
  if (fs.existsSync(outPath)) {
    const bakPath = outPath + '.' + new Date().toISOString().replaceAll(':', '-').slice(0, 19) + '.bak';
    fs.copyFileSync(outPath, bakPath);
  }
  fs.writeFileSync(outPath, outCsv, 'utf8');
  console.log(`Wrote ${outPath} (${changes} updated)`);
  // Mirror to ./data for the seeder
  try { await syncToSeederDir(FILES.books, outCsv); } catch {}
}

function resolveInputPath(fileName) {
  const prefer = path.join(BACKEND_DATA, fileName);
  if (fs.existsSync(prefer)) return prefer;
  const fallback = path.join(ROOT_DATA, fileName);
  return fallback;
}

async function repairAuthors(inPath) {
  if (!fs.existsSync(inPath)) {
    console.warn(`Missing ${FILES.authors}; skipping authors.`);
    return;
  }
  const original = fs.readFileSync(inPath, 'utf8');
  const rows = parseCsvSemicolon(original);
  if (rows.length === 0) return;
  const header = rows[0];
  const idx = indexMap(header);
  const nameIdx = idx.name;
  const picIdx = idx.picture_url;
  const bioIdx = idx.biography;
  if (nameIdx == null || picIdx == null) {
    console.warn('authors.csv missing required columns (name, picture_url); skipping repair.');
    return;
  }
  const outRows = [header.slice()];
  let changes = 0;

  for (let i = 1; i < rows.length; i++) {
    const row = rows[i].slice();
    if (row.length === 0) continue;
    try {
      const name = safe(row, nameIdx);
      const url = safe(row, picIdx);
      if (!name) { outRows.push(row); continue; }
      // Decide whether to replace the existing picture with a reliable OpenLibrary photo
      const knownHost = url && /\b(covers\.openlibrary\.org|upload\.wikimedia\.org)\b/.test(url);
      let currentOk = !!knownHost;
      if (!currentOk && url) {
        try {
          const looksImage = await urlLooksValidImage(url);
          const isTinyGif = await isGifUrl(url);
          currentOk = looksImage && !isTinyGif;
        } catch { currentOk = false; }
      }
      // Prefer OpenLibrary-hosted author photos for consistency and smaller sizes
      let olPhoto = null;
      try { olPhoto = await findOpenLibraryAuthorPhoto(name); } catch { olPhoto = null; }
      const isAlreadyOL = url && url.includes('covers.openlibrary.org');

      if (olPhoto) {
        // Replace if missing, invalid, GIF, or not already using OL
        if (!url || !currentOk || !isAlreadyOL) {
          row[picIdx] = olPhoto;
          changes++;
          console.log(`Author picture updated: ${name} -> ${olPhoto}`);
        }
      } else if (!currentOk) {
        // Fallback to Wikipedia thumbnail
        const wiki = await findWikipediaAuthorPhoto(name);
        if (wiki) {
          row[picIdx] = wiki;
          changes++;
          console.log(`Author picture (Wikipedia) set: ${name} -> ${wiki}`);
        } else if (url) {
          console.warn(`No OL/Wikipedia photo found for ${name}, and current picture URL seems invalid.`);
        }
      }
      // Enrich biography if empty
      if (bioIdx != null && !safe(row, bioIdx)) {
        const bio = await fetchOpenLibraryAuthorBio(name);
        if (bio) row[bioIdx] = bio;
      }
      outRows.push(row);
      await delay(150);
    } catch (e) {
      console.warn(`Author row ${i + 1} processing error: ${e.message}`);
      outRows.push(rows[i]);
    }
  }

  const outCsv = stringifyCsvSemicolon(outRows);
  const outPath = path.join(OUT_DIR, FILES.authors);
  if (fs.existsSync(outPath)) {
    const bakPath = outPath + '.' + new Date().toISOString().replaceAll(':', '-').slice(0, 19) + '.bak';
    fs.copyFileSync(outPath, bakPath);
  }
  fs.writeFileSync(outPath, outCsv, 'utf8');
  console.log(`Wrote ${outPath} (${changes} authors updated)`);
  // Mirror to ./data for the seeder
  try { await syncToSeederDir(FILES.authors, outCsv); } catch {}
}

async function findOpenLibraryAuthorPhoto(name) {
  if (!name) return null;
  const searchUrl = `https://openlibrary.org/search/authors.json?q=${encodeURIComponent(name)}`;
  const resp = await fetchWithRetries(searchUrl, {}, REQ_TIMEOUT, REQ_RETRIES);
  if (!resp.ok) return null;
  const data = await resp.json();
  const docs = Array.isArray(data?.docs) ? data.docs : [];
  if (docs.length === 0) return null;

  const normName = normalize(name);
  const ranked = docs.map(d => ({
    doc: d,
    score: (
      (normalize(d.name) === normName ? 4 : normalize(d.name || '').includes(normName) ? 2 : 0) +
      (Array.isArray(d.alternate_names) && d.alternate_names.some(a => normalize(a) === normName) ? 2 : 0) +
      (typeof d.work_count === 'number' ? Math.min(2, Math.floor(d.work_count / 50)) : 0)
    )
  })).sort((a, b) => b.score - a.score);

  for (const { doc } of ranked.slice(0, 5)) {
    // Prefer direct photo id from doc if present
    if (Array.isArray(doc.photos) && doc.photos.length) {
      for (const pid of doc.photos) {
        const url = `https://covers.openlibrary.org/a/id/${pid}-L.jpg`;
        if (await urlLooksValidImage(url)) return url;
      }
    }
    // Fetch author detail to get photos
    if (doc.key) {
      try {
  const detail = await fetchWithRetries(`https://openlibrary.org${doc.key}.json`, {}, REQ_TIMEOUT, REQ_RETRIES);
        if (detail.ok) {
          const json = await detail.json();
          if (Array.isArray(json.photos)) {
            for (const pid of json.photos) {
              const url = `https://covers.openlibrary.org/a/id/${pid}-L.jpg`;
              if (await urlLooksValidImage(url)) return url;
            }
          }
          // Fallback: try OLID-based author cover even if photos not declared
          const olid = (json?.key || doc.key || '').split('/').pop();
          if (olid) {
            const url = `https://covers.openlibrary.org/a/olid/${olid}-L.jpg`;
            if (await urlLooksValidImage(url)) return url;
          }
        }
      } catch { /* ignore */ }
    }
  }
  return null;
}

async function reportGifIfAny(csvPath, urlColumn, label) {
  try {
    if (!fs.existsSync(csvPath)) return;
    const rows = parseCsvSemicolon(fs.readFileSync(csvPath, 'utf8'));
    if (rows.length === 0) return;
    const header = rows[0];
    const idx = indexMap(header);
    const col = idx[urlColumn];
    if (col == null) return;
    let count = 0;
    for (let i = 1; i < rows.length; i++) {
      const url = rows[i][col] || '';
      if (!url) continue;
      try {
        if (await isGifUrl(url)) count++;
      } catch { /* ignore */ }
      if (count >= 3) break; // sample only
    }
    if (count > 0) console.warn(`[${label}] Detected ${count}+ GIF image(s). Not auto-repairing ${label}.`);
  } catch { /* ignore */ }
}

async function enrichSeries(inPath) {
  try {
    if (!fs.existsSync(inPath)) return;
    const rows = parseCsvSemicolon(fs.readFileSync(inPath, 'utf8'));
    if (rows.length === 0) return;
    const header = rows[0];
    const idx = indexMap(header);
    const nameIdx = idx.series_name;
    const descIdx = idx.description;
    const coverIdx = idx.cover_url;
    const outRows = [header.slice()];
    let changes = 0;
    for (let i = 1; i < rows.length; i++) {
      const row = rows[i].slice();
      const name = safe(row, nameIdx);
      const cover = safe(row, coverIdx);
      let changed = false;
      if (cover) {
        try { if (await isGifUrl(cover)) changed = true; } catch {}
      } else changed = true;
      const needsDesc = !safe(row, descIdx);
      if (changed || needsDesc) {
        const info = await searchSeriesInfo(name);
        if (info) {
          if (info.cover && (!cover || changed)) { row[coverIdx] = info.cover; changed = true; }
          if (needsDesc && info.description) { row[descIdx] = info.description; changed = true; }
        }
      }
      if (changed) changes++;
      outRows.push(row);
      await delay(120);
    }
    const outCsv = stringifyCsvSemicolon(outRows);
    const outPath = path.join(OUT_DIR, FILES.series);
    if (fs.existsSync(outPath)) {
      const bakPath = outPath + '.' + new Date().toISOString().replaceAll(':', '-').slice(0, 19) + '.bak';
      fs.copyFileSync(outPath, bakPath);
    }
    fs.writeFileSync(outPath, outCsv, 'utf8');
    if (changes) console.log(`Series enriched: ${changes} updated`);
  // Mirror to ./data for the seeder
  try { await syncToSeederDir(FILES.series, outCsv); } catch {}
  } catch { /* ignore */ }
}

function indexMap(header) {
  const map = {};
  header.forEach((h, i) => { map[h] = i; });
  return map;
}

function safe(arr, i) { return i == null ? '' : (arr[i] || '').trim(); }

async function isGifUrl(url) {
  const resp = await fetchWithRetries(url, { method: 'GET' }, REQ_TIMEOUT, REQ_RETRIES);
  const ct = (resp.headers.get('content-type') || '').toLowerCase();
  if (ct.startsWith('image/gif')) return true;
  const buf = new Uint8Array(await resp.arrayBuffer());
  // Check GIF magic
  if (buf.length >= 6) {
    const sig = String.fromCharCode(...buf.slice(0, 6));
    if (sig === 'GIF87a' || sig === 'GIF89a') return true;
  }
  // Heuristic: extremely tiny images often are placeholders
  if (buf.length > 0 && buf.length < 200) return true;
  return false;
}

async function findOpenLibraryCover({ title, author, isbn, bias = 'balanced' }) {
  // Prefer ISBN directly if present
  if (isbn && isbn.replace(/[^0-9Xx]/g, '')) {
    const imgUrl = `https://covers.openlibrary.org/b/isbn/${encodeURIComponent(isbn)}-L.jpg`;
    if (await urlLooksValidImage(imgUrl)) return imgUrl;
  }

  // Try search.json with original and normalized titles; also try bias fallback
  const titleCandidates = [title, normalizeTitleForSearch(title)].filter(Boolean);
  const biases = bias === 'author' ? ['author', 'title'] : bias === 'title' ? ['title', 'author'] : ['balanced', 'author'];
  for (const t of titleCandidates) {
    for (const b of biases) {
      const params = new URLSearchParams();
      if (t) params.set('title', t);
      if (author) params.set('author', author);
      params.set('limit', '5');
      const searchUrl = `https://openlibrary.org/search.json?${params.toString()}`;
      const resp = await fetchWithRetries(searchUrl, {}, REQ_TIMEOUT, REQ_RETRIES);
      if (!resp.ok) continue;
      const data = await resp.json();
      const docs = Array.isArray(data?.docs) ? data.docs : [];
      if (!docs.length) continue;
      const nt = normalize(t);
      const na = normalize(author);
      const ranked = docs.map(d => ({
        doc: d,
        score: (
          (normalize(d.title) === nt ? (b==='author'?4:6) : t && normalize(d.title || '').includes(nt) ? (b==='author'?2:3) : 0) +
          (Array.isArray(d.author_name) && d.author_name.some(a => normalize(a) === na) ? (b==='author'?6:3) : 0) +
          (isbn && Array.isArray(d.isbn) && d.isbn.includes(isbn) ? 3 : 0) +
          (typeof d.cover_i === 'number' ? 1 : 0)
        )
      })).sort((a, b) => b.score - a.score);
      for (const { doc } of ranked) {
        if (typeof doc.cover_i === 'number') {
          const url = `https://covers.openlibrary.org/b/id/${doc.cover_i}-L.jpg`;
          if (await urlLooksValidImage(url)) return url;
        }
        if (Array.isArray(doc.isbn)) {
          for (const is of doc.isbn) {
            const url = `https://covers.openlibrary.org/b/isbn/${encodeURIComponent(is)}-L.jpg`;
            if (await urlLooksValidImage(url)) return url;
          }
        }
      }
    }
  }
  return null;
}

async function enrichBookRow({ row, idx, bias }) {
  try {
    const title = safe(row, idx.title);
    const author = safe(row, idx.author_name);
    const needSynopsis = !safe(row, idx.synopsis);
    const needYear = !safe(row, idx.year);
    const needIsbn = !safe(row, idx.isbn);
    if (!needSynopsis && !needYear && !needIsbn) return row;
    const details = await fetchOpenLibraryWorkDetails({ title, author, bias });
    if (!details) return row;
    if (needSynopsis && details.description) row[idx.synopsis] = truncate(details.description, 600);
    if (needYear && details.first_publish_year) row[idx.year] = String(details.first_publish_year);
    if (needIsbn && details.isbn) row[idx.isbn] = details.isbn;
    // also upgrade cover if empty
    if (!safe(row, idx.cover_url) && details.cover) row[idx.cover_url] = details.cover;
  } catch { /* ignore */ }
  return row;
}

function truncate(text, max) {
  if (!text) return text;
  const s = String(text);
  return s.length > max ? s.slice(0, max - 1) + '…' : s;
}

async function fetchOpenLibraryWorkDetails({ title, author, bias }) {
  const titleCandidates = [title, normalizeTitleForSearch(title)].filter(Boolean);
  const biases = bias === 'author' ? ['author', 'title'] : bias === 'title' ? ['title', 'author'] : ['balanced', 'author'];
  const pool = [];
  for (const t of titleCandidates) {
    for (const b of biases) {
      const params = new URLSearchParams();
      if (t) params.set('title', t);
      if (author) params.set('author', author);
      params.set('limit', '5');
      const url = `https://openlibrary.org/search.json?${params.toString()}`;
      const resp = await fetchWithRetries(url, {}, REQ_TIMEOUT, REQ_RETRIES);
      if (!resp.ok) continue;
      const data = await resp.json();
      const docs = Array.isArray(data?.docs) ? data.docs : [];
      const nt = normalize(t);
      const na = normalize(author);
      const ranked = docs.map(d => ({
        doc: d,
        score: (
          (normalize(d.title) === nt ? (b==='author'?4:6) : t && normalize(d.title || '').includes(nt) ? (b==='author'?2:3) : 0) +
          (Array.isArray(d.author_name) && d.author_name.some(a => normalize(a) === na) ? (b==='author'?6:3) : 0) +
          (typeof d.cover_i === 'number' ? 1 : 0)
        )
      })).sort((a, b) => b.score - a.score);
      if (ranked.length) pool.push(...ranked);
    }
  }
  if (!pool.length) return null;
  const best = pool.sort((a, b) => b.score - a.score)[0]?.doc;
  if (!best) return null;
  const workKey = Array.isArray(best.work_key) ? best.work_key[0] : best.key; // sometimes /works/...
  const editionIsbn = Array.isArray(best.isbn) ? best.isbn.find(x => x) : best.isbn;
  let description = typeof best.first_sentence === 'string' ? best.first_sentence : '';
  try {
    if (workKey) {
  const wr = await fetchWithRetries(`https://openlibrary.org${workKey}.json`, {}, REQ_TIMEOUT, REQ_RETRIES);
      if (wr.ok) {
        const w = await wr.json();
        if (!description && w.description) description = typeof w.description === 'string' ? w.description : w.description?.value || '';
      }
    }
  } catch { /* ignore */ }
  const cover = typeof best.cover_i === 'number' ? `https://covers.openlibrary.org/b/id/${best.cover_i}-L.jpg` : (editionIsbn ? `https://covers.openlibrary.org/b/isbn/${encodeURIComponent(editionIsbn)}-L.jpg` : undefined);
  return {
    description,
    first_publish_year: best.first_publish_year,
    isbn: editionIsbn,
    cover,
  };
}

async function searchSeriesInfo(name) {
  if (!name) return null;
  const resp = await fetchWithRetries(`https://openlibrary.org/search.json?title=${encodeURIComponent(name)}&limit=5`, {}, REQ_TIMEOUT, REQ_RETRIES);
  if (!resp.ok) return null;
  const data = await resp.json();
  const docs = Array.isArray(data?.docs) ? data.docs : [];
  if (!docs.length) return null;
  const normName = normalize(name);
  const ranked = docs.map(d => ({
    doc: d,
    score: (
      (normalize(d.title) === normName ? 4 : normalize(d.title || '').includes(normName) ? 2 : 0) +
      (typeof d.cover_i === 'number' ? 1 : 0)
    )
  })).sort((a, b) => b.score - a.score);
  const best = ranked[0]?.doc;
  if (!best) return null;
  let description = '';
  try {
    const workKey = Array.isArray(best.work_key) ? best.work_key[0] : best.key;
    if (workKey) {
  const wr = await fetchWithRetries(`https://openlibrary.org${workKey}.json`, {}, REQ_TIMEOUT, REQ_RETRIES);
      if (wr.ok) {
        const w = await wr.json();
        description = typeof w.description === 'string' ? w.description : w.description?.value || '';
      }
    }
  } catch { /* ignore */ }
  const cover = typeof best.cover_i === 'number' ? `https://covers.openlibrary.org/b/id/${best.cover_i}-L.jpg` : null;
  return { description, cover };
}

async function fetchOpenLibraryAuthorBio(name) {
  try {
    const searchUrl = `https://openlibrary.org/search/authors.json?q=${encodeURIComponent(name)}`;
  const resp = await fetchWithRetries(searchUrl, {}, REQ_TIMEOUT, REQ_RETRIES);
    if (!resp.ok) return null;
    const data = await resp.json();
    const doc = Array.isArray(data?.docs) && data.docs.length ? data.docs[0] : null;
    if (!doc?.key) return null;
  const detail = await fetchWithRetries(`https://openlibrary.org${doc.key}.json`, {}, REQ_TIMEOUT, REQ_RETRIES);
    if (!detail.ok) return null;
    const json = await detail.json();
    const bio = json.bio ? (typeof json.bio === 'string' ? json.bio : json.bio.value) : null;
    return bio || null;
  } catch { return null; }
}

// Wikipedia thumbnail fallback for author portraits
async function findWikipediaAuthorPhoto(name) {
  try {
    if (!name) return null;
    const params = new URLSearchParams({
      action: 'query',
      prop: 'pageimages',
      format: 'json',
      piprop: 'thumbnail',
      pithumbsize: '512',
      redirects: '1',
      titles: name,
      origin: '*',
    });
    const url = `https://en.wikipedia.org/w/api.php?${params.toString()}`;
    const resp = await fetchWithRetries(url, {}, REQ_TIMEOUT, REQ_RETRIES);
    if (!resp.ok) return null;
    const json = await resp.json();
    const pages = json?.query?.pages || {};
    for (const k of Object.keys(pages)) {
      const page = pages[k];
      const thumb = page?.thumbnail?.source;
      if (thumb && thumb.includes('upload.wikimedia.org')) return thumb;
    }
    return null;
  } catch {
    return null;
  }
}

async function urlLooksValidImage(url) {
  try {
    // Trust known hosts to reduce false negatives and bandwidth
    if (/\b(covers\.openlibrary\.org|upload\.wikimedia\.org)\b/.test(url)) {
      // Do a lightweight fetch but skip strict content-type check
      const resp = await fetchWithRetries(url, { method: 'GET' }, REQ_TIMEOUT, REQ_RETRIES);
      const buf = new Uint8Array(await resp.arrayBuffer());
      if (buf.length >= 6) {
        const sig = String.fromCharCode(...buf.slice(0, 6));
        if (sig === 'GIF87a' || sig === 'GIF89a') return false;
      }
      return buf.length >= 200;
    }
    const resp = await fetchWithRetries(url, { method: 'GET' }, REQ_TIMEOUT, REQ_RETRIES);
    const ct = (resp.headers.get('content-type') || '').toLowerCase();
    if (!ct.startsWith('image/')) return false;
    const buf = new Uint8Array(await resp.arrayBuffer());
    // Reject if GIF
    if (buf.length >= 6) {
      const sig = String.fromCharCode(...buf.slice(0, 6));
      if (sig === 'GIF87a' || sig === 'GIF89a') return false;
    }
    return buf.length >= 200; // some minimal size
  } catch {
    return false;
  }
}

function normalize(s) {
  return (s || '')
    .toLowerCase()
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

// Normalize a title for better search: remove parentheticals/brackets and prefer the segment after a colon
function normalizeTitleForSearch(title) {
  if (!title) return title;
  let t = String(title);
  t = t.replace(/\s*\([^)]*\)\s*/g, ' ') // remove parentheticals
       .replace(/\s*\[[^\]]*\]\s*/g, ' ') // remove bracketed notes
       .replace(/\s{2,}/g, ' ') // collapse spaces
       .trim();
  if (t.includes(':')) {
    const parts = t.split(':').map(s => s.trim()).filter(Boolean);
    const last = parts[parts.length - 1];
    if (last && last.length >= 3 && last.length <= 80) t = last;
  }
  return t;
}

async function fetchWithTimeout(url, options = {}, timeoutMs = 10000) {
  const controller = new AbortController();
  const t = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const resp = await fetch(url, { ...options, signal: controller.signal, headers: { 'User-Agent': 'Librarie-CoverFix/1.0', ...(options.headers || {}) } });
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    return resp;
  } finally {
    clearTimeout(t);
  }
}

async function fetchWithRetries(url, options = {}, timeoutMs = 10000, retries = 2) {
  let attempt = 0;
  let lastErr;
  const maxAttempts = (retries ?? 0) + 1;
  while (attempt < maxAttempts) {
    try {
      return await fetchWithTimeout(url, options, timeoutMs);
    } catch (err) {
      lastErr = err;
      attempt++;
      if (attempt >= maxAttempts) break;
      const backoff = 300 * Math.pow(2, attempt - 1);
      await delay(backoff);
    }
  }
  throw lastErr || new Error('fetch failed');
}

// Minimal semicolon-CSV parser/stringifier with quote support
function parseCsvSemicolon(text) {
  const rows = [];
  let i = 0, field = '', inQuotes = false, row = [];
  while (i <= text.length) {
    const ch = text[i] || '\n'; // force flush at end
    if (inQuotes) {
      if (ch === '"') {
        if (text[i + 1] === '"') { field += '"'; i++; }
        else { inQuotes = false; }
      } else { field += ch; }
    } else {
      if (ch === ';') { row.push(field); field = ''; }
      else if (ch === '\r') { /* skip */ }
      else if (ch === '\n') { row.push(field); rows.push(row); row = []; field = ''; }
      else if (ch === '"') { inQuotes = true; }
      else { field += ch; }
    }
    i++;
  }
  // Remove possible trailing empty row from last forced flush
  if (rows.length && rows[rows.length - 1].every(c => c === '')) rows.pop();
  return rows;
}

function stringifyCsvSemicolon(rows) {
  const needsQuote = v => /[";\n\r]/.test(v);
  return rows.map(cols => cols.map(v => {
    const s = String(v ?? '');
    return needsQuote(s) ? '"' + s.replaceAll('"', '""') + '"' : s;
  }).join(';')).join('\n');
}

// Mirror updated CSV content into ./data for the seeder
async function syncToSeederDir(fileName, content) {
  const dstDir = ROOT_DATA;
  try { fs.mkdirSync(dstDir, { recursive: true }); } catch {}
  const dst = path.join(dstDir, fileName);
  try {
    if (fs.existsSync(dst)) {
      const bak = dst + '.' + new Date().toISOString().replaceAll(':', '-').slice(0, 19) + '.bak';
      fs.copyFileSync(dst, bak);
    }
    fs.writeFileSync(dst, content, 'utf8');
    console.log(`Synced ${fileName} to ${dst}`);
  } catch (e) {
    console.warn(`Failed to sync ${fileName} to seeder dir: ${e.message}`);
  }
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
