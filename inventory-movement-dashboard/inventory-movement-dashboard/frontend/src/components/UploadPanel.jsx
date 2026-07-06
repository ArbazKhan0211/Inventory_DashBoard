import { useState } from 'react';
import { computeSha256 } from '../utils/sha256.js';
import { verifyFile } from '../api/api.js';

/**
 * Lets the user pick a JSON file, computes its SHA-256 digest client-side,
 * then sends both the file and the digest to the backend for verification.
 * The backend recomputes the hash independently and only accepts the file
 * (persisting it as the new dataset) when both hashes match.
 */
export default function UploadPanel({ onVerified }) {
  const [file, setFile] = useState(null);
  const [clientHash, setClientHash] = useState('');
  const [status, setStatus] = useState('idle'); // idle | hashing | verifying | valid | invalid | error
  const [serverInfo, setServerInfo] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');

  async function handleFileChange(e) {
    const selected = e.target.files?.[0];
    if (!selected) return;

    setFile(selected);
    setStatus('hashing');
    setServerInfo(null);
    setErrorMessage('');

    try {
      const hash = await computeSha256(selected);
      setClientHash(hash);
      setStatus('idle');
    } catch (err) {
      setStatus('error');
      setErrorMessage('Failed to compute SHA-256 in the browser: ' + err.message);
    }
  }

  async function handleVerify() {
    if (!file || !clientHash) return;
    setStatus('verifying');
    setErrorMessage('');

    try {
      const result = await verifyFile(file, clientHash);
      setServerInfo(result);

      if (result?.valid) {
        setStatus('valid');
        onVerified(result.movements);
      } else {
        setStatus('invalid');
      }
    } catch (err) {
      setStatus('error');
      setErrorMessage(err.message);
    }
  }

  return (
    <section className="panel upload-panel">
      <h2>1. Upload &amp; Verify JSON</h2>

      <div className="upload-row">
        <input type="file" accept="application/json,.json" onChange={handleFileChange} />
        <button onClick={handleVerify} disabled={!clientHash || status === 'verifying' || status === 'hashing'}>
          {status === 'verifying' ? 'Verifying…' : 'Verify & Load'}
        </button>
      </div>

      {file && (
        <div className="upload-meta">
          <div><strong>File:</strong> {file.name} ({(file.size / 1024).toFixed(1)} KB)</div>
          <div className="hash-line">
            <strong>SHA-256 (computed in browser):</strong>
            <code>{status === 'hashing' ? 'computing…' : clientHash || '—'}</code>
          </div>
        </div>
      )}

      {status === 'valid' && serverInfo && (
        <div className="status-badge status-valid">
          ✅ Verified — {serverInfo.recordCount} records loaded and persisted on the backend.
        </div>
      )}

      {status === 'invalid' && serverInfo && (
        <div className="status-badge status-invalid">
          ❌ Hash mismatch. {serverInfo.message}
          <div className="hash-line"><strong>Client hash:</strong> <code>{serverInfo.providedSha256}</code></div>
          <div className="hash-line"><strong>Server hash:</strong> <code>{serverInfo.expectedSha256}</code></div>
        </div>
      )}

      {status === 'error' && (
        <div className="status-badge status-invalid">⚠️ {errorMessage}</div>
      )}
    </section>
  );
}
