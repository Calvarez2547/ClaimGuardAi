/**
 * Manually assembles the Electron desktop app from already-built artifacts.
 * Run after: npm run build && npm run build:electron && mvn package -DskipTests
 */
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const electronDist = path.join(root, 'node_modules', 'electron', 'dist');
const out = path.resolve(root, '..', 'release', 'ClaimGuard AI');
const appDir = path.join(out, 'resources', 'app');

// Clean output
fs.rmSync(out, { recursive: true, force: true });
fs.mkdirSync(appDir, { recursive: true });

// Copy Electron runtime
copyDir(electronDist, out);

// Rename electron.exe
const exeSrc = path.join(out, 'electron.exe');
const exeDst = path.join(out, 'ClaimGuard AI.exe');
if (fs.existsSync(exeSrc)) fs.renameSync(exeSrc, exeDst);

// Copy compiled main process (dist-electron/ → resources/app/)
copyDir(path.join(root, 'dist-electron'), appDir);

// Copy React renderer (dist/ → resources/app/dist/)
fs.mkdirSync(path.join(appDir, 'dist'), { recursive: true });
copyDir(path.join(root, 'dist'), path.join(appDir, 'dist'));

// Write a clean package.json — no "type":"module" (main process is CommonJS)
const pkg = JSON.parse(fs.readFileSync(path.join(root, 'package.json'), 'utf8'));
const deployPkg = { name: pkg.name, version: pkg.version, main: 'main.js' };
fs.writeFileSync(path.join(appDir, 'package.json'), JSON.stringify(deployPkg, null, 2));

// Copy backend JAR
const jarSrc = path.resolve(root, '..', 'backend', 'target', 'claimguardai-backend.jar');
const jarDst = path.join(out, 'resources', 'backend.jar');
if (fs.existsSync(jarSrc)) {
  fs.copyFileSync(jarSrc, jarDst);
  console.log('✓ backend.jar copied');
} else {
  console.warn('⚠ backend.jar not found — run: cd backend && mvn package -DskipTests');
}

console.log('✓ App assembled at:', out);

function copyDir(src, dst) {
  fs.mkdirSync(dst, { recursive: true });
  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const s = path.join(src, entry.name);
    const d = path.join(dst, entry.name);
    if (entry.isDirectory()) copyDir(s, d);
    else fs.copyFileSync(s, d);
  }
}
