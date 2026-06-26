/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/**/*.{html,ts}'
  ],
  theme: {
    extend: {
      colors: {
        'app-bg': '#0f1117',
        'app-card': '#1a1d27',
        'app-border': '#2a2d3a',
        'app-text': '#e4e4e7',
        'app-muted': '#9ca3af',
        'app-accent': '#6366f1',
        'app-accent-hover': '#4f46e5'
      }
    }
  },
  plugins: []
};
