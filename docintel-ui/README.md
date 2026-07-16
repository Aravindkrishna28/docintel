# DocIntel — UI

Responsive React UI for the DocIntel document-intelligence prototype: Upload → Review → Correct.

## Run it locally

```bash
npm install
npm run dev
```

Then open the URL Vite prints (defaults to `http://localhost:5173`).

## Build for production

```bash
npm run build
npm run preview
```

## Project structure

```
docintel-ui/
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
└── src/
    ├── main.jsx      # React entry point
    ├── App.jsx        # All 3 screens (Upload, Dashboard, Correction)
    └── index.css      # Tailwind directives
```

## Wiring to a real backend

Everything currently runs on local state and mock data. Two spots to connect to your Spring Boot API:

- **`handleFileChosen`** in `App.jsx` — replace the `setTimeout` simulation with your real
  `POST /api/documents/upload` followed by `POST /api/documents/{id}/process` calls.
- **`handleCorrectionsSubmit`** in `App.jsx` — replace the local state update with a
  `PUT /api/documents/{id}/corrections` call.

You'll also want to fetch real `fields` and the summary from `GET /api/documents/{id}` once
processing finishes, instead of using `INITIAL_FIELDS` / `SUMMARY_TEXT`.
