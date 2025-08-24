import { Component, OnInit, AfterViewInit, ViewChild, signal, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { AdminService, BookAnomalyDTO } from '../services/admin.service';

@Component({
  selector: 'app-book-anomalies',
  standalone: true,
  imports: [CommonModule, RouterModule, MatButtonModule, MatProgressSpinnerModule, MatTableModule, MatSortModule, MatCardModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  template: `
    <div class="motspassants-library">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <iconify-icon class="title-icon" icon="material-symbols:rule"></iconify-icon>
            Anomalies
            <button mat-icon-button class="refresh-btn" aria-label="Refresh anomalies" (click)="load()">
              <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
            </button>
          </h1>
          <p class="library-subtitle">Detected inconsistencies in data and storage</p>
        </div>
      </div>

      @if (loading()) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-spinner diameter="60" color="accent"></mat-spinner>
            <h3>Scanning anomalies...</h3>
            <p>Reviewing your library for potential issues</p>
          </div>
        </div>
      } @else {
        <div class="library-content">
          @if (anomalies().length === 0) {
            <div class="empty-library">
              <div class="empty-content">
                <iconify-icon class="empty-iconify" icon="material-symbols:rule"></iconify-icon>
                <h2>No anomalies found</h2>
                <p>Your library looks healthy. You can rescan to ensure everything stays consistent.</p>
                <button mat-raised-button color="accent" (click)="load()" class="cta-button">
                  <iconify-icon icon="material-symbols:refresh-rounded"></iconify-icon>
                  Rescan
                </button>
              </div>
            </div>
          } @else {
            <mat-card class="dark-card anomalies-card">
              <mat-card-content>
                <div class="table-container">
                  <table mat-table [dataSource]="dataSource" matSort class="mat-elevation-z1 anomalies-table">
                <!-- Type Column -->
                <ng-container matColumnDef="type">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header> Type </th>
                  <td mat-cell *matCellDef="let row"> {{ row.type }} </td>
                </ng-container>

        <!-- Message Column -->
        <ng-container matColumnDef="message">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header> Message </th>
                  <td mat-cell *matCellDef="let row">
          <div class="cell-ellipsis cell-primary">{{ row.message }}</div>
                  </td>
                </ng-container>

                <!-- Detail Column -->
        <ng-container matColumnDef="detail">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header> Detail </th>
                  <td mat-cell *matCellDef="let row">
          <div class="cell-ellipsis cell-muted">{{ row.detail }}</div>
                  </td>
                </ng-container>

                <!-- Actions Column -->
                <ng-container matColumnDef="actions">
                  <th mat-header-cell *matHeaderCellDef> </th>
                  <td mat-cell *matCellDef="let row" class="actions-cell">
                    <button mat-icon-button [routerLink]="['/books', row.bookId]" aria-label="Open book">
                      <iconify-icon icon="material-symbols:menu-book-rounded"></iconify-icon>
                    </button>
                  </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
                  </table>
                </div>
              </mat-card-content>
            </mat-card>
          }
        </div>
      }
    </div>
  `,
  styleUrls: ['./book-anomalies.component.css']
})
export class BookAnomaliesComponent implements OnInit, AfterViewInit {
  anomalies = signal<BookAnomalyDTO[]>([]);
  loading = signal<boolean>(false);
  displayedColumns: string[] = ['type', 'message', 'detail', 'actions'];
  dataSource = new MatTableDataSource<BookAnomalyDTO>([]);

  @ViewChild(MatSort) sort!: MatSort;

  constructor(private admin: AdminService) {}

  ngOnInit(): void {
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }

  load() {
    this.loading.set(true);
    this.admin.getBookAnomalies().subscribe({
      next: (items: BookAnomalyDTO[]) => {
        const data = items ?? [];
        this.anomalies.set(data);
        this.dataSource.data = data;
        this.loading.set(false);
      },
      error: () => {
        this.anomalies.set([]);
        this.dataSource.data = [];
        this.loading.set(false);
      }
    });
  }
}
