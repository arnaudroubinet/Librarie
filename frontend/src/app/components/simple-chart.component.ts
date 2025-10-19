import { AfterViewInit, Component, ElementRef, Input, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';

type ChartType = 'line' | 'donut';

@Component({
  selector: 'app-simple-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <figure class="simple-chart" [attr.aria-label]="ariaLabel" role="img">
      <canvas #canvas></canvas>
      <figcaption class="sr-only">{{ ariaLabel }}</figcaption>
    </figure>
  `,
  styles: [`
    .simple-chart { margin: 0; width: 100%; height: 260px; position: relative; }
    .simple-chart canvas { width: 100%; height: 100%; display: block; }
    .sr-only:not(:focus):not(:active) { clip: rect(0 0 0 0); clip-path: inset(50%); height: 1px; overflow: hidden; position: absolute; white-space: nowrap; width: 1px; }
  `]
})
export class SimpleChartComponent implements AfterViewInit, OnDestroy {
  @Input() type: ChartType = 'line';
  @Input() labels: string[] = [];
  @Input() data: number[] = [];
  @Input() colors: string[] = [];
  @Input() ariaLabel = 'Chart';

  @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;

  private ro?: ResizeObserver;

  ngAfterViewInit(): void {
    this.draw();
    this.ro = new ResizeObserver(() => this.draw());
    this.ro.observe(this.canvasRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.ro?.disconnect();
  }

  private draw(): void {
    const canvas = this.canvasRef.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const dpr = Math.max(1, window.devicePixelRatio || 1);
    canvas.width = Math.floor(rect.width * dpr);
    canvas.height = Math.floor(rect.height * dpr);
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.scale(dpr, dpr);

    // background transparent; grid for line
    if (this.type === 'line') {
      this.drawLine(ctx, rect.width, rect.height);
    } else {
      this.drawDonut(ctx, rect.width, rect.height);
    }
  }

  private drawLine(ctx: CanvasRenderingContext2D, width: number, height: number) {
    const padding = 16;
    const w = width - padding * 2;
    const h = height - padding * 2;
    const values = this.data.length ? this.data : [0];
    const min = Math.min(...values);
    const max = Math.max(...values);
    const range = Math.max(1, max - min);
    const stepX = values.length > 1 ? w / (values.length - 1) : w;

    // grid
    ctx.strokeStyle = 'rgba(255,255,255,0.06)';
    ctx.lineWidth = 1;
    ctx.beginPath();
    for (let i = 0; i <= 4; i++) {
      const y = padding + (h * i) / 4;
      ctx.moveTo(padding, y);
      ctx.lineTo(padding + w, y);
    }
    ctx.stroke();

    // gradient fill
    const c1 = this.colors[0] || '#00d2ff';
    const c2 = this.colors[1] || '#3a7bd5';
    const grad = ctx.createLinearGradient(0, padding, 0, padding + h);
    grad.addColorStop(0, this.alpha(c1, 0.8));
    grad.addColorStop(1, this.alpha(c2, 0.1));

    // area
    ctx.beginPath();
    values.forEach((v, i) => {
      const x = padding + i * stepX;
      const y = padding + h - ((v - min) / range) * h;
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.lineTo(padding + w, padding + h);
    ctx.lineTo(padding, padding + h);
    ctx.closePath();
    ctx.fillStyle = grad;
    ctx.fill();

    // stroke
    ctx.beginPath();
    values.forEach((v, i) => {
      const x = padding + i * stepX;
      const y = padding + h - ((v - min) / range) * h;
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.strokeStyle = c2;
    ctx.lineWidth = 2;
    ctx.stroke();
  }

  private drawDonut(ctx: CanvasRenderingContext2D, width: number, height: number) {
    const cx = width / 2;
    const cy = height / 2;
    const radius = Math.min(width, height) * 0.38;
    const inner = radius * 0.7;
    const total = this.data.reduce((a, b) => a + b, 0) || 1;
    let start = -Math.PI / 2;
    const palette = this.colors.length ? this.colors : ['#00d2ff', '#3a7bd5', '#9fdcff', '#73eaff', '#b5f3ff'];

    this.data.forEach((val, i) => {
      const angle = (val / total) * Math.PI * 2;
      const end = start + angle;
      ctx.beginPath();
      ctx.arc(cx, cy, radius, start, end);
      ctx.arc(cx, cy, inner, end, start, true);
      ctx.closePath();
      ctx.fillStyle = palette[i % palette.length];
      ctx.fill();
      start = end;
    });
  }

  private alpha(hex: string, a: number) {
    // support #rrggbb
    if (/^#([A-Fa-f0-9]{6})$/.test(hex)) {
      const r = parseInt(hex.slice(1, 3), 16);
      const g = parseInt(hex.slice(3, 5), 16);
      const b = parseInt(hex.slice(5, 7), 16);
      return `rgba(${r}, ${g}, ${b}, ${a})`;
    }
    return hex;
  }
}
