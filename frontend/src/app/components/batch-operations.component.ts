import { Component, OnInit, signal } from '@angular/core';
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
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="batch-operations-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>batch_prediction</mat-icon>
            Batch Operations
          </mat-card-title>
          <mat-card-subtitle>
            Manage bulk operations on multiple books
          </mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <div class="operations-section">
            <h3>Recent Operations</h3>
            
            @if (recentOperations().length === 0) {
              <p>No recent batch operations found.</p>
              <button mat-raised-button color="primary" (click)="loadRecentOperations()">
                Load Operations
              </button>
            } @else {
              @for (operation of recentOperations(); track operation.id) {
                <mat-card class="operation-card">
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
              
              <button mat-raised-button (click)="loadRecentOperations()">
                Refresh
              </button>
            }
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .batch-operations-container {
      max-width: 800px;
      margin: 2rem auto;
      padding: 0 1rem;
    }

    .operations-section h3 {
      margin-bottom: 1rem;
      font-size: 1.1rem;
      font-weight: 500;
    }

    .operation-card {
      margin-bottom: 1rem;
    }

    .operation-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }

    .operation-info h4 {
      margin: 0;
      font-size: 1rem;
      font-weight: 500;
    }

    .operation-info p {
      margin: 0.25rem 0 0 0;
      color: #666;
      font-size: 0.875rem;
    }

    .operation-status {
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 500;
      text-transform: uppercase;
    }

    .status-pending {
      background-color: #ff9800;
      color: white;
    }

    .status-running {
      background-color: #2196f3;
      color: white;
    }

    .status-completed {
      background-color: #4caf50;
      color: white;
    }

    .status-failed {
      background-color: #f44336;
      color: white;
    }

    .status-cancelled {
      background-color: #9e9e9e;
      color: white;
    }

    .operation-progress p {
      margin: 0.25rem 0;
      font-size: 0.875rem;
      color: #666;
    }
  `]
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