import { useMemo, useState, useEffect } from 'react';

const PAGE_SIZE = 10;

/**
 * Displays stock movements in a 10-rows-per-page table.
 * Resets to page 1 whenever the underlying (already-filtered) dataset changes.
 */
export default function MovementsTable({ movements }) {
  const [page, setPage] = useState(1);

  useEffect(() => {
    setPage(1);
  }, [movements]);

  const totalPages = Math.max(1, Math.ceil(movements.length / PAGE_SIZE));

  const pageRows = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return movements.slice(start, start + PAGE_SIZE);
  }, [movements, page]);

  return (
    <section className="panel table-panel">
      <h2>3. Stock Movements ({movements.length} records)</h2>

      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Date / Time</th>
              <th>SKU</th>
              <th>Movement Type</th>
              <th>Quantity</th>
            </tr>
          </thead>
          <tbody>
            {pageRows.length === 0 && (
              <tr>
                <td colSpan={4} className="empty-row">No movements match the current filters.</td>
              </tr>
            )}
            {pageRows.map((m) => (
              <tr key={m.id}>
                <td>{new Date(m.timestamp).toLocaleString()}</td>
                <td>{m.sku}</td>
                <td>
                  <span className={`badge badge-${m.movementType?.toLowerCase()}`}>{m.movementType}</span>
                </td>
                <td>{m.quantity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="pagination">
        <button onClick={() => setPage((p) => Math.max(1, p - 1))} disabled={page <= 1}>
          ‹ Prev
        </button>
        <span>Page {page} of {totalPages}</span>
        <button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={page >= totalPages}>
          Next ›
        </button>
      </div>
    </section>
  );
}
