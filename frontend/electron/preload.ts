import { contextBridge } from 'electron';

contextBridge.exposeInMainWorld('claimguardaiDesktop', {
  platform: process.platform,
  apiBaseUrl: 'http://localhost:9847',
});
