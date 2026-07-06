import { useMemo } from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const COLORS = { IN: '#2e7d32', OUT: '#c62828' };

/**
 * Shows the proportion of total quantity moved IN vs OUT within the
 * currently filtered dataset.
 */
export default function PieChartView({ movements }) {
  const data = useMemo(() => {
    const totals = movements.reduce(
      (acc, m) => {
        const key = m.movementType === 'OUT' ? 'OUT' : 'IN';
        acc[key] += m.quantity;
        return acc;
      },
      { IN: 0, OUT: 0 }
    );
    return [
      { name: 'IN', value: totals.IN },
      { name: 'OUT', value: totals.OUT },
    ];
  }, [movements]);

  const total = data[0].value + data[1].value;

  return (
    <section className="panel chart-panel">
      <h2>4. IN vs OUT Quantity</h2>
      {total === 0 ? (
        <p className="empty-row">No quantity data to chart for the current filters.</p>
      ) : (
        <ResponsiveContainer width="100%" height={280}>
          <PieChart>
            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              outerRadius={100}
              label={({ name, value }) => `${name}: ${value} (${((value / total) * 100).toFixed(1)}%)`}
            >
              {data.map((entry) => (
                <Cell key={entry.name} fill={COLORS[entry.name]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      )}
    </section>
  );
}
