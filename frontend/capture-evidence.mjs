import { chromium } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

async function captureEvidence() {
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();

  try {
    console.log('🔍 Starting accessibility evidence capture...');

    // 1. Navigate to the app
    await page.goto('http://localhost:4200');
    await page.waitForLoadState('networkidle');

    // 2. Capture initial state (no skip link visible)
    console.log('📸 Capturing initial state...');
    await page.screenshot({ 
      path: '/tmp/T-015-initial-state.png',
      fullPage: false
    });

    // 3. Press Tab to show skip link
    console.log('📸 Capturing skip link on focus...');
    await page.keyboard.press('Tab');
    await page.waitForTimeout(500); // Wait for focus styles
    await page.screenshot({ 
      path: '/tmp/T-015-skip-link-visible.png',
      fullPage: false
    });

    // 4. Navigate to books page
    await page.goto('http://localhost:4200/books');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000); // Wait for content to load

    // 5. Capture keyboard navigation with focus indicators
    console.log('📸 Capturing keyboard navigation...');
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    await page.waitForTimeout(500);
    await page.screenshot({ 
      path: '/tmp/T-015-keyboard-navigation.png',
      fullPage: false
    });

    // 6. Run axe accessibility scan
    console.log('🔍 Running axe accessibility scan...');
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
      .analyze();

    console.log('\n📊 Accessibility Scan Results:');
    console.log(`Total violations: ${accessibilityScanResults.violations.length}`);
    
    const criticalViolations = accessibilityScanResults.violations.filter(v => v.impact === 'critical');
    const seriousViolations = accessibilityScanResults.violations.filter(v => v.impact === 'serious');
    const moderateViolations = accessibilityScanResults.violations.filter(v => v.impact === 'moderate');
    const minorViolations = accessibilityScanResults.violations.filter(v => v.impact === 'minor');

    console.log(`  - Critical: ${criticalViolations.length}`);
    console.log(`  - Serious: ${seriousViolations.length}`);
    console.log(`  - Moderate: ${moderateViolations.length}`);
    console.log(`  - Minor: ${minorViolations.length}`);

    if (accessibilityScanResults.violations.length > 0) {
      console.log('\n❌ Violations found:');
      accessibilityScanResults.violations.slice(0, 5).forEach(v => {
        console.log(`  - [${v.impact}] ${v.id}: ${v.description}`);
      });
    }

    // 7. Open DevTools to inspect ARIA labels
    console.log('📸 Capturing ARIA labels in DevTools...');
    // Navigate to element with ARIA labels
    const nav = page.locator('nav.motspassants-sidebar');
    await nav.scrollIntoViewIfNeeded();
    
    // Take screenshot of the navigation with ARIA
    await page.screenshot({ 
      path: '/tmp/T-015-aria-implementation.png',
      fullPage: false
    });

    console.log('\n✅ Evidence capture complete!');
    console.log('\nScreenshots saved to /tmp/');

  } catch (error) {
    console.error('❌ Error capturing evidence:', error);
  } finally {
    await browser.close();
  }
}

captureEvidence();
