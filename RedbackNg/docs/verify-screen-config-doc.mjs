#!/usr/bin/env node
/**
 * Verifies that docs/screen-configuration.md covers:
 *   1. every `type` key declared in componentRegistry (src/app/loader.ts)
 *      (each must appear as a row in the appendix table: | `type` | ...)
 *   2. every @Input alias reachable from each registered component class,
 *      walking the `extends` chain through this repo's base classes.
 *
 * Usage: node docs/verify-screen-config-doc.mjs
 * Exit code 0 = all covered, 1 = gaps found.
 */
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, '..');
const APP = join(root, 'src', 'app');
const DOC = join(__dirname, 'screen-configuration.md');

// ---------------------------------------------------------------- file walk
function listTs(dir) {
  const out = [];
  for (const entry of readdirSync(dir)) {
    const p = join(dir, entry);
    const st = statSync(p);
    if (st.isDirectory()) out.push(...listTs(p));
    else if (p.endsWith('.ts') && !p.endsWith('.spec.ts')) out.push(p);
  }
  return out;
}

// ---------------------------------------------------------------- class parser
// Returns [{ name, header, body }] for every top-level class/declaration
// found in the source. Body is extracted by brace matching with a small
// string/comment state machine so braces inside literals don't confuse us.
function parseClasses(src) {
  const classes = [];
  const re = /export\s+(?:abstract\s+)?class\s+([A-Za-z0-9_]+)/g;
  let m;
  while ((m = re.exec(src))) {
    const name = m[1];
    const braceStart = src.indexOf('{', m.index);
    // header = text between the class name and the opening brace
    const header = src.slice(m.index, braceStart);
    let depth = 0, i = braceStart, inStr = null, inLineComment = false, inBlockComment = false;
    for (; i < src.length; i++) {
      const ch = src[i], next = src[i + 1];
      if (inLineComment) { if (ch === '\n') inLineComment = false; continue; }
      if (inBlockComment) { if (ch === '*' && next === '/') { inBlockComment = false; i++; } continue; }
      if (inStr) {
        if (ch === '\\') { i++; continue; }
        if (ch === inStr) inStr = null;
        continue;
      }
      if (ch === '/' && next === '/') { inLineComment = true; i++; continue; }
      if (ch === '/' && next === '*') { inBlockComment = true; i++; continue; }
      if (ch === "'" || ch === '"' || ch === '`') { inStr = ch; continue; }
      if (ch === '{') depth++;
      else if (ch === '}') { depth--; if (depth === 0) { i++; break; } }
    }
    const body = src.slice(braceStart + 1, i);
    classes.push({ name, header, body, full: src.slice(m.index, i) });
  }
  return classes;
}

const fileCache = new Map(); // file -> { source, classes }
function loadFile(file) {
  if (fileCache.has(file)) return fileCache.get(file);
  const source = readFileSync(file, 'utf8');
  const classes = parseClasses(source);
  const value = { source, classes };
  fileCache.set(file, value);
  return value;
}

