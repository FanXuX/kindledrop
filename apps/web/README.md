# KindleDrop Web UI

A modern React + Vite + Tailwind CSS web interface for KindleDrop, allowing users to easily send GitHub files to their Kindle devices.

## Features

- Clean, responsive UI built with React and Tailwind CSS
- Form to submit GitHub file URLs and Kindle email
- Real-time submission history with success/error status
- Proxied API calls to the Spring Boot engine
- Built with Vite for fast development and optimized production builds

## Development

### Prerequisites

- Node.js 18+ (managed by the root workspace)
- The Spring Boot engine running on `http://localhost:8080`

### Getting Started

```bash
# Install dependencies from the root workspace
cd ../..
npm install

# Start the development server
cd apps/web
npm run dev
```

The app will be available at `http://localhost:5173`.

### API Proxy

The Vite dev server is configured to proxy `/api` requests to `http://localhost:8080`. Ensure the Spring Boot engine is running before testing API calls.

## Build for Production

```bash
npm run build
```

Output will be in the `dist/` directory, ready for deployment.

## Project Structure

```
src/
├── main.tsx          # React entry point
├── index.css         # Tailwind CSS + global styles
├── App.tsx           # Main app component
├── components/
│   ├── SendForm.tsx    # Form to submit files
│   └── SendHistory.tsx # Display submission history
└── lib/
    └── apiClient.ts    # API communication
```

## Styling

Tailwind CSS is configured with PostCSS. All utility classes are available in components.

## Notes

- The app expects the `/api/send` endpoint to return JSON in the format:
  ```json
  {
    "ok": boolean,
    "resolvedUrl": string,
    "fileName": string,
    "bytes": number,
    "message": string (optional)
  }
  ```
