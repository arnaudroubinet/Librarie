# Keyboard Navigation Guide for MotsPassants

This guide documents all keyboard shortcuts and navigation patterns in the MotsPassants application.

## Global Keyboard Shortcuts

### Navigation
- **Tab**: Move focus to the next interactive element
- **Shift + Tab**: Move focus to the previous interactive element
- **Enter**: Activate focused link or button
- **Space**: Activate focused button or toggle control
- **Escape**: Close modals, dropdowns, or cancel current operation

### Skip Links
- **Tab** (first press on page load): Focus skip link
- **Enter** (when skip link focused): Jump to main content

## Page-Specific Shortcuts

### Search Page
- **/**: Focus search input (from anywhere on the page, unless already in an input)
- **Escape**: Clear search suggestions and unfocus search input
- **Enter** (in search input): Perform search
- **Arrow Up/Down** (in search suggestions): Navigate suggestions
- **Enter** (on suggestion): Apply suggestion

### Book List
- **Tab**: Navigate through book cards and controls
- **Enter**: Open selected book details
- **Arrow Keys** (when grid focused): Navigate between book cards (future enhancement)

### Book Details
- **Escape**: Return to previous page
- **Tab**: Navigate through action buttons and metadata
- **Enter**: Activate focused action (Read, Download, etc.)

### E-book Reader
- **Left Arrow**: Previous page
- **Right Arrow**: Next page
- **Space**: Next page
- **Shift + Space**: Previous page
- **Home**: First page
- **End**: Last page
- **+**: Zoom in
- **-**: Zoom out
- **0**: Reset zoom
- **F**: Toggle fullscreen
- **Escape**: Exit fullscreen or close reader

## Form Controls

### Text Inputs
- **Tab**: Move to next field
- **Shift + Tab**: Move to previous field
- **Enter**: Submit form (if in last field or if submit button is default)
- **Escape**: Clear field (if supported)

### Dropdowns (mat-select)
- **Enter** or **Space**: Open dropdown
- **Arrow Up/Down**: Navigate options
- **Home**: First option
- **End**: Last option
- **Enter**: Select highlighted option
- **Escape**: Close dropdown without selecting

### Checkboxes and Radio Buttons
- **Space**: Toggle checkbox or select radio button
- **Arrow Keys**: Navigate between radio buttons in a group

### Date Pickers
- **Space** or **Enter**: Open calendar
- **Arrow Keys**: Navigate dates
- **Page Up/Down**: Previous/Next month
- **Home**: First day of month
- **End**: Last day of month
- **Enter**: Select highlighted date
- **Escape**: Close calendar

## Accessibility Features

### Screen Reader Support
- All interactive elements have accessible names
- Page structure uses semantic HTML and ARIA landmarks
- Dynamic content changes are announced
- Loading states and errors are announced immediately

### Keyboard Focus
- Focus indicators are visible on all interactive elements
- Tab order follows visual layout
- Focus is managed in modals and dynamic content
- Skip links allow bypassing repetitive content

### Visual Indicators
- 2:1 minimum focus indicator contrast ratio
- Focus outline visible against all backgrounds
- Current page highlighted in navigation
- Active element clearly indicated

## Tips for Keyboard Users

### Efficient Navigation
1. Use skip links to bypass navigation
2. Use browser search (Ctrl+F) to find content quickly
3. Learn the search shortcut (/) for quick access
4. Use Tab to jump between major sections

### Troubleshooting
- **Focus not visible**: Try adjusting display settings or using a different browser
- **Shortcut not working**: Ensure you're not in an input field
- **Dropdown won't open**: Try using Space instead of Enter
- **Can't reach an element**: Check if it's within a modal or hidden section

## Browser-Specific Notes

### Chrome/Edge
- All keyboard shortcuts work as expected
- F6 cycles through page sections (browser feature)
- Ctrl+L focuses address bar

### Firefox
- All keyboard shortcuts work as expected
- F7 enables caret browsing for text selection
- Alt+Shift+Tab for reverse tab order

### Safari
- Tab might skip some elements by default
- Enable "Press Tab to highlight each item on a webpage" in Preferences > Advanced
- Option+Tab for application switching doesn't interfere

## Future Enhancements

### Planned Shortcuts
- **G + H**: Go to home (book list)
- **G + S**: Go to search
- **G + A**: Go to authors
- **G + R**: Go to series
- **?**: Show keyboard shortcuts help
- **1-9**: Quick navigate to nth item in list

### Planned Features
- Grid navigation with arrow keys
- Customizable keyboard shortcuts
- Vim-style navigation mode
- Mouse-free book reading

## Reporting Issues

If you encounter keyboard navigation issues:
1. Note the page and specific element
2. Describe the expected vs. actual behavior
3. List your browser and operating system
4. Report via GitHub issues with "accessibility" label

## Testing

To test keyboard navigation:
```bash
# Run automated accessibility tests
npm run test:a11y

# Run with visible browser to observe focus
npm run test:a11y:headed
```

## Resources

- [WebAIM: Keyboard Accessibility](https://webaim.org/techniques/keyboard/)
- [W3C: Keyboard Navigation](https://www.w3.org/WAI/WCAG21/Understanding/keyboard.html)
- [ARIA Authoring Practices - Keyboard Patterns](https://www.w3.org/WAI/ARIA/apg/patterns/)
