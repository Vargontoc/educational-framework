/**
 * Strict opt-in check for the Dev Content Manager mini-app.
 *
 * The feature is enabled ONLY when the environment variable
 * VITE_ENABLE_DEV_CONTENT is exactly the string 'true'.
 * Any other value (missing, empty, 'false', etc.) disables the feature.
 */
export function isDevContentEnabled(): boolean {
  return import.meta.env.VITE_ENABLE_DEV_CONTENT === 'true'
}
