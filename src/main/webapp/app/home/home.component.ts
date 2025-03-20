import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { SummaryService } from 'app/entities/summary/service/summary.service'; // Giả định service này tồn tại
import { NgModule } from '@angular/core';
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

  // Signal cho khoảng thời gian và dữ liệu tài chính
  selectedPeriod = signal<'week' | 'month' | 'year'>('month');
  totalAssets = signal<number>(0);
  totalIncome = signal<number>(0);
  totalExpense = signal<number>(0);
  totalProfit = signal<number>(0);
  profitPercentage = signal<number>(0);

  private readonly destroy$ = new Subject<void>();
  private readonly accountService = inject(AccountService);
  private readonly summaryService = inject(SummaryService); // Service để lấy dữ liệu
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
    this.summaryService.getSummary(period).subscribe(summary => {
      this.totalAssets.set(summary.totalAssets ?? 0);
      this.totalIncome.set(summary.totalIncome ?? 0);
      this.totalExpense.set(summary.totalExpense ?? 0);
      this.totalProfit.set(summary.totalProfit ?? 100);
      this.profitPercentage.set(summary.profitPercentage ?? 0);
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
