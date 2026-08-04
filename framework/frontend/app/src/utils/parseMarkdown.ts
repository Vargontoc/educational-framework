import { marked } from 'marked'
import DOMPurify from 'dompurify'

export function parseMarkdown(content: string): string {
  const html = marked.parse(content, { async: false }) as string
  return DOMPurify.sanitize(html)
}
