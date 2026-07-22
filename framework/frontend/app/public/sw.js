/**
 * Service Worker para PWA
 * 
 * Según SPRINT-002:
 * - PWA opcional, no promocionada
 * - Caché básica para recursos estáticos
 * - No interferir con acceso por URL
 */

const CACHE_NAME = 'nubi-cache-v1'
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/manifest.webmanifest'
]

/**
 * Instalación: cachear recursos estáticos básicos
 */
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(STATIC_ASSETS)
    })
  )
  self.skipWaiting()
})

/**
 * Activación: limpiar cachés antiguas
 */
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames
          .filter((name) => name !== CACHE_NAME)
          .map((name) => caches.delete(name))
      )
    })
  )
  self.clients.claim()
})

/**
 * Fetch: estrategia stale-while-revalidate para recursos estáticos
 * Network-first para API calls
 */
self.addEventListener('fetch', (event) => {
  const { request } = event
  const url = new URL(request.url)

  // API calls: network-first
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(request).catch(() => {
        return new Response(
          JSON.stringify({ error: 'Offline' }),
          { status: 503, headers: { 'Content-Type': 'application/json' } }
        )
      })
    )
    return
  }

  // Recursos estáticos: stale-while-revalidate
  event.respondWith(
    caches.match(request).then((cached) => {
      const fetchPromise = fetch(request).then((response) => {
        if (response && response.status === 200) {
          const responseClone = response.clone()
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(request, responseClone)
          })
        }
        return response
      }).catch(() => cached)

      return cached || fetchPromise
    })
  )
})
