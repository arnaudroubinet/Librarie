import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const url = 'http://localhost:4200/books';
  console.log('Navigating to', url);
  await page.goto(url, { waitUntil: 'networkidle' });

  // Wait for the mat-select trigger and click it to open the menu
  try {
    await page.waitForSelector('mat-select', { timeout: 5000 });
    await page.click('mat-select');
  } catch (e) {
    console.log('mat-select not found or click failed:', e.message);
  }

  // Wait for the overlay panel
  try {
    await page.waitForSelector('.mat-mdc-select-panel', { timeout: 5000 });
  } catch (e) {
    console.log('select panel not found:', e.message);
  }

  // Give small time for transitions
  await page.waitForTimeout(500);

  const out = 'books-page.png';
  await page.screenshot({ path: out, fullPage: true });
  console.log('Saved screenshot to', out);
  await browser.close();
})();
