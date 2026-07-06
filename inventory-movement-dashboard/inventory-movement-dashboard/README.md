# Inventory Movement Dashboard

A full-stack app to upload a stock-movements JSON file, verify its integrity with a
client-computed SHA-256 digest, and explore the data through a filterable, paginated
table plus a pie chart and a time-series chart.

Backend: Java 17, Spring Boot 3.2 (Maven). Frontend: React 18 + Vite, charts via Recharts.

The project has two folders: backend (Spring Boot REST API) and frontend (React single-page app).

## Running the backend

Go into the backend folder and run:

mvn spring-boot:run

The API starts on http://localhost:8080.

On first run it seeds itself from the bundled sample dataset at
backend/src/main/resources/data/movements.json (10,000 sample records). Once you
successfully upload and verify a file through the UI, the backend writes it to
backend/data/movements-runtime.json and uses that file for every subsequent
GET /api/movements call, including after a restart.

To build a runnable jar instead, run mvn clean package from the backend folder, then
run java -jar target/inventory-movement-dashboard-1.0.0.jar.

### Backend endpoints

GET /api/movements takes query params from and to (both required, format YYYY-MM-DD,
inclusive), an optional type param (IN, OUT, or omit/ALL for both), and an optional
warehouse param (e.g. WH-NORTH, omit/ALL for all warehouses, this is the bonus filter).
It returns the filtered list of movement records as JSON, read from whatever dataset
is currently stored on the backend.

POST /api/verify-file is a multipart/form-data request with a file field (the JSON
file) and a sha256 field (the hex SHA-256 digest computed by the frontend over the
same file bytes). The backend recomputes SHA-256 over the raw uploaded bytes and
compares it against the sha256 field. Only on a match does it parse the JSON and
persist it as the new backend dataset. The response always reports both hashes
(expectedSha256 is the server-computed one, providedSha256 is the client-supplied one)
so a mismatch is easy to debug, along with valid, message, recordCount, and, when
valid, the parsed movements array.

### Run backend tests

From the backend folder, run mvn test. This covers SHA-256 hashing
(FileVerificationServiceTest) and the date/type/warehouse filtering logic
(MovementQueryServiceTest).

Note: this sandbox environment could not reach Maven Central to actually execute
mvn compile or mvn test while building this repo, since network egress is restricted
to a small allowlist that doesn't include repo.maven.apache.org. So the backend code
has been written carefully and reviewed by hand but not compiler-verified here. It
follows standard, current Spring Boot 3.2 idioms throughout. Please run mvn test
locally as the first step, see the trade-offs section below for more on this.

## Running the frontend

Go into the frontend folder and run npm install, then npm run dev.

This opens on http://localhost:5173. The Vite dev server proxies /api/* requests to
http://localhost:8080 (see vite.config.js), so make sure the backend is running first.

This was built and verified in this environment with npm install and npm run build,
it compiles cleanly with Vite.

To build a static production bundle, run npm run build from the frontend folder, then
npm run preview to serve the built dist folder locally as a sanity check.

## Using the app

First, upload and verify a file: choose a movements JSON file. The browser computes
its SHA-256 digest (via crypto.subtle, no extra library needed) and shows it. Click
Verify & Load to send the file and digest to the backend. If the hashes match, the
backend parses and persists the file, and the UI immediately shows the parsed record
count. If they don't match, the UI shows both hashes and does not touch the existing
backend dataset.

Next, use the filters: pick a date range (required) and optionally a movement type
and/or warehouse. Every change re-queries GET /api/movements.

The table is paginated, 10 rows per page, reflecting the active filters.

The pie chart shows total quantity IN vs OUT for the currently filtered result set.

The time-series chart is a daily-bucketed line chart, one line for IN and one for
OUT, showing only the dates actually present in the filtered data.

The table, pie chart, and time-series chart all read from the same filtered dataset
returned by GET /api/movements, so they're always in sync.

## Design notes and trade-offs

The backend keeps data as a JSON file, not a database, per the spec's requirement to
read from a stored JSON file. This is intentionally simple; a real system would use
a database and a proper migration/versioning story instead of overwriting a single
file.

Filtering happens server-side, not client-side, per the spec: the frontend always
calls GET /api/movements with the current filters rather than filtering an in-memory
blob itself. This does mean a network round-trip per filter change, which is fine at
this dataset size (10k records, sub-second responses).

Date range semantics: from and to are inclusive whole days interpreted in UTC. If
your movements dataset uses non-UTC-normalized timestamps, adjust
MovementQueryService accordingly.

Concurrency: MovementDataStore uses a read-write lock around the in-memory list and
does a simple whole-file overwrite on upload. This is adequate for a single-instance
demo app; it is not built for multiple concurrent backend instances sharing one file.

Validation: the backend independently recomputes the SHA-256 hash rather than
trusting the client's digest as-is, the client-provided hash is only used for the
comparison, never taken as ground truth on its own.

The warehouse filter is implemented as the optional bonus field from the sample
dataset, both as a query param on the backend and a dropdown on the frontend
(populated dynamically from whatever warehouses appear in the currently loaded data).

Not implemented or could be extended: authentication, multi-file history,
websocket/live updates on upload, server-side pagination (current pagination is
client-side over the already-filtered result set, which is fine at 10k rows but
wouldn't scale indefinitely), and a proper CI pipeline.

Tests: backend unit tests cover the two riskiest pieces of logic, hashing and
filtering. Frontend tests were not added since no test runner was wired up; given
more time, Vitest and React Testing Library would be a natural next step for
UploadPanel, FiltersPanel, and the chart data-shaping logic in PieChartView and
TimeSeriesChart.

## Sample data

backend/src/main/resources/data/movements.json contains 10,000 generated sample
records matching the shape from the spec, for example a record with id mv1001,
timestamp 2026-03-10T17:46:00Z, sku SKU003, movementType IN, quantity 48, and
warehouse WH-NORTH.
