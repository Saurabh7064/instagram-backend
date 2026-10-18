import { createRequire } from 'node:module'
import { mkdir, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const backendRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..')
const uiRoot = path.resolve(backendRoot, '../instagram-ui')
const require = createRequire(path.join(uiRoot, 'package.json'))
const { chromium } = require('playwright')
const date = process.env.PROOF_DATE ?? new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Los_Angeles' }).format(new Date())
const proofDir = path.join(backendRoot, 'docs/demo-proofs', date)
const moduleId = 'docs/interview/01-leetcode/lessons/04-prefix-suffix-intervals'
const cases = [
  ['01-range-sum-query-immutable', 'Range Sum Query', 'Construction iteration: index=0', 'RangeSumQuery'],
  ['02-subarray-sum-equals-k', 'Subarray Sum Equals K', 'Iteration index=0', 'SubarraySumEqualsK'],
  ['03-product-of-array-except-self', 'Product of Array Except Self', 'Left-pass iteration index=0', 'ProductExceptSelf'],
  ['04-merge-intervals', 'Merge Intervals', 'Merge iteration index=1', 'MergeIntervals'],
  ['05-insert-interval', 'Insert Interval', 'Overlap iteration index=1', 'InsertInterval'],
]
await mkdir(proofDir, { recursive: true })
const browser = await chromium.launch({ headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1100 } })
const errors = []
page.on('pageerror', error => errors.push(error.message))
const results = []
try {
  for (const [slug, title, narration, javaName] of cases) {
    const response = await page.goto(`http://127.0.0.1:5173/learn?lesson=${encodeURIComponent(`${moduleId}/${slug}/README`)}`, { waitUntil: 'networkidle' })
    if (response.status() !== 200) throw new Error(`HTTP ${response.status()} for ${slug}`)
    const content = page.locator('.lesson-content')
    if (!(await page.locator('.lesson-hero h1').innerText()).includes(title)) throw new Error(`Wrong lesson: ${slug}`)
    await content.getByText(narration, { exact: false }).first().waitFor()
    if (await content.locator('details').count() !== 3) throw new Error(`Expected 3 checkpoint answers: ${slug}`)
    if (await content.locator(`a[href*="${javaName}Practice.java"]`).count() < 1) throw new Error(`Missing practice link: ${slug}`)
    const heading = page.getByRole('heading', { name: 'Complete reference solution', exact: true })
    const reference = heading.locator('xpath=following-sibling::pre[1]')
    if (!(await reference.innerText()).includes('// Input:')) throw new Error(`Missing adjacent input: ${slug}`)
    const numbered = await reference.locator('.code-line-number').count()
    if (numbered < 5) throw new Error(`Missing reference line numbers: ${slug}`)
    if (await reference.locator('.code-line-number').first().innerText() !== '1') throw new Error(`Unstable first line: ${slug}`)
    // Capture the actual narration and table, not just the hero title.
    const paragraph = content.getByText(narration, { exact: false }).first()
    await paragraph.scrollIntoViewIfNeeded()
    await paragraph.evaluate(el => el.scrollIntoView({ block: 'start' }))
    await page.evaluate(() => window.scrollBy(0, -95))
    const screenshot = `${slug}-trace.png`
    await page.screenshot({ path: path.join(proofDir, screenshot) })
    results.push({ lesson: slug, http: response.status(), checkpoints: 3, referenceLines: numbered, screenshot })
  }
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto(`http://127.0.0.1:5173/learn?lesson=${encodeURIComponent(`${moduleId}/05-insert-interval/README`)}`, { waitUntil: 'networkidle' })
  if (!(await page.getByRole('button', { name: 'Browse lessons' }).isVisible())) throw new Error('Missing mobile catalog control')
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth)
  if (overflow) throw new Error('Document overflows mobile viewport')
  if (errors.length) throw new Error(`Browser errors: ${errors.join('; ')}`)
  await writeFile(path.join(proofDir, 'prefix-interval-browser-checks.json'), JSON.stringify({ results, mobileViewport: '390x844', mobilePageOverflow: false, pageErrors: errors }, null, 2) + '\n')
  console.log(`Passed ${results.length} new lesson browser checks, code line numbers, checkpoint/practice links, and mobile layout. Proofs: ${proofDir}`)
} finally {
  await browser.close()
}
