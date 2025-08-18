#!/usr/bin/env node
/**
 * Augments backend/data/books.csv with additional volumes for known series.
 * - Looks up ISBNs and cover URLs via OpenLibrary Search API by title + author
 * - Appends rows in semicolon-delimited format matching existing schema
 * - Keeps IDs unique (b-0002-30xx-0000-00000004xxx block)
 *
 * Usage: node scripts/fill-missing-series-books.mjs [--dry-run]
 */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const WORKSPACE = path.resolve(__dirname, '..');
const BOOKS_CSV = path.resolve(WORKSPACE, 'backend', 'data', 'books.csv');

const DRY_RUN = process.argv.includes('--dry-run');

// Small sleep helper to avoid hitting API too hard
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// Prefer 13-digit ISBN if present
function pickIsbn(isbns) {
  if (!Array.isArray(isbns) || isbns.length === 0) return null;
  const isbn13 = isbns.find((i) => /^(97[89])\d{10}$/.test(i));
  if (isbn13) return isbn13;
  const isbn10 = isbns.find((i) => /^\d{9}[\dXx]$/.test(i));
  return isbn10 || isbns[0];
}

async function lookupOpenLibrary({ title, author }) {
  const url = new URL('https://openlibrary.org/search.json');
  url.searchParams.set('title', title);
  if (author) url.searchParams.set('author', author);
  url.searchParams.set('limit', '1');

  const res = await fetch(url, { headers: { 'User-Agent': 'LibrarieSeeder/1.0' } });
  if (!res.ok) {
    return { isbn: null, cover_url: '' };
  }
  const data = await res.json();
  const doc = data?.docs?.[0];
  if (!doc) return { isbn: null, cover_url: '' };
  const isbn = pickIsbn(doc.isbn);
  const cover_url = isbn ? `https://covers.openlibrary.org/b/isbn/${isbn}-L.jpg` : '';
  return { isbn, cover_url };
}

// Minimal CSV escaper for semicolon-delimited fields; double quotes if needed
function csvField(v) {
  if (v == null) return '';
  const s = String(v);
  if (s.includes(';') || s.includes('"') || s.includes('\n')) {
    return '"' + s.replaceAll('"', '""') + '"';
  }
  return s;
}

function appendRows(file, rows) {
  const content = rows.map((r) => r.join(';')).join('\n');
  fs.appendFileSync(file, '\n' + content + '\n', 'utf8');
}

// Parse a semicolon-delimited line respecting double quotes
function splitSemicolonCsv(line) {
  const out = [];
  let cur = '';
  let inQuotes = false;
  for (let i = 0; i < line.length; i++) {
    const ch = line[i];
    if (ch === '"') {
      if (inQuotes && line[i + 1] === '"') {
        cur += '"';
        i++;
      } else {
        inQuotes = !inQuotes;
      }
    } else if (ch === ';' && !inQuotes) {
      out.push(cur);
      cur = '';
    } else {
      cur += ch;
    }
  }
  out.push(cur);
  return out;
}

function loadExistingTitleSeriesKeys(file) {
  const txt = fs.readFileSync(file, 'utf8');
  const lines = txt.split(/\r?\n/).filter((l) => l.trim().length > 0);
  const header = lines.shift(); // discard header
  const keys = new Set();
  for (const line of lines) {
    const cols = splitSemicolonCsv(line);
    // book_uuid;title;year;cover_url;synopsis;author_name;author_uuid;series_name;series_uuid;series_index;isbn;language
    if (cols.length >= 12) {
      const title = cols[1]?.trim();
      const seriesUuid = cols[8]?.trim();
      if (title && seriesUuid) {
        keys.add(`${seriesUuid}::${title.toLowerCase()}`);
      }
    }
  }
  return keys;
}

