/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // Colores de marca Nubi (mapeados a CSS custom properties)
        nubi: {
          primary: 'var(--nubi-color-primary)',
          secondary: 'var(--nubi-color-secondary)',
          accent: 'var(--nubi-color-accent)',
        },
        // Colores semánticos
        surface: {
          DEFAULT: 'var(--nubi-bg-surface)',
          secondary: 'var(--nubi-bg-surface-secondary)',
          tertiary: 'var(--nubi-bg-surface-tertiary)',
        },
        text: {
          DEFAULT: 'var(--nubi-text-primary)',
          secondary: 'var(--nubi-text-secondary)',
          tertiary: 'var(--nubi-text-tertiary)',
          inverse: 'var(--nubi-text-inverse)',
        },
        border: {
          DEFAULT: 'var(--nubi-border-default)',
          strong: 'var(--nubi-border-strong)',
        },
      },
      fontFamily: {
        sans: ['var(--nubi-font-family-base)'],
      },
      fontSize: {
        'xs': 'var(--nubi-font-size-xs)',
        'sm': 'var(--nubi-font-size-sm)',
        'base': 'var(--nubi-font-size-base)',
        'lg': 'var(--nubi-font-size-lg)',
        'xl': 'var(--nubi-font-size-xl)',
        '2xl': 'var(--nubi-font-size-2xl)',
      },
      spacing: {
        'xs': 'var(--nubi-spacing-xs)',
        'sm': 'var(--nubi-spacing-sm)',
        'md': 'var(--nubi-spacing-md)',
        'lg': 'var(--nubi-spacing-lg)',
        'xl': 'var(--nubi-spacing-xl)',
        '2xl': 'var(--nubi-spacing-2xl)',
      },
      borderRadius: {
        'sm': 'var(--nubi-radius-sm)',
        'md': 'var(--nubi-radius-md)',
        'lg': 'var(--nubi-radius-lg)',
        'xl': 'var(--nubi-radius-xl)',
      },
      boxShadow: {
        'sm': 'var(--nubi-shadow-sm)',
        'md': 'var(--nubi-shadow-md)',
        'lg': 'var(--nubi-shadow-lg)',
      },
      transitionDuration: {
        'DEFAULT': '250ms',
        'fast': '200ms',
        'slow': '300ms',
      },
      transitionTimingFunction: {
        'DEFAULT': 'cubic-bezier(0.4, 0, 0.2, 1)',
        'in': 'cubic-bezier(0.4, 0, 1, 1)',
        'out': 'cubic-bezier(0, 0, 0.2, 1)',
        'in-out': 'cubic-bezier(0.4, 0, 0.2, 1)',
      },
    },
  },
  plugins: [],
}
