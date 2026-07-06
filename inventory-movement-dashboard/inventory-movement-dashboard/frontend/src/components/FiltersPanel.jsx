/**
 * Date range (required), movement type, and an optional warehouse dropdown
 * (bonus). Values are lifted up to App so they can drive the table + both
 * charts consistently.
 */
export default function FiltersPanel({ filters, onChange, warehouses }) {
  function update(partial) {
    onChange({ ...filters, ...partial });
  }

  return (
    <section className="panel filters-panel">
      <h2>2. Filters</h2>
      <div className="filters-row">
        <label>
          From
          <input
            type="date"
            value={filters.from}
            max={filters.to || undefined}
            onChange={(e) => update({ from: e.target.value })}
          />
        </label>

        <label>
          To
          <input
            type="date"
            value={filters.to}
            min={filters.from || undefined}
            onChange={(e) => update({ to: e.target.value })}
          />
        </label>

        <label>
          Movement Type
          <select value={filters.type} onChange={(e) => update({ type: e.target.value })}>
            <option value="ALL">All</option>
            <option value="IN">IN</option>
            <option value="OUT">OUT</option>
          </select>
        </label>

        {warehouses && warehouses.length > 0 && (
          <label>
            Warehouse
            <select value={filters.warehouse} onChange={(e) => update({ warehouse: e.target.value })}>
              <option value="ALL">All</option>
              {warehouses.map((w) => (
                <option key={w} value={w}>{w}</option>
              ))}
            </select>
          </label>
        )}
      </div>
    </section>
  );
}
