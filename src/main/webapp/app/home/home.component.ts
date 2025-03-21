import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { forkJoin } from 'rxjs';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { SummaryService } from 'app/entities/summary/service/summary.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [SharedModule, RouterModule, CommonModule, FormsModule],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account = signal<Account | null>(null);

  // Signals for current period data
  selectedPeriod = signal<'week' | 'month' | 'year'>('month');
  totalAssets = signal<number>(0);
  totalIncome = signal<number>(0);
  totalExpense = signal<number>(0);
  totalProfit = signal<number>(0);
  profitPercentage = signal<number>(0);

  // Signals for percentage change compared to previous period
  assetsChangePercentage = signal<number>(0);
  incomeChangePercentage = signal<number>(0);
  expenseChangePercentage = signal<number>(0);
  profitChangePercentage = signal<number>(0);

  // Signal for error message
  errorMessage = signal<string | null>(null);

  private readonly destroy$ = new Subject<void>();
  private readonly accountService = inject(AccountService);
  private readonly summaryService = inject(SummaryService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => {
        this.account.set(account);
        if (account) {
          this.loadSummary(); // Tải dữ liệu dashboard khi đã đăng nhập
        }
      });
  }

  onPeriodChange(): void {
    if (this.account()) {
      this.loadSummary(); // Tải lại dữ liệu khi thay đổi khoảng thời gian
    }
  }

  private loadSummary(): void {
    const period = this.selectedPeriod();
    forkJoin({
      summary: this.summaryService.getSummary(period),
      financialChange: this.summaryService.getFinancialChange(period),
    }).subscribe({
      next: ({ summary, financialChange }) => {
        // Reset error message
        this.errorMessage.set(null);

        // Update signals with summary data
        this.totalAssets.set(summary.totalAssets ?? 0);
        this.totalIncome.set(summary.totalIncome ?? 0);
        this.totalExpense.set(summary.totalExpense ?? 0);
        this.totalProfit.set(summary.totalProfit ?? 0); // Xóa giá trị mặc định 100
        this.profitPercentage.set(summary.profitPercentage ?? 0);

        // Update signals with financial change data
        this.assetsChangePercentage.set(financialChange.assetsChangePercentage ?? 0);
        this.incomeChangePercentage.set(financialChange.incomeChangePercentage ?? 0);
        this.expenseChangePercentage.set(financialChange.expenseChangePercentage ?? 0);
        this.profitChangePercentage.set(financialChange.profitChangePercentage ?? 0);
      },
      error: error => {
        // Reset all signals to 0
        this.totalAssets.set(0);
        this.totalIncome.set(0);
        this.totalExpense.set(0);
        this.totalProfit.set(0);
        this.profitPercentage.set(0);
        this.assetsChangePercentage.set(0);
        this.incomeChangePercentage.set(0);
        this.expenseChangePercentage.set(0);
        this.profitChangePercentage.set(0);

        // Set error message
        if (error.message === 'Summary not found') {
          this.errorMessage.set(`No summary data available for the selected period: ${period}`);
        } else if (error.message === 'Unauthorized') {
          this.errorMessage.set('You are not authorized to access this data. Please log in again.');
          this.router.navigate(['/login']);
        } else {
          this.errorMessage.set('An error occurred while loading the dashboard data.');
        }
      },
    });
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
