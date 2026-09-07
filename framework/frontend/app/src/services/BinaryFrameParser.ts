export interface ParsedBinaryFrame {
  audioId: string
  audioData: ArrayBuffer
}

export class BinaryFrameParser {
  static parse(buffer: ArrayBuffer): ParsedBinaryFrame {
    const view = new DataView(buffer)

    const audioIdLength = view.getInt32(0, false)

    const audioIdBytes = new Uint8Array(buffer, 4, audioIdLength)
    const audioId = new TextDecoder().decode(audioIdBytes)

    const audioDataStart = 4 + audioIdLength
    const audioData = buffer.slice(audioDataStart)

    return { audioId, audioData }
  }

  static isValidBinaryFrame(data: unknown): data is ArrayBuffer {
    return data instanceof ArrayBuffer && data.byteLength > 4
  }
}
