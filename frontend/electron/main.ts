import { app, BrowserWindow, shell, dialog, Menu } from 'electron';
import { spawn, ChildProcess } from 'child_process';
import path from 'path';
import http from 'http';
import fs from 'fs';

const isDev = process.env.ELECTRON_ENV === 'development';
const BACKEND_PORT = 8080;
const BACKEND_URL = `http://localhost:${BACKEND_PORT}`;
const HEALTH_URL = `${BACKEND_URL}/api/health`;
const ICON_PATH = path.join(__dirname, 'icon.ico');

let mainWindow: BrowserWindow | null = null;
let splashWindow: BrowserWindow | null = null;
let backendProcess: ChildProcess | null = null;

// ── Backend lifecycle ─────────────────────────────────────────────────────────

function findJar(): string | null {
  const candidates = [
    path.join(process.resourcesPath, 'backend.jar'),
    path.join(__dirname, '..', '..', 'backend', 'target', 'claimguardai-backend.jar'),
    path.join(__dirname, '..', '..', 'backend', 'target', 'claimguardai-backend-1.2.0.jar'),
  ];
  return candidates.find(fs.existsSync) ?? null;
}

function startBackend(): Promise<void> {
  if (isDev) return Promise.resolve();

  const jarPath = findJar();
  if (!jarPath) {
    return Promise.reject(new Error('backend.jar not found in resources'));
  }

  const userDataPath = app.getPath('userData');
  const dbPath = path.join(userDataPath, 'claimguardai-data').replace(/\\/g, '/');
  const dbUrl = `jdbc:h2:file:${dbPath};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`;

  backendProcess = spawn('java', [
    `-Dserver.port=${BACKEND_PORT}`,
    '-jar',
    jarPath,
    '--spring.profiles.active=desktop',
  ], {
    env: {
      ...process.env,
      CLAIMGUARDAI_DESKTOP_DB_URL: dbUrl,
      SERVER_PORT: String(BACKEND_PORT),
    },
    windowsHide: true,
  });

  backendProcess.on('error', (err) => {
    if (err.message.includes('ENOENT')) {
      dialog.showErrorBox(
        'Java Not Found',
        'ClaimGuard AI requires Java 21 or later.\n\nPlease install Java and make sure it is on your PATH, then restart the app.',
      );
      app.quit();
    }
  });

  return waitForBackend();
}

function waitForBackend(maxAttempts = 90, intervalMs = 1000): Promise<void> {
  return new Promise((resolve, reject) => {
    let attempts = 0;

    function attempt() {
      http.get(HEALTH_URL, (res) => {
        if (res.statusCode === 200) {
          resolve();
        } else {
          retry();
        }
        res.resume();
      }).on('error', retry);
    }

    function retry() {
      attempts++;
      if (attempts >= maxAttempts) {
        reject(new Error(`Backend did not start after ${maxAttempts} seconds`));
      } else {
        setTimeout(attempt, intervalMs);
      }
    }

    attempt();
  });
}

// ── Windows ───────────────────────────────────────────────────────────────────

function createSplash() {
  splashWindow = new BrowserWindow({
    width: 420,
    height: 260,
    resizable: false,
    frame: false,
    transparent: false,
    alwaysOnTop: true,
    icon: ICON_PATH,
    webPreferences: { nodeIntegration: false, contextIsolation: true },
    backgroundColor: '#06264a',
  });

  splashWindow.loadFile(path.join(__dirname, 'splash.html'));
}

function createMainWindow() {
  mainWindow = new BrowserWindow({
    width: 1320,
    height: 860,
    minWidth: 960,
    minHeight: 640,
    show: false,
    icon: ICON_PATH,
    backgroundColor: '#f6f9fc',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
    title: 'ClaimGuard AI',
  });

  if (isDev) {
    mainWindow.loadURL('http://localhost:5173');
  } else {
    mainWindow.loadFile(path.join(__dirname, 'dist', 'index.html'));
  }

  // Right-click context menu with standard OS clipboard actions
  mainWindow.webContents.on('context-menu', (_e, params) => {
    const menu = Menu.buildFromTemplate([
      { role: 'cut', enabled: params.editFlags.canCut },
      { role: 'copy', enabled: params.editFlags.canCopy },
      { role: 'paste', enabled: params.editFlags.canPaste },
      { type: 'separator' },
      { role: 'selectAll', enabled: params.editFlags.canSelectAll },
    ]);
    menu.popup({ window: mainWindow! });
  });

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });

  mainWindow.once('ready-to-show', () => {
    splashWindow?.close();
    splashWindow = null;
    mainWindow?.show();
  });

  mainWindow.on('closed', () => { mainWindow = null; });
}

// ── Application menu ─────────────────────────────────────────────────────────

function buildMenu() {
  const template: Electron.MenuItemConstructorOptions[] = [
    {
      label: 'Edit',
      submenu: [
        { role: 'undo' },
        { role: 'redo' },
        { type: 'separator' },
        { role: 'cut' },
        { role: 'copy' },
        { role: 'paste' },
        { role: 'selectAll' },
      ],
    },
    {
      label: 'View',
      submenu: [
        { role: 'reload' },
        { role: 'toggleDevTools' },
        { type: 'separator' },
        { role: 'resetZoom' },
        { role: 'zoomIn' },
        { role: 'zoomOut' },
        { type: 'separator' },
        { role: 'togglefullscreen' },
      ],
    },
    {
      label: 'Window',
      submenu: [{ role: 'minimize' }, { role: 'close' }],
    },
  ];
  Menu.setApplicationMenu(Menu.buildFromTemplate(template));
}

// ── App lifecycle ─────────────────────────────────────────────────────────────

app.whenReady().then(async () => {
  buildMenu();
  if (!isDev) createSplash();

  try {
    await startBackend();
  } catch (err) {
    dialog.showErrorBox('Startup Error', String(err));
    app.quit();
    return;
  }

  createMainWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createMainWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

app.on('before-quit', () => {
  if (backendProcess) {
    backendProcess.kill('SIGTERM');
    backendProcess = null;
  }
});
