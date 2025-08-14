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

  constructor(private element: ElementRef) {}

  ngOnInit() {
    this.setupScrollListener();
  }

  ngOnDestroy() {
    if (this.scrollListener) {
      window.removeEventListener('scroll', this.scrollListener);
    }
  }

  private setupScrollListener() {
    this.scrollListener = () => {
      if (this.disabled) {
        return;
      }

      const windowHeight = window.innerHeight;
      const documentHeight = document.documentElement.scrollHeight;
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

      // Check if we're within threshold pixels from the bottom
      if (documentHeight - (scrollTop + windowHeight) < this.threshold) {
        this.scrolled.emit();
      }
    };

    window.addEventListener('scroll', this.scrollListener, { passive: true });
  }
}