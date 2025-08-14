#!/usr/bin/env node
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

// Minimal fetch using undici (Node 18+ has global fetch)
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const root = path.resolve(__dirname, '..');
const javaFile = path.join(root, 'backend', 'src', 'main', 'java', 'org', 'roubinet', 'librarie', 'application', 'service', 'DemoDataService.java');
const csvFile = path.join(root, 'amazon-images.csv');

const AMAZON_REGEX = /https?:\/\/(?:www\.)?amazon\.[^\s"')]+/;
const DEBUG = false; // disable verbose logging
const dbg = (...args) => { if (DEBUG) console.log(...args); };

async function readJavaContent() {
  const content = await fs.readFile(javaFile, 'utf8');
  return content;
}

function parseBookArrays(javaContent) {
  // Find arrays like: String[][] name = { {"Title", "Year", "Url", ...}, ... };
  const arrays = [];
  const arrayRegex = /String\[\]\[\]\s+(\w+)\s*=\s*\{([\s\S]*?)\};/g;
  let m;
  while ((m = arrayRegex.exec(javaContent)) !== null) {
    const arrayName = m[1];
    const body = m[2];
    // Determine nearest enclosing function name above this array
    const before = javaContent.slice(0, m.index);
    const funcMatch = /private\s+void\s+(create\w+)\s*\(/i.exec(before.split(/\n/).reverse().join('\n'))
      || /private\s+void\s+(create\w+)\s*\(/i.exec(before);
    const funcName = funcMatch ? funcMatch[1] : '';

    // Extract entries: {"Title", "Year", "URL", ...}
    const entryRegex = /\{\s*"([^"]+)"\s*,\s*"(\d{4})"\s*,\s*"(https?:[^"\n]+)"/g;
    let e;
    while ((e = entryRegex.exec(body)) !== null) {
      arrays.push({
        arrayName,
        funcName,
        title: e[1],
        year: e[2],
        url: e[3]
      });
    }
  }
  return arrays;
}

function authorFor(funcName, arrayName) {
  const f = (funcName || '').toLowerCase();
  const a = (arrayName || '').toLowerCase();
  if (f.includes('tolkien')) return 'J.R.R. Tolkien';
  if (f.includes('stephenking') || f.includes('king')) return 'Stephen King';
  if (f.includes('isaac') || f.includes('asimov')) return 'Isaac Asimov';
  if (f.includes('wheel') || f.includes('time')) {
    if (a.includes('jordan')) return 'Robert Jordan';
    if (a.includes('sanderson')) return 'Brandon Sanderson';
    // default
    return 'Robert Jordan';
  }
  return '';
}

function htmlDecodeAttribute(value) {
  if (!value) return value;
  return value
    .replace(/&quot;/g, '"')
    .replace(/&#34;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&#39;/g, "'")
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>');
}

function extractLandingImageUrl(html) {
  // Try: <img class="landingImage" src="..."> or common fallbacks
  // Amazon often lazy loads, src may be in data-old-hires or data-a-dynamic-image JSON
  let imgMatch = html.match(/<img[^>]*class=["'][^"']*landingImage[^"']*["'][^>]*>/i);
  if (!imgMatch) {
    // Kindle/eBooks specific
    imgMatch = html.match(/<img[^>]*id=["']ebooksImgBlkFront["'][^>]*>/i) ||
               html.match(/<img[^>]*id=["']imgBlkFront["'][^>]*>/i) ||
               html.match(/<img[^>]*id=["']frontImage["'][^>]*>/i) ||
               html.match(/<img[^>]*class=["'][^"']*a-dynamic-image[^"']*["'][^>]*>/i);
  }
  if (!imgMatch) return null;
  const tag = imgMatch[0];
  // Prefer data-old-hires
  const dataOldHires = /data-old-hires=["']([^"']+)["']/i.exec(tag);
  if (dataOldHires?.[1]) return htmlDecodeAttribute(dataOldHires[1]);
  // Next, src
  const src = /\ssrc=["']([^"']+)["']/i.exec(tag);
  if (src?.[1] && !src[1].includes('data:')) return htmlDecodeAttribute(src[1]);
  // Finally try data-a-dynamic-image (JSON map): {"url":[w,h],...} take first key
  const dyn = /data-a-dynamic-image=["'](.*?)["']/i.exec(tag);
  if (dyn?.[1]) {
    try {
      const json = JSON.parse(htmlDecodeAttribute(dyn[1]));
      const first = Object.keys(json)[0];
      if (first) return first;
    } catch {}
  }
  return null;
}

async function fetchHtml(url, opts = {}) {
  dbg('  HTTP GET', url, opts?.referer ? `(Referer: ${opts.referer})` : '');
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 30000);
  try {
    const res = await fetch(url, {
      headers: {
        // Desktop Chrome-like UA, FR locale for amazon.fr
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36',
        'Accept-Language': 'fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
        'Accept-Encoding': 'gzip, deflate, br',
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache',
        'Upgrade-Insecure-Requests': '1',
        'Sec-Fetch-Site': 'same-origin',
        'Sec-Fetch-Mode': 'navigate',
        'Sec-Fetch-User': '?1',
        'Sec-Fetch-Dest': 'document',
        'DNT': '1',
        // Basic locale/currency cookies used by amazon.fr
        'Cookie': 'lc-main=fr_FR; i18n-prefs=EUR',
        ...(opts.referer ? { 'Referer': opts.referer } : {})
      },
      signal: controller.signal
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const html = await res.text();
    // Detect bot/captcha/robot-check pages and treat as 503 to trigger backoff
    if (/captcha|robot check|automated access/i.test(html)) {
      throw new Error('HTTP 503');
    }
  dbg('  HTTP OK', url, `(${html.length} bytes)`);
    return html;
  } finally {
    clearTimeout(timeout);
  }
}

const sleep = (ms) => new Promise(r => setTimeout(r, ms));

function createBookFetchWithBackoff() {
  let retryCount = 0;
  return async function fetchWithBackoff(url, opts) {
    for (;;) {
      try {
  // small jitter to look less bot-like
  await sleep(200 + Math.random() * 400);
  return await fetchHtml(url, opts);
      } catch (err) {
        const msg = String(err?.message || '');
        if (/503/.test(msg)) {
          retryCount++;
          if (retryCount >= 5) {
            console.warn(`[503] Cooldown 60s after 5 retries for ${url}`);
            await sleep(60000);
            retryCount = 0;
          } else {
            console.warn(`[503] Retry #${retryCount} in 5s for ${url}`);
            await sleep(5000);
          }
          continue; // retry
        }
        // Non-503: propagate
        throw err;
      }
    }
  };
}

function canonicalizeAmazonDpUrl(u) {
  try {
    const asin = /\/dp\/([A-Z0-9]{8,})/i.exec(u)?.[1];
    if (asin) return `https://www.amazon.fr/dp/${asin}`;
    // sometimes /gp/aw/d/ASIN
    const aw = /\/gp\/aw\/d\/([A-Z0-9]{8,})/i.exec(u)?.[1];
    if (aw) return `https://www.amazon.fr/dp/${aw}`;
  } catch {}
  return u;
}

async function searchAmazonForBook(title, author, fetchFn = fetchHtml) {
  const q = encodeURIComponent(`${title} ${author} Kindle`);
  const searchUrl = `https://www.amazon.fr/s?k=${q}&i=digital-text`;
  dbg(` Search for: "${title}" by "${author}"`);
  dbg(' Search URL:', searchUrl);
  const html = await fetchFn(searchUrl, { referer: 'https://www.amazon.fr/' });

  // Prefer parsing s-search-result blocks (only)
  const candidates = [];
  // data-component-type based blocks
  const reBlock1 = /<div[^>]*data-component-type=["']s-search-result["'][^>]*data-asin=["']([A-Z0-9]{10})["'][^>]*>([\s\S]*?)<\/div>/gi;
  for (let m; (m = reBlock1.exec(html)); ) {
    const asin = m[1];
    const block = m[2];
    const t = /<span[^>]*class=["'][^"']*a-size-medium[^"']*["'][^>]*>([^<]+)<\/span>/i.exec(block)?.[1]
           || /<h2[^>]*>\s*(?:<a[^>]*>)?\s*(?:<span[^>]*>)?([^<]+)</i.exec(block)?.[1]
           || '';
    const text = t.trim();
    if (!text) continue;
    const href = `https://www.amazon.fr/dp/${asin}`;
    candidates.push({ href, text });
  }
  // class-based s-search-result (if present) with inner data-asin
  if (!candidates.length) {
    const reBlock2 = /<div[^>]*class=["'][^"']*s-search-result[^"']*["'][^>]*>([\s\S]*?)<\/div>/gi;
    for (let m; (m = reBlock2.exec(html)); ) {
      const block = m[1];
      const asin = /data-asin=["']([A-Z0-9]{10})["']/i.exec(block)?.[1];
      if (!asin) continue;
      const t = /<span[^>]*class=["'][^"']*a-size-medium[^"']*["'][^>]*>([^<]+)<\/span>/i.exec(block)?.[1]
             || /<h2[^>]*>\s*(?:<a[^>]*>)?\s*(?:<span[^>]*>)?([^<]+)</i.exec(block)?.[1]
             || '';
      const text = t.trim();
      if (!text) continue;
      const href = `https://www.amazon.fr/dp/${asin}`;
      candidates.push({ href, text });
    }
  }

  dbg(` Found ${candidates.length} candidates`);
  candidates.slice(0, 5).forEach((c, i) => dbg(`  [${i}]`, c.text, '->', c.href));
  if (!candidates.length) return null;

  // Case-insensitive inclusion match on title only
  const target = (title || '').toLowerCase();
  const best = candidates.find(c => (c.text || '').toLowerCase().includes(target));
  dbg(' Match target (case-insensitive):', target);
  if (best) {
    dbg(' Chosen candidate:', best.text, '->', best.href);
  } else {
    dbg(' No candidate matched the title (case-insensitive)');
  }
  if (!best) return null;
  return { url: best.href, searchUrl };
}

function normalize(str) {
  return (str || '')
    .toLowerCase()
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '') // remove diacritics
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

function stripLeadingArticle(str) {
  if (!str) return '';
  // Remove common leading English articles: The, A, An (ignore case)
  return String(str).trim().replace(/^(?:the|a|an)\s+/i, '');
}

function stripCommonArticles(str) {
  if (!str) return '';
  const words = ['the', 'a', 'an', 'of', 'and'];
  const re = new RegExp(`\\b(?:${words.join('|')})\\b`, 'gi');
  return String(str).replace(re, ' ').replace(/\s+/g, ' ').trim();
}

function pickFromSrcset(attr) {
  if (!attr) return '';
  const parts = attr.split(',').map(s => s.trim()).filter(Boolean);
  if (!parts.length) return '';
  const last = parts[parts.length - 1];
  const url = last.split(/\s+/)[0];
  return url || '';
}

function extractImageForTitle(html, title) {
  const titleLower = (title || '').toLowerCase();
  // Collect all image tags with an alt attribute
  const tags = html.match(/<img[^>]*alt=["'][^"']+["'][^>]*>/gi) || [];
  for (const tag of tags) {
    const alt = /alt=["']([^"']+)["']/i.exec(tag)?.[1] || '';
    const altLower = alt.toLowerCase();
    if (!altLower) continue;
    // alt must include the title (ignore case only)
    if (!altLower.includes(titleLower)) continue;

    // Prefer data-old-hires
    const dataOldHires = /data-old-hires=["']([^"']+)["']/i.exec(tag)?.[1];
    if (dataOldHires) return htmlDecodeAttribute(dataOldHires);
    // Try src
    const src = /\ssrc=["']([^"']+)["']/i.exec(tag)?.[1];
    if (src && /^https?:/i.test(src)) return htmlDecodeAttribute(src);
    // Try dynamic image json
    const dyn = /data-a-dynamic-image=["'](.*?)["']/i.exec(tag)?.[1];
    if (dyn) {
      try {
        const json = JSON.parse(htmlDecodeAttribute(dyn));
        const first = Object.keys(json)[0];
        if (first) return first;
      } catch {}
    }
  }
  return null;
}

function extractFromScripts(html) {
  // Try to find hiRes/mainImageUrl/twitter image
  const candidates = [];
  // og:image
  const og = /<meta\s+property=["']og:image["']\s+content=["']([^"']+)["'][^>]*>/i.exec(html);
  if (og?.[1]) candidates.push(og[1]);
  // twitter:image
  const tw = /<meta\s+name=["']twitter:image["']\s+content=["']([^"']+)["'][^>]*>/i.exec(html);
  if (tw?.[1]) candidates.push(tw[1]);
  // hiRes
  const hiRes = new RegExp('"hiRes"\\s*:\\s*"(https?:\\\\/\\\\/[^"\\\\]+)"', 'i').exec(html);
  if (hiRes?.[1]) candidates.push(hiRes[1].replace(/\\\//g, '/'));
  // mainImageUrl
  const mainImg = new RegExp('"mainImageUrl"\\s*:\\s*"(https?:\\\\/\\\\/[^"\\\\]+)"', 'i').exec(html);
  if (mainImg?.[1]) candidates.push(mainImg[1].replace(/\\\//g, '/'));
  // imageGalleryData first hiRes
  const gallery = new RegExp('"imageGalleryData"\\s*:\\s*\\[(.*?)\\]', 'is').exec(html);
  if (gallery?.[1]) {
    const m = new RegExp('"hiRes"\\s*:\\s*"(https?:\\\\/\\\\/[^"\\\\]+)"', 'i').exec(gallery[1]);
    if (m?.[1]) candidates.push(m[1].replace(/\\\//g, '/'));
  }
  // Return first plausible
  return candidates.find(u => /https?:\/\//.test(u)) || null;
}

function findProductHrefByHyphenatedTitle(html, title) {
  const stripped = stripCommonArticles(title || '');
  const needle = stripped.toLowerCase().replace(/\s+/g, '-');
  dbg('  HREF match needle (lower/hyphenated):', needle);
  const reA = /<a\s+[^>]*href=["']([^"']+)["'][^>]*>([\s\S]*?)<\/a>/gi;
  for (let m; (m = reA.exec(html)); ) {
    let href = m[1] || '';
    if (!href) continue;
  const aTag = m[0] || '';
    const cls = /class=["']([^"']+)["']/i.exec(aTag)?.[1]?.trim() || '';
    const requiredClass = 'a-link-normal s-line-clamp-2 s-line-clamp-3-for-col-12 s-link-style a-text-normal';
    const classExact = cls === requiredClass;
    if (!classExact) {
      continue;
    }
    const hrefLower = href.toLowerCase();
    const includes = hrefLower.includes(needle);

    dbg('   Anchor href (lower) =', hrefLower);
    dbg('   Match needle      =', needle);
    if (includes) {
      dbg('   HREF includes needle\n     hrefLower =', hrefLower, '\n     needle    =', needle);
    } else {
      continue;
    }
    // Normalize to absolute URL on amazon.fr
    if (href.startsWith('/')) href = `https://www.amazon.fr${href}`;
    // Prefer canonical /dp/ASIN if present
    href = canonicalizeAmazonDpUrl(href);
    dbg('  Matched HREF:', href);
    return href;
  }
  return '';
}

function extractImageFromSearchByHref(html, title) {
  const needle = (title || '').toLowerCase().replace(/\s+/g, '-');
  dbg('  HREF match needle (lower/hyphenated):', needle);
  const reBlock = /<div[^>]*data-component-type=["']s-search-result["'][^>]*>([\s\S]*?)<\/div>/gi;
  for (let m; (m = reBlock.exec(html)); ) {
    const block = m[1];
    // Collect anchors within block
    const anchors = block.match(/<a\s+[^>]*href=["']([^"']+)["'][^>]*>/gi) || [];
    let hrefMatch = '';
    for (const aTag of anchors) {
      const href = /href=["']([^"']+)["']/i.exec(aTag)?.[1] || '';
      if (!href) continue;
      if (href.toLowerCase().includes(needle)) {
        hrefMatch = href;
        break;
      }
    }
    if (!hrefMatch) continue;
    dbg('  Matched block by href:', hrefMatch);
    // From the matched block, get the product image
    const imgTag = block.match(/<img[^>]*class=["'][^"']*s-image[^"']*["'][^>]*>/i)?.[0] || '';
    if (!imgTag) continue;
    const srcset = /\ssrcset=["']([^"']+)["']/i.exec(imgTag)?.[1];
    const src = /\ssrc=["']([^"']+)["']/i.exec(imgTag)?.[1];
    const chosen = pickFromSrcset(srcset) || src || '';
    if (chosen) return htmlDecodeAttribute(chosen);
  }
  return '';
}

function toVariants(url) {
  const variants = [url];
  const asin = /\/dp\/([A-Z0-9]{8,})/i.exec(url)?.[1];
  if (asin) {
    const base = `https://www.amazon.fr/dp/${asin}`;
    variants.push(base);
    variants.push(`https://www.amazon.fr/gp/aw/d/${asin}`);
  }
  return Array.from(new Set(variants));
}

async function buildCsv(rows) {
  const header = 'source_url,image_url';
  const esc = v => {
    if (v == null) return '';
    const s = String(v);
    return /[",\n]/.test(s) ? '"' + s.replace(/"/g, '""') + '"' : s;
  };
  const lines = [header, ...rows.map(r => `${esc(r.source)},${esc(r.image ?? '')}`)];
  await fs.writeFile(csvFile, lines.join('\n'), 'utf8');
}

function replaceUrlsInJava(javaContent, mapping) {
  let updated = javaContent;
  for (const [src, img] of mapping.entries()) {
    if (!img) continue;
    // Replace the URL inside quotes only
    const escaped = src.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const re = new RegExp(`(["'])${escaped}(["'])`, 'g');
    updated = updated.replace(re, `$1${img}$2`);
  }
  return updated;
}

async function main() {
  const content = await readJavaContent();
  const entries = parseBookArrays(content);
  if (!entries.length) {
    console.log('No book arrays found.');
    return;
  }
  dbg(`Found ${entries.length} book entries`);
  // Sequential processing (no parallelization)

  async function processEntry(e) {
    const srcUrl = e.url;
    const fetchForBook = createBookFetchWithBackoff();
    let image = '';
    dbg(`\nProcessing entry: "${e.title}" (${e.year})`);
    dbg(' Source URL:', srcUrl);
    if (AMAZON_REGEX.test(srcUrl)) {
      // Direct amazon URL
      dbg(' Detected direct Amazon URL');
      let img = null;
      let lastErr = null;
      const tried = [];
      const variants = toVariants(srcUrl);
      dbg(' Variants to try:', variants.join(', '));
      for (const v of toVariants(srcUrl)) {
        try {
          // Use a generic Amazon referer to mimic in-site navigation
          tried.push(v);
          const html = await fetchForBook(v, { referer: 'https://www.amazon.fr/' });
          // Prefer alt-matching image for title, then fall back
          img = extractImageForTitle(html, e.title);
          if (img) {
            dbg('  Image found by alt-title match');
          } else {
            img = extractLandingImageUrl(html);
            if (img) dbg('  Image found by landingImage/fallback tag');
          }
          if (!img) {
            img = extractFromScripts(html);
            if (img) dbg('  Image found by meta/script fallbacks');
          }
          if (img) break;
        } catch (err) {
          lastErr = err;
        }
      }
  if (!img && lastErr) dbg(`FAIL ${srcUrl}: ${lastErr.message}`);
      image = img || '';
  if (!image) dbg(`  Tried product URLs: ${tried.join(', ')}`);
    } else {
      // Non-amazon: search by title + author (only via s-search-result image)
      const author = authorFor(e.funcName, e.arrayName);
      dbg(' Detected non-Amazon URL, will search Amazon for href match');
      let productUrl = '';
      try {
        const q = encodeURIComponent(`${e.title} ${author} Kindle`);
        const searchUrl = `https://www.amazon.fr/s?k=${q}&i=digital-text`;
        const searchHtml = await fetchForBook(searchUrl, { referer: 'https://www.amazon.fr/' });
        dbg(`  Search URL: ${searchUrl}`);
        productUrl = findProductHrefByHyphenatedTitle(searchHtml, e.title);
        if (productUrl) {
          dbg(`  Product URL: ${productUrl}`);
          try {
            const html = await fetchForBook(productUrl, { referer: searchUrl });
            let img = extractImageForTitle(html, e.title);
            if (!img) {
              img = extractLandingImageUrl(html);
              if (img) dbg('  Product image via landingImage/fallback');
            } else {
              dbg('  Product image via alt-title match');
            }
            if (!img) {
              img = extractFromScripts(html);
              if (img) dbg('  Product image via meta/script fallbacks');
            }
            image = img || '';
          } catch (err) {
            dbg(`Product fetch FAIL ${productUrl}: ${err.message}`);
          }
        }
      } catch (err) {
        dbg(`Search FAIL ${e.title} by ${author}: ${err.message}`);
      }
      if (!image) {
        const q = encodeURIComponent(`${e.title} ${author} Kindle`);
        dbg(`  Search URL: https://www.amazon.fr/s?k=${q}&i=digital-text`);
        if (productUrl) dbg(`  Product URL: ${productUrl}`);
        dbg(' Reason: no href containing the hyphenated title or no image on product');
      }
    }
    // Unified minimal output per entry
    console.log(`"${e.title}" -> ${image || 'NOT FOUND'}`);
    return { source: srcUrl, image };
  }

  const results = [];
  for (let i = 0; i < entries.length; i++) {
    try {
      const r = await processEntry(entries[i]);
      results.push(r);
    } catch {
      results.push(null);
    }
  }
  const mapping = new Map(); // source url -> image url
  for (const r of results) {
    if (!r) continue;
    mapping.set(r.source, r.image || null);
  }

  await buildCsv(results);
  dbg(`CSV written: ${path.relative(root, csvFile)}`);

  // Read CSV back and build mapping from it (as required)
  const csv = await fs.readFile(csvFile, 'utf8');
  const lines = csv.split(/\r?\n/).filter(Boolean);
  const outMap = new Map();
  for (let i = 1; i < lines.length; i++) {
    const line = lines[i];
    const parts = line.match(/([^",]+|"(?:[^"]|"")*")(?:,|$)/g)?.map(s => s.replace(/,$/, '')) || [];
    const unq = s => {
      if (s == null) return '';
      let t = s.trim();
      if (t.startsWith('"') && t.endsWith('"')) {
        t = t.slice(1, -1).replace(/""/g, '"');
      }
      return t;
    };
    const source = unq(parts[0]);
    const image = unq(parts[1] || '');
    if (source) outMap.set(source, image || null);
  }

  const updated = replaceUrlsInJava(content, outMap);
  if (updated !== content) {
    await fs.writeFile(javaFile, updated, 'utf8');
  dbg('DemoDataService.java updated with image URLs.');
  } else {
  dbg('No replacements made in DemoDataService.java.');
  }

}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
