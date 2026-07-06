/**
 * Computes the SHA-256 digest of a File's raw bytes using the browser's
 * built-in Web Crypto API (no external crypto library needed).
 *
 * @param {File} file
 * @returns {Promise<string>} lowercase hex digest, e.g. "3a7bd3e2..."
 */
export async function computeSha256(file) {
  const buffer = await file.arrayBuffer();
  const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
  return bufferToHex(hashBuffer);
}

function bufferToHex(buffer) {
  const bytes = new Uint8Array(buffer);
  let hex = '';
  for (let i = 0; i < bytes.length; i++) {
    hex += bytes[i].toString(16).padStart(2, '0');
  }
  return hex;
}
