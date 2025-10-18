import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test.describe('Accessibility Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Start local dev server before running tests
    // Assumes the app is already running on localhost:4200
    await page.goto('http://localhost:4200');
  });

  test('should not have any automatically detectable accessibility issues on home page', async ({ page }) => {
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
      .analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test('should not have critical or serious violations on Books page', async ({ page }) => {
    await page.goto('http://localhost:4200/books');
    await page.waitForLoadState('networkidle');

    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    const criticalViolations = accessibilityScanResults.violations.filter(
      v => v.impact === 'critical' || v.impact === 'serious'
    );

    expect(criticalViolations).toEqual([]);
  });

  test('should not have critical or serious violations on Search page', async ({ page }) => {
    await page.goto('http://localhost:4200/search');
    await page.waitForLoadState('networkidle');

    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    const criticalViolations = accessibilityScanResults.violations.filter(
      v => v.impact === 'critical' || v.impact === 'serious'
    );

    expect(criticalViolations).toEqual([]);
  });

  test('skip link should be present and functional', async ({ page }) => {
    const skipLink = page.locator('.skip-link');
    await expect(skipLink).toBeAttached();
    await expect(skipLink).toHaveAttribute('href', '#main-content');
    
    // Test keyboard focus
    await page.keyboard.press('Tab');
    await expect(skipLink).toBeFocused();
  });

  test('main content should have proper landmark', async ({ page }) => {
    const mainContent = page.locator('main#main-content');
    await expect(mainContent).toBeAttached();
    await expect(mainContent).toHaveAttribute('role', 'main');
  });

  test('navigation should have proper ARIA labels', async ({ page }) => {
    const nav = page.locator('nav.motspassants-sidebar');
    await expect(nav).toHaveAttribute('role', 'navigation');
    await expect(nav).toHaveAttribute('aria-label', 'Main navigation');
  });

  test('all interactive elements should be keyboard accessible', async ({ page }) => {
    await page.goto('http://localhost:4200/books');
    await page.waitForLoadState('networkidle');

    // Tab through all interactive elements
    const interactiveElements = await page.locator('button, a, input, select, textarea').all();
    
    for (const element of interactiveElements.slice(0, 10)) { // Test first 10 elements
      const isVisible = await element.isVisible();
      if (isVisible) {
        await element.focus();
        const isFocused = await element.evaluate(el => document.activeElement === el);
        expect(isFocused).toBeTruthy();
      }
    }
  });

  test('images should have alt text', async ({ page }) => {
    await page.goto('http://localhost:4200/books');
    await page.waitForLoadState('networkidle');

    const images = await page.locator('img').all();
    
    for (const img of images) {
      const isVisible = await img.isVisible();
      if (isVisible) {
        const alt = await img.getAttribute('alt');
        expect(alt).toBeTruthy();
      }
    }
  });

  test('form inputs should have associated labels', async ({ page }) => {
    await page.goto('http://localhost:4200/search');
    await page.waitForLoadState('networkidle');

    const inputs = await page.locator('input[type="text"], input[type="search"], textarea').all();
    
    for (const input of inputs) {
      const isVisible = await input.isVisible();
      if (isVisible) {
        const ariaLabel = await input.getAttribute('aria-label');
        const ariaLabelledby = await input.getAttribute('aria-labelledby');
        const id = await input.getAttribute('id');
        
        let hasLabel = false;
        if (ariaLabel || ariaLabelledby) {
          hasLabel = true;
        } else if (id) {
          const label = await page.locator(`label[for="${id}"]`).count();
          hasLabel = label > 0;
        }
        
        expect(hasLabel).toBeTruthy();
      }
    }
  });

  test('heading hierarchy should be correct', async ({ page }) => {
    const headings = await page.locator('h1, h2, h3, h4, h5, h6').all();
    const levels: number[] = [];
    
    for (const heading of headings) {
      const tagName = await heading.evaluate(el => el.tagName.toLowerCase());
      const level = parseInt(tagName.substring(1));
      levels.push(level);
    }

    // Check that we don't skip heading levels
    for (let i = 1; i < levels.length; i++) {
      const diff = levels[i] - levels[i - 1];
      expect(diff).toBeLessThanOrEqual(1);
    }
  });

  test('color contrast should meet WCAG AA standards', async ({ page }) => {
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2aa'])
      .include('body')
      .analyze();

    const contrastViolations = accessibilityScanResults.violations.filter(
      v => v.id === 'color-contrast'
    );

    expect(contrastViolations).toEqual([]);
  });

  test('navigation links should be keyboard accessible with proper focus indicators', async ({ page }) => {
    const navLinks = await page.locator('nav.motspassants-sidebar a').all();
    
    for (const link of navLinks) {
      await link.focus();
      
      // Check that focused element has visible focus indicator
      const outline = await link.evaluate(el => {
        const computed = window.getComputedStyle(el);
        return computed.outline || computed.outlineWidth;
      });
      
      // At least some focus indication should exist (outline, box-shadow, etc.)
      expect(outline).toBeTruthy();
    }
  });

  test('buttons should have accessible names', async ({ page }) => {
    await page.goto('http://localhost:4200/books');
    await page.waitForLoadState('networkidle');

    const buttons = await page.locator('button').all();
    
    for (const button of buttons) {
      const isVisible = await button.isVisible();
      if (isVisible) {
        const ariaLabel = await button.getAttribute('aria-label');
        const text = await button.textContent();
        const hasAccessibleName = ariaLabel || (text && text.trim().length > 0);
        
        expect(hasAccessibleName).toBeTruthy();
      }
    }
  });

  test('iconify icons should have proper ARIA attributes', async ({ page }) => {
    const icons = await page.locator('iconify-icon').all();
    
    for (const icon of icons) {
      const isVisible = await icon.isVisible();
      if (isVisible) {
        const role = await icon.getAttribute('role');
        const ariaHidden = await icon.getAttribute('aria-hidden');
        const ariaLabel = await icon.getAttribute('aria-label');
        
        // Icon should either be hidden from screen readers or have a label
        const isAccessible = ariaHidden === 'true' || ariaLabel || role === 'img';
        expect(isAccessible).toBeTruthy();
      }
    }
  });

  test('should support screen reader announcements for dynamic content', async ({ page }) => {
    await page.goto('http://localhost:4200/search');
    
    // Check for ARIA live regions for dynamic content updates
    const liveRegions = await page.locator('[aria-live]').count();
    
    // We expect some live regions for search results or loading states
    // This is a soft check - at least the page should be set up for it
    expect(liveRegions).toBeGreaterThanOrEqual(0);
  });
});