// Targets: extend key series with canonical next volumes
const targets = [
  // The Lord of the Rings
  { title: 'The Two Towers', author: 'J. R. R. Tolkien', year: 1954, series_name: 'The Lord of the Rings', series_uuid: 'e-0002-0018-0000-000000020018', series_index: 2, author_uuid: 'a-0002-0022-0000-000000010142', author_name: 'J. R. R. Tolkien', language: 'en-GB' },
  { title: 'The Return of the King', author: 'J. R. R. Tolkien', year: 1955, series_name: 'The Lord of the Rings', series_uuid: 'e-0002-0018-0000-000000020018', series_index: 3, author_uuid: 'a-0002-0022-0000-000000010142', author_name: 'J. R. R. Tolkien', language: 'en-GB' },

  // A Song of Ice and Fire
  { title: 'A Clash of Kings', author: 'George R. R. Martin', year: 1998, series_name: 'A Song of Ice and Fire', series_uuid: 'e-0002-0019-0000-000000020019', series_index: 2, author_uuid: 'a-0002-0023-0000-000000010143', author_name: 'George R. R. Martin', language: 'en-US' },
  { title: 'A Storm of Swords', author: 'George R. R. Martin', year: 2000, series_name: 'A Song of Ice and Fire', series_uuid: 'e-0002-0019-0000-000000020019', series_index: 3, author_uuid: 'a-0002-0023-0000-000000010143', author_name: 'George R. R. Martin', language: 'en-US' },
  { title: 'A Feast for Crows', author: 'George R. R. Martin', year: 2005, series_name: 'A Song of Ice and Fire', series_uuid: 'e-0002-0019-0000-000000020019', series_index: 4, author_uuid: 'a-0002-0023-0000-000000010143', author_name: 'George R. R. Martin', language: 'en-US' },
  { title: 'A Dance with Dragons', author: 'George R. R. Martin', year: 2011, series_name: 'A Song of Ice and Fire', series_uuid: 'e-0002-0019-0000-000000020019', series_index: 5, author_uuid: 'a-0002-0023-0000-000000010143', author_name: 'George R. R. Martin', language: 'en-US' },

  // Wheel of Time
  { title: 'The Great Hunt', author: 'Robert Jordan', year: 1990, series_name: 'The Wheel of Time', series_uuid: 'e-0002-001a-0000-00000002001a', series_index: 2, author_uuid: 'a-0002-0024-0000-000000010144', author_name: 'Robert Jordan', language: 'en-US' },
  { title: 'The Dragon Reborn', author: 'Robert Jordan', year: 1991, series_name: 'The Wheel of Time', series_uuid: 'e-0002-001a-0000-00000002001a', series_index: 3, author_uuid: 'a-0002-0024-0000-000000010144', author_name: 'Robert Jordan', language: 'en-US' },
  { title: 'The Shadow Rising', author: 'Robert Jordan', year: 1992, series_name: 'The Wheel of Time', series_uuid: 'e-0002-001a-0000-00000002001a', series_index: 4, author_uuid: 'a-0002-0024-0000-000000010144', author_name: 'Robert Jordan', language: 'en-US' },

  // Stormlight Archive
  { title: 'Words of Radiance', author: 'Brandon Sanderson', year: 2014, series_name: 'The Stormlight Archive', series_uuid: 'e-0002-001b-0000-00000002001b', series_index: 2, author_uuid: 'a-0002-0025-0000-000000010145', author_name: 'Brandon Sanderson', language: 'en-US' },
  { title: 'Oathbringer', author: 'Brandon Sanderson', year: 2017, series_name: 'The Stormlight Archive', series_uuid: 'e-0002-001b-0000-00000002001b', series_index: 3, author_uuid: 'a-0002-0025-0000-000000010145', author_name: 'Brandon Sanderson', language: 'en-US' },
  { title: 'Rhythm of War', author: 'Brandon Sanderson', year: 2020, series_name: 'The Stormlight Archive', series_uuid: 'e-0002-001b-0000-00000002001b', series_index: 4, author_uuid: 'a-0002-0025-0000-000000010145', author_name: 'Brandon Sanderson', language: 'en-US' },

  // Malazan Book of the Fallen
  { title: 'Deadhouse Gates', author: 'Steven Erikson', year: 2000, series_name: 'Malazan Book of the Fallen', series_uuid: 'e-0002-001e-0000-00000002001e', series_index: 2, author_uuid: 'a-0002-0027-0000-000000010147', author_name: 'Steven Erikson', language: 'en-GB' },
  { title: 'Memories of Ice', author: 'Steven Erikson', year: 2001, series_name: 'Malazan Book of the Fallen', series_uuid: 'e-0002-001e-0000-00000002001e', series_index: 3, author_uuid: 'a-0002-0027-0000-000000010147', author_name: 'Steven Erikson', language: 'en-GB' },

  // Mistborn (Era One)
  { title: 'The Well of Ascension', author: 'Brandon Sanderson', year: 2007, series_name: 'Mistborn (Era One)', series_uuid: 'e-0002-001f-0000-00000002001f', series_index: 2, author_uuid: 'a-0002-0025-0000-000000010145', author_name: 'Brandon Sanderson', language: 'en-US' },
  { title: 'The Hero of Ages', author: 'Brandon Sanderson', year: 2008, series_name: 'Mistborn (Era One)', series_uuid: 'e-0002-001f-0000-00000002001f', series_index: 3, author_uuid: 'a-0002-0025-0000-000000010145', author_name: 'Brandon Sanderson', language: 'en-US' },

  // The Kingkiller Chronicle
  { title: 'The Wise Man\'s Fear', author: 'Patrick Rothfuss', year: 2011, series_name: 'The Kingkiller Chronicle', series_uuid: 'e-0002-0020-0000-000000020020', series_index: 2, author_uuid: 'a-0002-0028-0000-000000010148', author_name: 'Patrick Rothfuss', language: 'en-US' },

  // Gentlemen Bastard Sequence
  { title: 'Red Seas Under Red Skies', author: 'Scott Lynch', year: 2007, series_name: 'Gentlemen Bastard Sequence', series_uuid: 'e-0002-0021-0000-000000020021', series_index: 2, author_uuid: 'a-0002-0029-0000-000000010149', author_name: 'Scott Lynch', language: 'en-GB' },
  { title: 'The Republic of Thieves', author: 'Scott Lynch', year: 2013, series_name: 'Gentlemen Bastard Sequence', series_uuid: 'e-0002-0021-0000-000000020021', series_index: 3, author_uuid: 'a-0002-0029-0000-000000010149', author_name: 'Scott Lynch', language: 'en-GB' },

  // The Broken Earth
  { title: 'The Obelisk Gate', author: 'N. K. Jemisin', year: 2016, series_name: 'The Broken Earth', series_uuid: 'e-0002-0022-0000-000000020022', series_index: 2, author_uuid: 'a-0002-002a-0000-000000010150', author_name: 'N. K. Jemisin', language: 'en-US' },
  { title: 'The Stone Sky', author: 'N. K. Jemisin', year: 2017, series_name: 'The Broken Earth', series_uuid: 'e-0002-0022-0000-000000020022', series_index: 3, author_uuid: 'a-0002-002a-0000-000000010150', author_name: 'N. K. Jemisin', language: 'en-US' },

  // The Belgariad
  { title: 'Queen of Sorcery', author: 'David Eddings', year: 1982, series_name: 'The Belgariad', series_uuid: 'e-0002-0023-0000-000000020023', series_index: 2, author_uuid: 'a-0002-002b-0000-000000010151', author_name: 'David Eddings', language: 'en-US' },
  { title: 'Magician\'s Gambit', author: 'David Eddings', year: 1983, series_name: 'The Belgariad', series_uuid: 'e-0002-0023-0000-000000020023', series_index: 3, author_uuid: 'a-0002-002b-0000-000000010151', author_name: 'David Eddings', language: 'en-US' },
  { title: 'Castle of Wizardry', author: 'David Eddings', year: 1984, series_name: 'The Belgariad', series_uuid: 'e-0002-0023-0000-000000020023', series_index: 4, author_uuid: 'a-0002-002b-0000-000000010151', author_name: 'David Eddings', language: 'en-US' },
  { title: 'Enchanters\' End Game', author: 'David Eddings', year: 1984, series_name: 'The Belgariad', series_uuid: 'e-0002-0023-0000-000000020023', series_index: 5, author_uuid: 'a-0002-002b-0000-000000010151', author_name: 'David Eddings', language: 'en-US' },

  // Riftwar (start with the original saga follow-ups)
  { title: 'Silverthorn', author: 'Raymond E. Feist', year: 1985, series_name: 'Riftwar Cycle', series_uuid: 'e-0002-0024-0000-000000020024', series_index: 2, author_uuid: 'a-0002-002c-0000-000000010152', author_name: 'Raymond E. Feist', language: 'en-US' },
  { title: 'A Darkness at Sethanon', author: 'Raymond E. Feist', year: 1986, series_name: 'Riftwar Cycle', series_uuid: 'e-0002-0024-0000-000000020024', series_index: 3, author_uuid: 'a-0002-002c-0000-000000010152', author_name: 'Raymond E. Feist', language: 'en-US' },

  // The Dark Tower
  { title: 'The Drawing of the Three', author: 'Stephen King', year: 1987, series_name: 'The Dark Tower', series_uuid: 'e-0002-0025-0000-000000020025', series_index: 2, author_uuid: 'a-0002-002d-0000-000000010153', author_name: 'Stephen King', language: 'en-US' },
  { title: 'The Waste Lands', author: 'Stephen King', year: 1991, series_name: 'The Dark Tower', series_uuid: 'e-0002-0025-0000-000000020025', series_index: 3, author_uuid: 'a-0002-002d-0000-000000010153', author_name: 'Stephen King', language: 'en-US' },

  // The Dresden Files
  { title: 'Fool Moon', author: 'Jim Butcher', year: 2001, series_name: 'The Dresden Files', series_uuid: 'e-0002-0026-0000-000000020026', series_index: 2, author_uuid: 'a-0002-002e-0000-000000010154', author_name: 'Jim Butcher', language: 'en-US' },
  { title: 'Grave Peril', author: 'Jim Butcher', year: 2001, series_name: 'The Dresden Files', series_uuid: 'e-0002-0026-0000-000000020026', series_index: 3, author_uuid: 'a-0002-002e-0000-000000010154', author_name: 'Jim Butcher', language: 'en-US' },

  // The Lightbringer Series
  { title: 'The Blinding Knife', author: 'Brent Weeks', year: 2012, series_name: 'The Lightbringer Series', series_uuid: 'e-0002-0027-0000-000000020027', series_index: 2, author_uuid: 'a-0002-002f-0000-000000010155', author_name: 'Brent Weeks', language: 'en-US' },
  { title: 'The Broken Eye', author: 'Brent Weeks', year: 2014, series_name: 'The Lightbringer Series', series_uuid: 'e-0002-0027-0000-000000020027', series_index: 3, author_uuid: 'a-0002-002f-0000-000000010155', author_name: 'Brent Weeks', language: 'en-US' },
  { title: 'The Blood Mirror', author: 'Brent Weeks', year: 2016, series_name: 'The Lightbringer Series', series_uuid: 'e-0002-0027-0000-000000020027', series_index: 4, author_uuid: 'a-0002-002f-0000-000000010155', author_name: 'Brent Weeks', language: 'en-US' },
  { title: 'The Burning White', author: 'Brent Weeks', year: 2019, series_name: 'The Lightbringer Series', series_uuid: 'e-0002-0027-0000-000000020027', series_index: 5, author_uuid: 'a-0002-002f-0000-000000010155', author_name: 'Brent Weeks', language: 'en-US' },

  // The Powder Mage Trilogy
  { title: 'The Crimson Campaign', author: 'Brian McClellan', year: 2014, series_name: 'The Powder Mage Trilogy', series_uuid: 'e-0002-0028-0000-000000020028', series_index: 2, author_uuid: 'a-0002-0030-0000-000000010156', author_name: 'Brian McClellan', language: 'en-US' },
  { title: 'The Autumn Republic', author: 'Brian McClellan', year: 2015, series_name: 'The Powder Mage Trilogy', series_uuid: 'e-0002-0028-0000-000000020028', series_index: 3, author_uuid: 'a-0002-0030-0000-000000010156', author_name: 'Brian McClellan', language: 'en-US' },

  // The Prince of Nothing
  { title: 'The Warrior-Prophet', author: 'R. Scott Bakker', year: 2004, series_name: 'The Prince of Nothing', series_uuid: 'e-0002-0029-0000-000000020029', series_index: 2, author_uuid: 'a-0002-0031-0000-000000010157', author_name: 'R. Scott Bakker', language: 'en-US' },
  { title: 'The Thousandfold Thought', author: 'R. Scott Bakker', year: 2006, series_name: 'The Prince of Nothing', series_uuid: 'e-0002-0029-0000-000000020029', series_index: 3, author_uuid: 'a-0002-0031-0000-000000010157', author_name: 'R. Scott Bakker', language: 'en-US' },

  // The Faithful and the Fallen
  { title: 'Valour', author: 'John Gwynne', year: 2014, series_name: 'The Faithful and the Fallen', series_uuid: 'e-0002-002a-0000-00000002002a', series_index: 2, author_uuid: 'a-0002-0032-0000-000000010158', author_name: 'John Gwynne', language: 'en-GB' },
  { title: 'Ruin', author: 'John Gwynne', year: 2015, series_name: 'The Faithful and the Fallen', series_uuid: 'e-0002-002a-0000-00000002002a', series_index: 3, author_uuid: 'a-0002-0032-0000-000000010158', author_name: 'John Gwynne', language: 'en-GB' },
  { title: 'Wrath', author: 'John Gwynne', year: 2016, series_name: 'The Faithful and the Fallen', series_uuid: 'e-0002-002a-0000-00000002002a', series_index: 4, author_uuid: 'a-0002-0032-0000-000000010158', author_name: 'John Gwynne', language: 'en-GB' },

  // Shannara Trilogy
  { title: 'The Elfstones of Shannara', author: 'Terry Brooks', year: 1982, series_name: 'Shannara Trilogy', series_uuid: 'e-0002-002b-0000-00000002002b', series_index: 2, author_uuid: 'a-0002-0033-0000-000000010159', author_name: 'Terry Brooks', language: 'en-US' },
  { title: 'The Wishsong of Shannara', author: 'Terry Brooks', year: 1985, series_name: 'Shannara Trilogy', series_uuid: 'e-0002-002b-0000-00000002002b', series_index: 3, author_uuid: 'a-0002-0033-0000-000000010159', author_name: 'Terry Brooks', language: 'en-US' },

  // His Dark Materials
  { title: 'The Subtle Knife', author: 'Philip Pullman', year: 1997, series_name: 'His Dark Materials', series_uuid: 'e-0002-002c-0000-00000002002c', series_index: 2, author_uuid: 'a-0002-0034-0000-000000010160', author_name: 'Philip Pullman', language: 'en-GB' },
  { title: 'The Amber Spyglass', author: 'Philip Pullman', year: 2000, series_name: 'His Dark Materials', series_uuid: 'e-0002-002c-0000-00000002002c', series_index: 3, author_uuid: 'a-0002-0034-0000-000000010160', author_name: 'Philip Pullman', language: 'en-GB' },

  // The Chronicles of Amber
  { title: 'The Guns of Avalon', author: 'Roger Zelazny', year: 1972, series_name: 'The Chronicles of Amber', series_uuid: 'e-0002-002d-0000-00000002002d', series_index: 2, author_uuid: 'a-0001-0038-0000-000000000137', author_name: 'Roger Zelazny', language: 'en-US' },
  { title: 'Sign of the Unicorn', author: 'Roger Zelazny', year: 1975, series_name: 'The Chronicles of Amber', series_uuid: 'e-0002-002d-0000-00000002002d', series_index: 3, author_uuid: 'a-0001-0038-0000-000000000137', author_name: 'Roger Zelazny', language: 'en-US' },

  // Foundation
  { title: 'Foundation and Empire', author: 'Isaac Asimov', year: 1952, series_name: 'Foundation', series_uuid: 'e-0002-002e-0000-00000002002e', series_index: 2, author_uuid: 'a-0002-0035-0000-000000010161', author_name: 'Isaac Asimov', language: 'en-US' },
  { title: 'Second Foundation', author: 'Isaac Asimov', year: 1953, series_name: 'Foundation', series_uuid: 'e-0002-002e-0000-00000002002e', series_index: 3, author_uuid: 'a-0002-0035-0000-000000010161', author_name: 'Isaac Asimov', language: 'en-US' },

  // Dune
  { title: 'Dune Messiah', author: 'Frank Herbert', year: 1969, series_name: 'Dune', series_uuid: 'e-0002-002f-0000-00000002002f', series_index: 2, author_uuid: 'a-0002-0036-0000-000000010162', author_name: 'Frank Herbert', language: 'en-US' },
  { title: 'Children of Dune', author: 'Frank Herbert', year: 1976, series_name: 'Dune', series_uuid: 'e-0002-002f-0000-00000002002f', series_index: 3, author_uuid: 'a-0002-0036-0000-000000010162', author_name: 'Frank Herbert', language: 'en-US' },

  // The Expanse
  { title: "Caliban's War", author: 'James S. A. Corey', year: 2012, series_name: 'The Expanse', series_uuid: 'e-0002-0030-0000-000000020030', series_index: 2, author_uuid: 'a-0002-0037-0000-000000010163', author_name: 'James S. A. Corey', language: 'en-US' },
  { title: "Abaddon's Gate", author: 'James S. A. Corey', year: 2013, series_name: 'The Expanse', series_uuid: 'e-0002-0030-0000-000000020030', series_index: 3, author_uuid: 'a-0002-0037-0000-000000010163', author_name: 'James S. A. Corey', language: 'en-US' },

  // Dragonlance Chronicles
  { title: 'Dragons of Winter Night', author: 'Margaret Weis', year: 1985, series_name: 'Dragonlance Chronicles', series_uuid: 'e-0002-0031-0000-000000020031', series_index: 2, author_uuid: 'a-0002-0038-0000-000000010164', author_name: 'Margaret Weis & Tracy Hickman', language: 'en-US' },
  { title: 'Dragons of Spring Dawning', author: 'Margaret Weis', year: 1985, series_name: 'Dragonlance Chronicles', series_uuid: 'e-0002-0031-0000-000000020031', series_index: 3, author_uuid: 'a-0002-0038-0000-000000010164', author_name: 'Margaret Weis & Tracy Hickman', language: 'en-US' },

  // Percy Jackson (original pentalogy)
  { title: 'The Sea of Monsters', author: 'Rick Riordan', year: 2006, series_name: 'Percy Jackson & the Olympians', series_uuid: 'e-0002-0032-0000-000000020032', series_index: 2, author_uuid: 'a-0002-0039-0000-000000010165', author_name: 'Rick Riordan', language: 'en-US' },
  { title: "The Titan's Curse", author: 'Rick Riordan', year: 2007, series_name: 'Percy Jackson & the Olympians', series_uuid: 'e-0002-0032-0000-000000020032', series_index: 3, author_uuid: 'a-0002-0039-0000-000000010165', author_name: 'Rick Riordan', language: 'en-US' },
  { title: 'The Battle of the Labyrinth', author: 'Rick Riordan', year: 2008, series_name: 'Percy Jackson & the Olympians', series_uuid: 'e-0002-0032-0000-000000020032', series_index: 4, author_uuid: 'a-0002-0039-0000-000000010165', author_name: 'Rick Riordan', language: 'en-US' },
  { title: 'The Last Olympian', author: 'Rick Riordan', year: 2009, series_name: 'Percy Jackson & the Olympians', series_uuid: 'e-0002-0032-0000-000000020032', series_index: 5, author_uuid: 'a-0002-0039-0000-000000010165', author_name: 'Rick Riordan', language: 'en-US' },

  // The Inheritance Cycle
  { title: 'Eldest', author: 'Christopher Paolini', year: 2005, series_name: 'The Inheritance Cycle', series_uuid: 'e-0002-0033-0000-000000020033', series_index: 2, author_uuid: 'a-0002-003a-0000-000000010166', author_name: 'Christopher Paolini', language: 'en-US' },
  { title: 'Brisingr', author: 'Christopher Paolini', year: 2008, series_name: 'The Inheritance Cycle', series_uuid: 'e-0002-0033-0000-000000020033', series_index: 3, author_uuid: 'a-0002-003a-0000-000000010166', author_name: 'Christopher Paolini', language: 'en-US' },
  { title: 'Inheritance', author: 'Christopher Paolini', year: 2011, series_name: 'The Inheritance Cycle', series_uuid: 'e-0002-0033-0000-000000020033', series_index: 4, author_uuid: 'a-0002-003a-0000-000000010166', author_name: 'Christopher Paolini', language: 'en-US' },
];

