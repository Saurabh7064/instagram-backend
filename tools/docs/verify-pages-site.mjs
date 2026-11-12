import assert from 'node:assert/strict'
import { promises as fs } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url))
const repositoryRoot = path.resolve(scriptDirectory, '../..')
const docsRoot = path.join(repositoryRoot, 'docs')

async function collectFiles(directory, predicate) {
  const entries = await fs.readdir(directory, { withFileTypes: true })
  const files = await Promise.all(
    entries.map(async (entry) => {
      const absolutePath = path.join(directory, entry.name)
      if (entry.isDirectory()) return collectFiles(absolutePath, predicate)
      return entry.isFile() && predicate(absolutePath) ? [absolutePath] : []
    }),
  )
  return files.flat()
}

const indexHtml = await fs.readFile(path.join(docsRoot, 'index.html'), 'utf8')
const sidebar = await fs.readFile(path.join(docsRoot, '_sidebar.md'), 'utf8')
await fs.access(path.join(docsRoot, '.nojekyll'))

assert.match(indexHtml, /docsify@4\/lib\/docsify\.min\.js/, 'Docsify runtime is missing')
assert.match(indexHtml, /repositoryName = 'instagram-backend'/, 'Project Pages base path is missing')
assert.match(indexHtml, /homepage: 'index\.md'/, 'Documentation home page is not configured')
assert.match(indexHtml, /loadSidebar: true/, 'Generated navigation is not enabled')
assert.match(indexHtml, /plugins\/search\.min\.js/, 'Full-text search plugin is missing')

const markdownFiles = await collectFiles(docsRoot, (absolutePath) => absolutePath.endsWith('.md'))
const expectedPaths = markdownFiles
  .map((absolutePath) => path.relative(docsRoot, absolutePath).split(path.sep).join('/'))
  .filter((relativePath) => relativePath !== '_sidebar.md')
  .sort()

const linkedPaths = [...sidebar.matchAll(/\]\(([^)]+\.md)\)/g)]
  .map((match) => match[1])
  .filter((relativePath) => relativePath !== 'index.md')
  .sort()

const expectedWithoutHome = expectedPaths.filter((relativePath) => relativePath !== 'index.md')
assert.deepEqual(linkedPaths, expectedWithoutHome, 'Sidebar must link every Markdown document exactly once')

let detailDocuments = 0
let detailBlocks = 0
for (const relativePath of expectedPaths) {
  const markdown = await fs.readFile(path.join(docsRoot, relativePath), 'utf8')
  const openingDetails = markdown.match(/<details(?:\s[^>]*)?>/g) ?? []
  if (openingDetails.length === 0) continue

  const closingDetails = markdown.match(/<\/details>/g) ?? []
  const summaries = markdown.match(/<summary(?:\s[^>]*)?>/g) ?? []
  assert.equal(closingDetails.length, openingDetails.length, `${relativePath} has unclosed <details> blocks`)
  assert.equal(summaries.length, openingDetails.length, `${relativePath} must give each <details> a clickable question`)
  detailDocuments += 1
  detailBlocks += openingDetails.length
}

console.log(
  `Verified GitHub Pages site: ${expectedPaths.length} documents, ${detailBlocks} expandable blocks across ${detailDocuments} documents`,
)
