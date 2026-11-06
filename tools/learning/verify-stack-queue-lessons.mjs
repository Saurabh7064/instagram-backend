import assert from 'node:assert/strict'
import { createRequire } from 'node:module'
import { access, mkdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const backendRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..')
const uiRoot = process.env.UI_ROOT ?? path.resolve(backendRoot, '../instagram-ui')
const require = createRequire(path.join(uiRoot, 'package.json'))
const { chromium } = require('playwright')
const baseUrl = process.env.LEARNING_BASE_URL ?? 'http://127.0.0.1:5173'
const date = process.env.PROOF_DATE ?? new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Los_Angeles' }).format(new Date())
const proofDir = path.join(backendRoot, 'docs/demo-proofs', date)
const curriculumRoot = await access(path.join(backendRoot, 'docs/learn/01-leetcode')).then(() => 'docs/learn').catch(() => 'docs/interview')
const moduleId = `${curriculumRoot}/01-leetcode/lessons/05-stack-queue`
const cases = [
  ['01-valid-parentheses', 'Valid Parentheses', 'ValidParentheses'],
  ['02-min-stack', 'Min Stack', 'MinStack'],
  ['03-daily-temperatures', 'Daily Temperatures', 'DailyTemperatures'],
  ['04-evaluate-reverse-polish-notation', 'Evaluate Reverse Polish Notation', 'EvaluateRpn'],
  ['05-sliding-window-maximum', 'Sliding Window Maximum', 'SlidingWindowMaximum'],
]

async function verifyLocalLinks(sourcePath) {
  const markdown = await readFile(path.join(backendRoot, sourcePath), 'utf8')
  let count = 0
  for (const match of markdown.matchAll(/\[[^\]]+\]\(([^)]+)\)/g)) {
    const href = match[1].replace(/^<|>$/g, '').split('#')[0]
    if (!href || /^(?:https?:|mailto:)/.test(href)) continue
    await access(path.resolve(backendRoot, path.dirname(sourcePath), decodeURIComponent(href)))
    count++
  }
  return count
}

await mkdir(proofDir, { recursive: true })
const browser = await chromium.launch({ headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1100 } })
const errors = []
page.on('pageerror', error => errors.push(error.message))
const results = []
try {
  const moduleLocalLinks = await verifyLocalLinks(`${moduleId}/README.md`)
  for (const [slug, title, javaName] of cases) {
    const sourcePath = `${moduleId}/${slug}/README.md`
    const localLinks = await verifyLocalLinks(sourcePath)
    const response = await page.goto(`${baseUrl}/learn?lesson=${encodeURIComponent(sourcePath.replace(/\.md$/, ''))}`, { waitUntil: 'networkidle' })
    assert.equal(response.status(), 200, `HTTP response for ${slug}`)
    assert.ok((await page.locator('.lesson-hero h1').innerText()).includes(title), `Wrong lesson selected for ${slug}`)
    const content = page.locator('.lesson-content')
    const headings = await content.locator('h2').allTextContents()
    assert.equal(headings[0], 'Question', `First lesson section for ${slug}`)
    assert.equal(headings[1], 'What you will learn', `Second lesson section for ${slug}`)
    await content.getByRole('heading', { name: 'Block-by-block code walkthrough', exact: true }).waitFor()
    assert.ok(await content.locator(`a[href$="${javaName}Practice.java"]`).count() > 0, `Missing practice link for ${slug}`)
    assert.ok(await content.locator(`a[href$="${javaName}Solution.java"]`).count() > 0, `Missing reference link for ${slug}`)
    const answers = content.locator('details')
    assert.equal(await answers.count(), 3, `Checkpoint count for ${slug}`)
    for (let index = 0; index < 3; index++) {
      const answer = answers.nth(index)
      await answer.locator('summary').click()
      assert.equal(await answer.getAttribute('open'), '', `Answer expands for ${slug} checkpoint ${index + 1}`)
      const text = await answer.innerText()
      const question = await answer.locator('summary').innerText()
      assert.ok(text.replace(question, '').trim().length > 40, `Explained answer missing for ${slug}`)
      await answer.locator('summary').click()
    }
    const heading = content.getByRole('heading', { name: 'Complete reference solution', exact: true })
    const reference = heading.locator('xpath=following-sibling::pre[1]')
    const referenceText = await reference.innerText()
    assert.ok(referenceText.includes('// Input:'), `Reference input missing for ${slug}`)
    assert.ok(referenceText.includes('// Expected'), `Reference expected result missing for ${slug}`)
    const blocks = content.locator('pre')
    let numberedLines = 0
    for (let index = 0; index < await blocks.count(); index++) {
      const numbers = await blocks.nth(index).locator('.code-line-number').allTextContents()
      assert.ok(numbers.length > 0, `Unnumbered code block ${index} in ${slug}`)
      assert.deepEqual(numbers, numbers.map((_, line) => String(line + 1)), `Unstable code line numbers for ${slug}`)
      numberedLines += numbers.length
    }
    assert.ok(await content.locator('table').count() > 0, `Missing execution state table for ${slug}`)
    const narration = content.locator('strong').filter({ hasText: 'Narrated execution' }).first()
    await narration.waitFor()
    await narration.evaluate(element => {
      const paragraph = element.closest('p') ?? element
      window.scrollTo({ top: window.scrollY + paragraph.getBoundingClientRect().top - 100, behavior: 'instant' })
    })
    const screenshot = `stack-queue-${slug}-trace.png`
    await page.screenshot({ path: path.join(proofDir, screenshot) })
    results.push({ lesson: slug, http: response.status(), checkpointsExpanded: 3, localLinks, codeBlocks: await blocks.count(), numberedLines, referenceLines: await reference.locator('.code-line-number').count(), screenshot })
  }
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto(`${baseUrl}/learn?lesson=${encodeURIComponent(`${moduleId}/05-sliding-window-maximum/README`)}`, { waitUntil: 'networkidle' })
  assert.equal(await page.getByRole('button', { name: 'Browse lessons', exact: true }).isVisible(), true, 'Mobile catalog control')
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth), false, 'Mobile document overflow')
  await page.screenshot({ path: path.join(proofDir, 'stack-queue-mobile.png') })
  assert.deepEqual(errors, [], 'Browser errors')
  await writeFile(path.join(proofDir, 'stack-queue-browser-checks.json'), JSON.stringify({ baseUrl, moduleLocalLinks, results, mobileViewport: '390x844', mobilePageOverflow: false, pageErrors: errors }, null, 2) + '\n')
  console.log(`Passed ${results.length} lesson browser checks: local links, 15 expanded checkpoint answers, numbered code, full reference inputs/results, state tables, mobile layout, no browser errors. Proofs: ${proofDir}`)
} finally {
  await browser.close()
}