// ---------------------------------------------------------------- import map
function importMap(file) {
  const { source } = loadFile(file);
  const map = new Map();
  const re = /import\s*\{([^}]+)\}\s*from\s*['"](app\/[^'"]+)['"]/g;
  let m;
  while ((m = re.exec(source))) {
    for (const raw of m[1].split(',')) {
      const parts = raw.trim().split(/\s+as\s+/).map((s) => s.trim());
      if (parts[0]) map.set(parts[0], m[2].replace(/^app\//, ''));
    }
  }
  return map;
}

// ---------------------------------------------------------------- @Input extraction
function inputAliases(classInfo) {
  const aliases = new Map();
  let m;
  const aliased = /@Input\(\s*['"]([A-Za-z0-9_]+)['"]\s*\)\s*([A-Za-z0-9_]+)/g;
  while ((m = aliased.exec(classInfo.body))) aliases.set(m[1], m[2]);
  const plain = /@Input\(\s*\)\s*([A-Za-z0-9_]+)/g;
  while ((m = plain.exec(classInfo.body))) aliases.set(m[1], m[1]);
  return aliases;
}

// resolve `extends P` from classInfo to a classInfo; null when not found
function resolveParent(classInfo) {
  const m = /extends\s+([A-Za-z0-9_]+)/.exec(classInfo.header);
  if (!m) return null;
  const parentName = m[1];
  const sameFile = currentFileClasses.find((c) => c.name === parentName);
  if (sameFile) return sameFile;
  const imports = importMap(currentFile);
  if (imports.has(parentName)) {
    const targetFile = join(APP, imports.get(parentName) + '.ts');
    const target = loadFile(targetFile);
    return target.classes.find((c) => c.name === parentName) ?? null;
  }
  // last resort: index the whole app
  for (const [file, entry] of fileCache) {
    const c = entry.classes.find((cc) => cc.name === parentName);
    if (c) return c;
  }
  return null;
}

// ---------------------------------------------------------------- registry
const loaderSrc = readFileSync(join(APP, 'loader.ts'), 'utf8');
const registry = new Map();
const regBlock = loaderSrc.slice(loaderSrc.indexOf('componentRegistry'));
const regRe = /^\s*['"]([A-Za-z0-9-]+)['"]\s*:\s*([A-Za-z0-9_]+),?\s*$/gm;
let m;
while ((m = regRe.exec(regBlock))) registry.set(m[1], m[2]);

// ---------------------------------------------------------------- coverage check
const loaderClasses = loadFile(join(APP, 'loader.ts')).classes;
void loaderClasses;

const inputCacheMap = new Map(); // keyed by "file::class"

function allInputsForClass(className, file, seen = new Set()) {
  const key = file + '::' + className;
  if (seen.has(className)) return new Map();
  seen.add(className);
  if (inputCacheMap.has(key)) return inputCacheMap.get(key);

  const target = loadFile(file).classes.find((c) => c.name === className);
  if (!target) return new Map();

  global.currentFile = file; // used by resolveParent
  global.currentFileClasses = loadFile(file).classes;

  const own = inputAliases(target);
  const parent = resolveParent(target);
  const merged = new Map(own);
  if (parent) {
    const parentFile = fileForClass(parent.name);
    for (const [alias] of allInputsForClass(parent.name, parentFile, seen)) merged.set(alias);
  }
  inputCacheMap.set(key, merged);
  return merged;
}

// find which file a class lives in (cache first, then linear scan)
function fileForClass(className) {
  for (const [file, entry] of fileCache) {
    if (entry.classes.some((c) => c.name === className)) return file;
  }
  for (const file of listTs(APP)) {
    if (loadFile(file).classes.some((c) => c.name === className)) return file;
  }
  throw new Error('Class not found: ' + className);
}

// ---- run ----
const doc = readFileSync(DOC, 'utf8');
const missingTypes = [];
const missingAliases = [];
const aliasSets = new Map();

for (const [typeKey, className] of registry) {
  if (!new RegExp('\\|\\s*`' + typeKey + '`\\s*\\|').test(doc)) {
    missingTypes.push(typeKey + ' → ' + className);
  }
  fileForClass(className); // prime cache
  const inputs = allInputsForClass(className, fileForClass(className));
  aliasSets.set(typeKey, inputs);
  for (const alias of inputs.keys()) {
    const re = new RegExp('`' + alias.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&') + '`');
    if (!re.test(doc)) missingAliases.push(typeKey + ' → @Input(\'' + alias + '\')  [' + className + ']');
  }
}

// ---- report ----
const distinctAliases = new Set();
for (const s of aliasSets.values()) for (const a of s.keys()) distinctAliases.add(a);

// cross-check the appendix table row count against the registry size
const appendixRows = (doc.match(/^\|\s*`[A-Za-z0-9-]+`\s*\|\s*Rb[A-Za-z0-9_]+\s*\|/gm) || []).length;

console.log('Registry types:  ' + registry.size);
console.log('Appendix rows:   ' + appendixRows);
console.log('Distinct input aliases reachable from registry classes: ' + distinctAliases.size);

let fail = false;
if (appendixRows !== registry.size) {
  fail = true;
  console.error('\nAppendix table row count (' + appendixRows + ') != registry size (' + registry.size + ')');
}
if (missingTypes.length) {
  fail = true;
  console.error('\nMISSING from appendix table (' + missingTypes.length + '):');
  for (const t of missingTypes) console.error('  ' + t);
}
if (missingAliases.length) {
  fail = true;
  console.error('\nMISSING input aliases in doc (' + missingAliases.length + '):');
  for (const a of missingAliases) console.error('  ' + a);
}
if (!fail) console.log('\nOK: all registry types present in the appendix table; all input aliases documented.');
process.exit(fail ? 1 : 0);
