/**
 * Cache en memoria para AudioBuffer de audio dinámico recibido.
 * Implementa estrategia LRU (Least Recently Used) con límite de 50 entradas.
 */
export class AudioCache {
  private static readonly MAX_SIZE = 50

  private cache: Map<string, AudioBuffer> = new Map()
  private accessOrder: string[] = []

  /**
   * Almacena un AudioBuffer en la cache.
   * Si se excede el límite, elimina el menos recientemente usado.
   */
  set(audioId: string, buffer: AudioBuffer): void {
    if (this.cache.has(audioId)) {
      this.moveToEnd(audioId)
      this.cache.set(audioId, buffer)
      return
    }

    if (this.cache.size >= AudioCache.MAX_SIZE) {
      this.evictLRU()
    }

    this.cache.set(audioId, buffer)
    this.accessOrder.push(audioId)
  }

  /**
   * Obtiene un AudioBuffer de la cache.
   * Actualiza el orden de acceso (LRU).
   */
  get(audioId: string): AudioBuffer | undefined {
    const buffer = this.cache.get(audioId)
    if (buffer) {
      this.moveToEnd(audioId)
    }
    return buffer
  }

  /**
   * Verifica si un audioId existe en la cache.
   */
  has(audioId: string): boolean {
    return this.cache.has(audioId)
  }

  /**
   * Elimina un AudioBuffer de la cache.
   */
  delete(audioId: string): void {
    this.cache.delete(audioId)
    const index = this.accessOrder.indexOf(audioId)
    if (index !== -1) {
      this.accessOrder.splice(index, 1)
    }
  }

  /**
   * Limpia toda la cache.
   */
  clear(): void {
    this.cache.clear()
    this.accessOrder = []
  }

  /**
   * Retorna el número de elementos en la cache.
   */
  getSize(): number {
    return this.cache.size
  }

  /**
   * Mueve un audioId al final del orden de acceso (más recientemente usado).
   */
  private moveToEnd(audioId: string): void {
    const index = this.accessOrder.indexOf(audioId)
    if (index !== -1) {
      this.accessOrder.splice(index, 1)
      this.accessOrder.push(audioId)
    }
  }

  /**
   * Elimina el elemento menos recientemente usado (LRU).
   */
  private evictLRU(): void {
    if (this.accessOrder.length === 0) return
    const lruKey = this.accessOrder.shift()
    if (lruKey) {
      this.cache.delete(lruKey)
    }
  }
}
