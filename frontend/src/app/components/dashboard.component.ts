import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SimpleChartComponent } from './simple-chart.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [CommonModule, RouterModule, SimpleChartComponent],
  template: `
    <div class="ngx-container">
      <div class="ngx-grid">
        <div class="ngx-card hoverable">
          <div class="ngx-card-header">
            <span class="ngx-h2">Reading Activity</span>
          </div>
          <div class="ngx-card-content">
            <app-simple-chart type="line" [labels]="months" [data]="areaValues" [colors]="colors" ariaLabel="Reading activity over months"></app-simple-chart>
          </div>
        </div>

        <div class="ngx-card hoverable">
          <div class="ngx-card-header">
            <span class="ngx-h2">Books by Genre</span>
          </div>
          <div class="ngx-card-content">
            <app-simple-chart type="donut" [labels]="donutLabels" [data]="donutValues" [colors]="colors" ariaLabel="Books by genre"></app-simple-chart>
          </div>
        </div>

        <div class="ngx-card hoverable">
          <div class="ngx-card-header">
            <span class="ngx-h2">KPI Snapshot</span>
          </div>
          <div class="ngx-card-content" style="display:grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px;">
            <div class="ngx-kpi">
              <iconify-icon class="kpi-icon" icon="mdi:book-open-page-variant"></iconify-icon>
              <div>
                <div class="kpi-value">1,248</div>
                <div class="kpi-label">Total Books</div>
              </div>
            </div>
            <div class="ngx-kpi">
              <iconify-icon class="kpi-icon" icon="mdi:timer-sand"></iconify-icon>
              <div>
                <div class="kpi-value">82h</div>
                <div class="kpi-label">Reading Time (mo)</div>
              </div>
            </div>
            <div class="ngx-kpi">
              <iconify-icon class="kpi-icon" icon="mdi:star"></iconify-icon>
              <div>
                <div class="kpi-value">4.3</div>
                <div class="kpi-label">Avg. Rating</div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  months: string[] = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  colors: string[] = ['#00d2ff', '#3a7bd5'];
  areaValues: number[] = [10,14,18,23,19,25,28,26,22,24,27,31];
  donutLabels: string[] = ['Sci‑Fi','Fantasy','Mystery','Non‑Fiction','Other'];
  donutValues: number[] = [44,33,21,18,12];

  ngOnInit(): void {
    const cs = getComputedStyle(document.documentElement);
    const c1 = cs.getPropertyValue('--accent-grad-start')?.trim() || this.colors[0];
    const c2 = cs.getPropertyValue('--accent-grad-end')?.trim() || this.colors[1];
    this.colors = [c1, c2];
  }
}
