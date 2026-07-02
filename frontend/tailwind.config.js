/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        'app-bg': '#f6f9fc',
        'app-panel': '#ffffff',
        'app-panel-soft': '#f8fbff',
        'app-text': '#10213f',
        'app-muted': '#64748b',
        'app-border': '#dbe5ef',
        navy: {
          DEFAULT: '#06264a',
          dark: '#031b38',
          text: '#d9ebff',
          accent: '#19c3ff',
        },
        primary: {
          DEFAULT: '#0f6fec',
          soft: '#e8f2ff',
          hover: '#0a87d8',
        },
        'c-teal': {
          DEFAULT: '#12a9a6',
          soft: '#e5fbf8',
        },
        'c-green': {
          DEFAULT: '#16a56f',
          soft: '#e9f9f1',
        },
        'c-orange': {
          DEFAULT: '#e99112',
          soft: '#fff4df',
        },
        'c-red': {
          DEFAULT: '#e94343',
          soft: '#ffeded',
        },
      },
      boxShadow: {
        card: '0 16px 40px rgba(16, 33, 63, 0.08)',
      },
      fontFamily: {
        sans: [
          'Inter',
          'ui-sans-serif',
          'system-ui',
          '-apple-system',
          'BlinkMacSystemFont',
          '"Segoe UI"',
          'sans-serif',
        ],
      },
      borderRadius: {
        DEFAULT: '8px',
      },
      screens: {
        'max-lg2': { max: '1180px' },
        'max-md2': { max: '860px' },
      },
    },
  },
  plugins: [],
};
