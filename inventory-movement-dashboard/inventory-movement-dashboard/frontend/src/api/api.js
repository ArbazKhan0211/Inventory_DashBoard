/**
 * Thin wrapper around the backend REST API.
 * In dev, Vite proxies /api/* to http://localhost:8080 (see vite.config.js),
 * so relative paths work both in dev and when the built app is served by
 * something in front of the same backend.
 */

export async function fetchMovements({ from, to, type, warehouse }) {
  const params = new URLSearchParams();
  params.set('from', from);
  params.set('to', to);
  if (type && type !== 'ALL') params.set('type', type);
  if (warehouse && warehouse !== 'ALL') params.set('warehouse', warehouse);

  const res = await fetch(`/api/movements?${params.toString()}`);
  if (!res.ok) {
    const body = await safeJson(res);
    throw new Error(body?.message || `Failed to fetch movements (HTTP ${res.status})`);
  }
  return res.json();
}

export async function verifyFile(file, sha256Hex) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('sha256', sha256Hex);

  const res = await fetch('/api/verify-file', {
    method: 'POST',
    body: formData,
  });

  const body = await safeJson(res);
  if (!res.ok && !body) {
    throw new Error(`Verification request failed (HTTP ${res.status})`);
  }
  return body;
}

async function safeJson(res) {
  try {
    return await res.json();
  } catch {
    return null;
  }
}
