import { useMemo } from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';

/**
 * Daily-bucketed line chart: total quantity moved per day, split into
 * separate IN and OUT series. Only dates actually present in the filtered
 * data are shown on the X-axis.
 */
export default function TimeSeriesChart({ movements }) {
  const data = useMemo(() => {
    const byDate = new Map(); // 'YYYY-MM-DD' -> { date, IN, OUT }

    for (const m of movements) {
      const dateKey = new Date(m.timestamp).toISOString().slice(0, 10);
      if (!byDate.has(dateKey)) {
        byDate.set(dateKey, { date: dateKey, IN: 0, OUT: 0 });
      }
      const bucket = byDate.get(dateKey);
      if (m.movementType === 'OUT') {
        bucket.OUT += m.quantity;
      } else {
        bucket.IN += m.quantity;
      }
    }

    return Array.from(byDate.values()).sort((a, b) => a.date.localeCompare(b.date));
  }, [movements]);

  return (
    <section className="panel chart-panel">
      <h2>5. Daily Quantity Moved (IN vs OUT)</h2>
      {data.length === 0 ? (
        <p className="empty-row">No data to chart for the current filters.</p>
      ) : (
        <ResponsiveContainer width="100%" height={320}>
          <LineChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 10 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" tick={{ fontSize: 12 }} />
            <YAxis />
            <Tooltip />
            <Legend />
            <Line type="monotone" dataKey="IN" stroke="#2e7d32" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="OUT" stroke="#c62828" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </section>
  );
}
