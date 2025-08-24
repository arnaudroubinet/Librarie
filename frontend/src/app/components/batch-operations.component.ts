import { Component, OnInit, signal, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BatchService } from '../services/batch.service';
import { BatchOperation } from '../models/batch.model';

@Component({
  selector: 'app-batch-operations',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="motspassants-library">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <iconify-icon class="title-icon" icon="material-symbols:batch-prediction"></iconify-icon>
            Batch Operations
            <button mat-icon-button class="refresh-btn" aria-label="Refresh operations" (click)="loadRecentOperations()">
              <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
            </button>
          </h1>
          <p class="library-subtitle">Manage bulk operations across your library</p>
        </div>
      </div>

      <div class="library-content">
        <div class="batch-operations-container">
          <mat-card class="dark-card">
            <mat-card-content>
              <div class="operations-section">
                <h3>Recent Operations</h3>
                
                @if (recentOperations().length === 0) {
                  <p>No recent batch operations found.</p>
                } @else {
                  @for (operation of recentOperations(); track operation.id || i; let i = $index) {
                    <mat-card class="operation-card dark-card">
                      <mat-card-content>
                        <div class="operation-header">
                          <div class="operation-info">
                            <h4>{{ operation.type }} Operation</h4>
                            <p>{{ operation.totalBooks }} books</p>
                          </div>
                          <div class="operation-status" [class]="batchService.getStatusCssClass(operation.status)">
                            {{ batchService.getStatusDisplayText(operation.status) }}
                          </div>
                        </div>
                        
                        <div class="operation-progress">
                          <p>Progress: {{ batchService.getOperationProgress(operation) }}%</p>
                          <p>Success Rate: {{ batchService.getOperationSuccessRate(operation) }}%</p>
                          <p>Duration: {{ batchService.getOperationDuration(operation) }}</p>
                        </div>
                      </mat-card-content>
                    </mat-card>
                  }
                }
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./batch-operations.component.css']
})
export class BatchOperationsComponent implements OnInit {
  recentOperations = signal<BatchOperation[]>([]);

  constructor(
    public batchService: BatchService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadRecentOperations();
  }

  loadRecentOperations() {
    this.batchService.getRecentBatchOperations(10).subscribe({
      next: (operations) => {
        this.recentOperations.set(operations);
      },
      error: (error) => {
        console.error('Failed to load operations:', error);
        this.snackBar.open('Failed to load batch operations', 'Close', { duration: 3000 });
      }
    });
  }
}