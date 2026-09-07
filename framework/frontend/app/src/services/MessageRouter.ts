import { BinaryFrameParser } from './BinaryFrameParser'

export class MessageRouter {
  static route(
    message: MessageEvent['data'],
    onJson: (data: unknown) => void,
    onBinary: (buffer: ArrayBuffer) => void
  ): void {
    if (typeof message === 'string') {
      try {
        const data = JSON.parse(message)
        onJson(data)
      } catch (error) {
        console.error('Error parsing JSON message:', error)
      }
    } else if (BinaryFrameParser.isValidBinaryFrame(message)) {
      onBinary(message)
    } else {
      console.warn('Unknown message type received')
    }
  }
}
