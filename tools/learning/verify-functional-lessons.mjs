import { createRequire } from 'node:module'
import { mkdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const backendRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..')
const uiRoot = path.resolve(backendRoot, '../instagram-ui')
const require = createRequire(path.join(uiRoot, 'package.json'))
const { chromium } = require('playwright')
const track = 'docs/interview/04-java/01-functional-programming-coding'
const lessons = JSON.parse(await readFile(path.join(backendRoot, track, 'concept-index.json'), 'utf8'))
if (lessons.length !== 28) throw new Error('Expected 28 authored concept lessons')
const javaLines = (await readFile(path.join(backendRoot, 'src/test/java/com/instagram/backend/interview/functionalprogramming/concepts/FunctionalConceptExamples.java'), 'utf8')).split('\n')
const date = process.env.PROOF_DATE ?? new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Los_Angeles' }).format(new Date())
const proofDir = path.join(backendRoot, 'docs/demo-proofs', date)
const baseUrl = process.env.LEARNING_URL ?? 'http://localhost:5173'
const lessonUrl = id => `${baseUrl}/learn?lesson=${encodeURIComponent(`${track}/${id}`)}`
const proofs = new Map([[2, 'functional-lambda-concept.png'], [8, 'functional-map-concept.png'], [9, 'functional-flatmap-concept.png']])
await mkdir(proofDir, { recursive: true })
const browser = await chromium.launch({ headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1200 } })
const errors = []
page.on('pageerror', error => errors.push(error.message))
const results = []
try {
  for (const lesson of lessons) {
    const response = await page.goto(lessonUrl(`lessons/${lesson.slug}/README`), { waitUntil: 'networkidle' })
    if (response.status() !== 200) throw new Error(`HTTP ${response.status()} for ${lesson.slug}`)
    const title = await page.locator('.lesson-hero h1').innerText()
    if (!title.includes(lesson.title)) throw new Error(`Wrong lesson title: ${lesson.slug}: ${title}`)
    const content = page.locator('.lesson-content')
    for (const name of ['Concept question', 'Understand the concept', 'Small Java example', 'Read the syntax and result', 'Pitfalls and decisions', 'Questions and explained answers', 'Stop/go']) {
      await content.getByRole('heading', { name, exact: true }).waitFor()
    }
    if (await content.locator('details').count() !== 3) throw new Error(`Expected 3 checkpoint answers: ${lesson.slug}`)
    if (await content.locator('table, .simulation-shell').count()) throw new Error(`Unexpected debugger table/simulation: ${lesson.slug}`)
    if (/Narrated execution|Block-by-block code walkthrough|Iteration (?:index|right|left)=|Emission \d+:/.test(await content.innerText())) {
      throw new Error(`Unexpected iteration narration: ${lesson.slug}`)
    }
    if (await content.locator('a[href*="FunctionalConceptExamples.java"]').count() !== 1) throw new Error(`Missing runnable example: ${lesson.slug}`)
    const markdown = await readFile(path.join(backendRoot, track, 'lessons', lesson.slug, 'README.md'), 'utf8')
    const expectedCode = markdown.match(/```java\n([\s\S]*?)\n```/)[1]
    const firstSourceLine = Number(markdown.match(/FunctionalConceptExamples\.java#L(\d+)/)[1])
    const compiledCode = javaLines.slice(firstSourceLine - 1, firstSourceLine - 1 + expectedCode.split('\n').length).map(line => line.slice(8)).join('\n')
    if (compiledCode !== expectedCode) throw new Error(`Java source differs from lesson: ${lesson.slug}`)
    const renderedLines = await content.locator('pre').first().locator('.code-line-text').allTextContents()
    if (renderedLines.join('\n').trim() !== expectedCode.trim()) throw new Error(`Rendered example differs: ${lesson.slug}`)
    const numbered = await content.locator('pre').first().locator('.code-line-number').count()
    if (numbered !== expectedCode.split('\n').length) throw new Error(`Wrong code numbering: ${lesson.slug}`)
    for (const question of await content.locator('details').all()) {
      await question.locator('summary').click()
      if (await question.getAttribute('open') === null) throw new Error(`Answer did not open: ${lesson.slug}`)
      await question.locator('summary').click()
    }
    if (proofs.has(lesson.n)) {
      await content.getByRole('heading', { name: 'Understand the concept', exact: true }).evaluate(el => el.scrollIntoView({ block: 'start' }))
      await page.evaluate(() => window.scrollBy(0, -100))
      await page.screenshot({ path: path.join(proofDir, proofs.get(lesson.n)) })
    }
    results.push({ lesson: lesson.slug, http: response.status(), checkpoints: 3, numberedExampleLines: numbered })
  }
  // Integration: course table -> map -> its next flatMap lesson.
  await page.goto(lessonUrl('README'), { waitUntil: 'networkidle' })
  await page.locator('.lesson-content').getByRole('link', { name: 'map: transform one value into another', exact: true }).click()
  await page.getByRole('heading', { name: '08 — map: transform one value into another', exact: true }).waitFor()
  await page.locator('.lesson-content').getByRole('link', { name: '09 — flatMap: flatten nested results', exact: true }).click()
  await page.getByRole('heading', { name: '09 — flatMap: flatten nested results', exact: true }).waitFor()
  for (const [oldSlug, target] of [['03-stream-transformations', '06-stream-model'], ['04-array-concatenation', '18-primitive-streams-and-arrays']]) {
    await page.goto(lessonUrl(`lessons/${oldSlug}/README`), { waitUntil: 'networkidle' })
    if (await page.locator(`.lesson-content a[href*="${target}"]`).count() !== 1) throw new Error(`Broken compatibility route: ${oldSlug}`)
  }
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto(lessonUrl('lessons/09-flatmap/README'), { waitUntil: 'networkidle' })
  if (!(await page.getByRole('button', { name: 'Browse lessons' }).isVisible())) throw new Error('Missing mobile catalog control')
  if (await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth)) throw new Error('Mobile page overflow')
  if (errors.length) throw new Error(`Browser errors: ${errors.join('; ')}`)
  await writeFile(path.join(proofDir, 'functional-concept-browser-checks.json'), JSON.stringify({ results, navigation: 'course -> map -> flatMap', legacyRoutes: 2, mobilePageOverflow: false, pageErrors: errors, screenshots: [...proofs.values()] }, null, 2) + '\n')
  console.log(`Passed ${results.length} concept pages, 84 checkpoints, exact numbered examples, navigation, two compatibility routes, and mobile layout. Proofs: ${proofDir}`)
} finally {
  await browser.close()
}
