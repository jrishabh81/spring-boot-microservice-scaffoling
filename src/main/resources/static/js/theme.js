// theme.js - Handles light/dark theme switching
//
// Responsibilities:
// - Load saved theme preference from localStorage
// - Provide toggle functionality to switch between light and dark modes
// - Apply theme by setting data-theme attribute on document root
// - Persist user preference in localStorage
//
(function(){
  'use strict';

  /**
   * THEME_KEY
   * localStorage key for persisting theme preference
   * @type {string}
   */
  const THEME_KEY = 'app-theme';

  /**
   * THEMES
   * Available theme options
   * @type {Object}
   */
  const THEMES = {
    LIGHT: 'light',
    DARK: 'dark'
  };

  /**
   * getSystemTheme
   * Detects the user's system theme preference using prefers-color-scheme media query.
   * Falls back to 'light' if not supported.
   *
   * @returns {string} 'light' or 'dark'
   */
  function getSystemTheme(){
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return THEMES.DARK;
    }
    return THEMES.LIGHT;
  }

  /**
   * getSavedTheme
   * Retrieves the user's saved theme preference from localStorage.
   * Returns null if not set.
   *
   * @returns {string|null} Saved theme or null
   */
  function getSavedTheme(){
    try {
      return localStorage.getItem(THEME_KEY);
    } catch (e) {
      return null;
    }
  }

  /**
   * saveTheme
   * Persists the theme preference to localStorage.
   *
   * @param {string} theme - Theme to save ('light' or 'dark')
   */
  function saveTheme(theme){
    try {
      localStorage.setItem(THEME_KEY, theme);
    } catch (e) {
      console.warn('Unable to save theme preference', e);
    }
  }

  /**
   * applyTheme
   * Applies the theme by setting the data-theme attribute on the document element.
   * This triggers CSS rules via [data-theme="dark"] selectors.
   *
   * @param {string} theme - Theme to apply ('light' or 'dark')
   */
  function applyTheme(theme){
    document.documentElement.setAttribute('data-theme', theme);
    // Update toggle button state if it exists
    const toggleBtn = document.getElementById('themeToggleBtn');
    if (toggleBtn) {
      toggleBtn.setAttribute('aria-pressed', theme === THEMES.DARK ? 'true' : 'false');
      const icon = toggleBtn.querySelector('.theme-icon');
      if (icon) {
        icon.textContent = theme === THEMES.DARK ? '☀️' : '🌙';
      }
    }
  }

  /**
   * toggleTheme
   * Switches between light and dark themes, saves preference, and applies the new theme.
   */
  function toggleTheme(){
    const current = document.documentElement.getAttribute('data-theme') || THEMES.LIGHT;
    const next = current === THEMES.LIGHT ? THEMES.DARK : THEMES.LIGHT;
    saveTheme(next);
    applyTheme(next);
  }

  /**
   * initializeTheme
   * Initializes the theme on page load by checking:
   * 1. User's saved preference in localStorage
   * 2. System preference (prefers-color-scheme)
   * 3. Defaults to light mode
   */
  function initializeTheme(){
    const saved = getSavedTheme();
    const theme = saved || getSystemTheme();
    applyTheme(theme);
  }

  /**
   * setupThemeToggle
   * Attaches click/keyboard event listeners to the theme toggle button.
   * The button is expected to have id="themeToggleBtn" in the DOM.
   */
  function setupThemeToggle(){
    const toggleBtn = document.getElementById('themeToggleBtn');
    if (toggleBtn) {
      toggleBtn.addEventListener('click', toggleTheme);
      // Allow keyboard activation
      toggleBtn.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          toggleTheme();
        }
      });
    }
  }

  // Initialize theme when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      initializeTheme();
      setupThemeToggle();
    });
  } else {
    initializeTheme();
    setupThemeToggle();
  }

  // Export functions for testing (if needed)
  window.themeManager = { toggleTheme, applyTheme, getSystemTheme };

})();

