const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const svgPath = path.join(__dirname, '..', 'public', 'favicon.svg');
const svgBuf = fs.readFileSync(svgPath);
const outDir = path.join(__dirname, '..', 'public');

// Write individual PNGs that Windows ICO can use, then build ICO manually
async function main() {
  const sizes = [256, 128, 64, 48, 32, 16];
  const pngs = await Promise.all(
    sizes.map(s =>
      sharp(svgBuf, { density: Math.ceil(s * 96 / 48) })
        .resize(s, s, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
        .png()
        .toBuffer()
    )
  );

  // Build ICO manually: header + directory + image data
  const n = sizes.length;
  const headerSize = 6;
  const dirEntrySize = 16;
  const dirSize = n * dirEntrySize;

  let offset = headerSize + dirSize;
  const offsets = [];
  for (const png of pngs) {
    offsets.push(offset);
    offset += png.length;
  }

  const totalSize = offset;
  const buf = Buffer.alloc(totalSize);

  // ICONDIR header
  buf.writeUInt16LE(0, 0);       // reserved
  buf.writeUInt16LE(1, 2);       // type = ICO
  buf.writeUInt16LE(n, 4);       // count

  // Directory entries
  for (let i = 0; i < n; i++) {
    const base = headerSize + i * dirEntrySize;
    const sz = sizes[i] >= 256 ? 0 : sizes[i]; // 256 stored as 0
    buf.writeUInt8(sz, base + 0);   // width
    buf.writeUInt8(sz, base + 1);   // height
    buf.writeUInt8(0, base + 2);    // color count
    buf.writeUInt8(0, base + 3);    // reserved
    buf.writeUInt16LE(1, base + 4); // planes
    buf.writeUInt16LE(32, base + 6);// bit count
    buf.writeUInt32LE(pngs[i].length, base + 8);
    buf.writeUInt32LE(offsets[i], base + 12);
  }

  // Image data
  for (let i = 0; i < n; i++) {
    pngs[i].copy(buf, offsets[i]);
  }

  const icoPath = path.join(outDir, 'icon.ico');
  fs.writeFileSync(icoPath, buf);
  console.log('icon.ico written:', icoPath, `(${(buf.length / 1024).toFixed(1)} KB)`);
}

main().catch(e => { console.error(e); process.exit(1); });
