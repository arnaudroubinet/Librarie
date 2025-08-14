import { Directive, EventEmitter, Input, Output, ElementRef, OnInit, OnDestroy } from '@angular/core';

@Directive({
  selector: '[appInfiniteScroll]',
  standalone: true
})
export class InfiniteScrollDirective implements OnInit, OnDestroy {
  @Input() threshold = 200; // pixels from bottom to trigger load
  @Input() disabled = false;
  @Output() scrolled = new EventEmitter<void>();

  private scrollListener?: () => void;
  private resizeListener?: () => void;
  private scrollTarget: Window | HTMLElement = window;

  constructor(private element: ElementRef) {}

  ngOnInit() {
    this.scrollTarget = this.findScrollContainer(this.element.nativeElement) ?? window;
    this.setupScrollListener();
    // Trigger once on init in case content is short and we need to load next pages
    setTimeout(() => this.checkAndEmit(), 0);
  }

  ngOnDestroy() {
    if (this.scrollListener) {
      if (this.scrollTarget === window) {
        window.removeEventListener('scroll', this.scrollListener);
      } else {
        (this.scrollTarget as HTMLElement).removeEventListener('scroll', this.scrollListener);
      }
    }
    if (this.resizeListener) {
      window.removeEventListener('resize', this.resizeListener);
    }
  }

  private setupScrollListener() {
    this.scrollListener = () => this.checkAndEmit();

    if (this.scrollTarget === window) {
      window.addEventListener('scroll', this.scrollListener, { passive: true });
      // Also respond to resizes which can change available space
      this.resizeListener = () => this.checkAndEmit();
      window.addEventListener('resize', this.resizeListener, { passive: true });
    } else {
      (this.scrollTarget as HTMLElement).addEventListener('scroll', this.scrollListener, { passive: true } as any);
    }
  }

  private checkAndEmit() {
    if (this.disabled) return;

    if (this.scrollTarget === window) {
      const windowHeight = window.innerHeight;
      const documentHeight = document.documentElement.scrollHeight;
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      if (documentHeight - (scrollTop + windowHeight) < this.threshold) {
        this.scrolled.emit();
      }
    } else {
      const el = this.scrollTarget as HTMLElement;
      const { scrollTop, scrollHeight, clientHeight } = el;
      if (scrollHeight - (scrollTop + clientHeight) < this.threshold) {
        this.scrolled.emit();
      }
    }
  }

  // Walk up DOM to find nearest scrollable container
  private findScrollContainer(startEl: HTMLElement): HTMLElement | null {
    let el: HTMLElement | null = startEl.parentElement;
    while (el && el !== document.body && el !== document.documentElement) {
      const style = getComputedStyle(el);
      const overflowY = style.overflowY;
      if (overflowY === 'auto' || overflowY === 'scroll') {
        return el;
      }
      el = el.parentElement;
    }
    return null;
  }
}