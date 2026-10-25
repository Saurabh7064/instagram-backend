import { createRequire } from 'node:module'
import { mkdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..')
const require = createRequire(path.join(root, '../instagram-ui/package.json'))
const { chromium } = require('playwright')
const track = 'docs/interview/04-java/02-multithreading-from-scratch'
const index = JSON.parse(await readFile(path.join(root, track, 'foundations-index.json'), 'utf8'))
if (index.length !== 7) throw new Error('Expected seven foundation parts')
const cases = index.map(part => ({ ...part, relative: `lessons/00-foundations/${part.slug}.md` }))
cases.push({ id: '01', title: 'Starting and Joining One Worker', relative: 'lessons/01-start-and-join/README.md' })
const sourceFiles = [
  'src/test/java/com/instagram/backend/interview/multithreading/foundations/ThreadFoundationsExamples.java',
  'src/test/java/com/instagram/backend/interview/multithreading/startandjoin/StartAndJoinSolution.java',
]
const sources = await Promise.all(sourceFiles.map(file => readFile(path.join(root, file), 'utf8')))
const normalize = text => text.split('\n').map(line => line.trim()).filter(Boolean).join('\n')
const date = process.env.PROOF_DATE ?? new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Los_Angeles' }).format(new Date())
const proofDir = path.join(root, 'docs/demo-proofs', date)
const baseUrl = process.env.LEARNING_URL ?? 'http://localhost:5173'
const url = relative => `${baseUrl}/learn?lesson=${encodeURIComponent(`${track}/${relative.replace(/\.md$/, '')}`)}`
await mkdir(proofDir, { recursive: true })
const browser = await chromium.launch({ headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1200 } })
const errors = []
const results = []
page.on('pageerror', error => errors.push(error.message))
try {
  for (const lesson of cases) {
    const markdown = await readFile(path.join(root, track, lesson.relative), 'utf8')
    const response = await page.goto(url(lesson.relative), { waitUntil: 'networkidle' })
    if (response.status() !== 200) throw new Error(`HTTP error: ${lesson.id}`)
    if (!(await page.locator('.lesson-hero h1').innerText()).includes(lesson.title)) throw new Error(`Wrong title: ${lesson.id}`)
    const content = page.locator('.lesson-content')
    for (const name of ['Concept question', 'What you will learn', 'Understand the concept', 'Questions and explained answers', 'Stop/go']) {
      await content.getByRole('heading', { name, exact: true }).waitFor()
    }
    if (await content.locator('details').count() !== 3) throw new Error(`Wrong checkpoint count: ${lesson.id}`)
    if (await content.locator('table, .simulation-shell').count()) throw new Error(`Unexpected debugger presentation: ${lesson.id}`)
    if (/Narrated execution|Block-by-block code walkthrough|Iteration \w+=/.test(await content.innerText())) throw new Error(`Unexpected narration: ${lesson.id}`)
    const codeBlocks = [...markdown.matchAll(/```java\n([\s\S]*?)\n```/g)].map(match => match[1])
    for (const [i, code] of codeBlocks.entries()) {
      if (!sources.some(source => normalize(source).includes(normalize(code)))) throw new Error(`Snippet not in compiled source: ${lesson.id}`)
      const pre = content.locator('pre').nth(i)
      const rendered = await pre.locator('.code-line-text').allTextContents()
      if (rendered.join('\n').trim() !== code.trim()) throw new Error(`Code mismatch: ${lesson.id}`)
      if (await pre.locator('.code-line-number').count() !== code.split('\n').length) throw new Error(`Numbering mismatch: ${lesson.id}`)
    }
    for (const question of await content.locator('details').all()) {
      await question.locator('summary').click()
      if (await question.getAttribute('open') === null) throw new Error(`Answer not expanded: ${lesson.id}`)
      await question.locator('summary').click()
    }
    if (lesson.id === '00B' || lesson.id === '01') {
      await content.getByRole('heading', { name: 'Understand the concept', exact: true }).evaluate(el => el.scrollIntoView({ block: 'start' }))
      await page.evaluate(() => window.scrollBy(0, -100))
      await page.screenshot({ path: path.join(proofDir, lesson.id === '00B' ? 'thread-what-is-a-thread.png' : 'thread-start-join-after-foundations.png') })
    }
    results.push({ part: lesson.id, title: lesson.title, http: response.status(), explainedAnswers: 3, codeBlocks: codeBlocks.length })
  }
  // Verify the actual course entry begins before worker creation and links into the foundation.
  await page.goto(url('README.md'), { waitUntil: 'networkidle' })
  const content = page.locator('.lesson-content')
  if (!(await content.innerText()).includes('A thread is one execution path')) throw new Error('Missing definition at course entry')
  await content.getByRole('link', { name: '00A — A program and a running process', exact: true }).click()
  await page.getByRole('heading', { name: '00A — A program and a running process', exact: true }).waitFor()
  await page.locator('.lesson-content').getByRole('link', { name: '00B — What a thread actually is', exact: true }).click()
  await page.getByRole('heading', { name: '00B — What a thread actually is', exact: true }).waitFor()
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto(url('lessons/00-foundations/00b-what-is-a-thread.md'), { waitUntil: 'networkidle' })
  if (!(await page.getByRole('button', { name: 'Browse lessons' }).isVisible())) throw new Error('Missing mobile catalog control')
  if (await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth)) throw new Error('Mobile overflow')
  if (errors.length) throw new Error(errors.join('; '))
  await writeFile(path.join(proofDir, 'thread-foundations-browser-checks.json'), JSON.stringify({ results, navigation: 'course -> program/process -> what a thread is', mobilePageOverflow: false, pageErrors: errors }, null, 2) + '\n')
  console.log('Passed eight threading concept pages, 24 answer expansions, source/rendered snippets, prerequisite navigation, and mobile layout.')
} finally {
  await browser.close()
}
