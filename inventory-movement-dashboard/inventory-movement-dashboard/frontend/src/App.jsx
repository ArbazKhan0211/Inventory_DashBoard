import { useEffect, useMemo, useState, useCallback } from 'react';
import UploadPanel from './components/UploadPanel.jsx';
import FiltersPanel from './components/FiltersPanel.jsx';
import MovementsTable from './components/MovementsTable.jsx';
import PieChartView from './components/PieChartView.jsx';
import TimeSeriesChart from './components/TimeSeriesChart.jsx';
import { fetchMovements } from './api/api.js';

function todayMinusDays(days) {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return d.toISOString().slice(0, 10);
}
function todayPlusDays(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
}

export default function App() {
  // Default to a wide window so the bundled sample dataset shows up immediately.
  const [filters, setFilters] = useState({
    from: todayMinusDays(400),
    to: todayPlusDays(400),
    type: 'ALL',
    warehouse: 'ALL',
  });

  const [movements, setMovements] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [hasUploaded, setHasUploaded] = useState(false);

  const loadMovements = useCallback(async (activeFilters) => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchMovements(activeFilters);
      setMovements(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  // Initial load from GET /api/movements (backend's currently stored dataset),
  // and reload whenever filters change.
  useEffect(() => {
    if (!filters.from || !filters.to) return;
    loadMovements(filters);
  }, [filters, loadMovements]);

  // After a successful SHA-256 verification + upload, the backend has already
  // persisted the new dataset. Re-fetch through GET /api/movements so the
  // table/charts reflect the current filters against the new data.
  function handleVerified() {
    setHasUploaded(true);
    loadMovements(filters);
  }

  const warehouses = useMemo(() => {
    const set = new Set(movements.map((m) => m.warehouse).filter(Boolean));
    return Array.from(set).sort();
  }, [movements]);

  return (
    <div className="app">
      <header className="app-header">
        <h1>📦 Inventory Movement Dashboard</h1>
        <p className="subtitle">
          Upload &amp; SHA-256 verify a movements JSON file, then explore it with filters,
          a paginated table, and charts.
        </p>
      </header>

      <UploadPanel onVerified={handleVerified} />

      {hasUploaded && (
        <div className="status-badge status-valid" style={{ marginBottom: '1rem' }}>
          Showing data from your uploaded file.
        </div>
      )}

      <FiltersPanel filters={filters} onChange={setFilters} warehouses={warehouses} />

      {loading && <p className="loading-text">Loading movements…</p>}
      {error && <div className="status-badge status-invalid">⚠️ {error}</div>}

      {!loading && !error && (
        <>
          <MovementsTable movements={movements} />
          <div className="charts-grid">
            <PieChartView movements={movements} />
            <TimeSeriesChart movements={movements} />
          </div>
        </>
      )}

      <footer className="app-footer">
        Inventory Movement Dashboard &middot; React + Spring Boot
      </footer>
    </div>
  );
}