function nextIdFactory(start = 40001) {
  let n = start;
  return () => {
    const seq = String(n).padStart(5, '0');
    const mid = String(3000 + Math.floor(n / 100)).padStart(4, '0'); // not meaningful, just styled
    const id = `b-0002-${mid}-0000-00000004${seq}`;
    n += 1;
    return id;
  };
}

async function main() {
  if (!fs.existsSync(BOOKS_CSV)) {
    console.error('books.csv not found at', BOOKS_CSV);
    process.exit(1);
  }

  const genId = nextIdFactory(1);
  const existing = loadExistingTitleSeriesKeys(BOOKS_CSV);

  const rows = [];
  for (const t of targets) {
    try {
      await sleep(200); // throttle a bit
      const key = `${t.series_uuid}::${(t.title || '').toLowerCase()}`;
      if (existing.has(key)) {
        // skip duplicates already present
        continue;
      }
      const { isbn, cover_url } = await lookupOpenLibrary({ title: t.title, author: t.author_name || t.author });
      const book_uuid = genId();
      const synopsis = `${t.title} — volume ${t.series_index} of ${t.series_name}.`;
      const row = [
        book_uuid,
        csvField(t.title),
        t.year,
        csvField(cover_url || ''),
        csvField(synopsis),
        csvField(t.author_name),
        t.author_uuid,
        csvField(t.series_name),
        t.series_uuid,
        t.series_index,
        csvField(isbn || ''),
        csvField(t.language || 'en-US'),
      ];
      rows.push(row);
    } catch (e) {
      console.warn('Lookup failed for', t.title, '-', e?.message || e);
    }
  }

  if (DRY_RUN) {
    console.log('Would append rows:\n');
    for (const r of rows) {
      console.log(r.join(';'));
    }
  } else {
    appendRows(BOOKS_CSV, rows);
    console.log(`Appended ${rows.length} rows to ${BOOKS_CSV}`);
  }
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
