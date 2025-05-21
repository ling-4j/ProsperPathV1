import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { SummaryService, DetailedFinancialData } from 'app/entities/summary/service/summary.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FullCalendarModule } from '@fullcalendar/angular';
import { NgApexchartsModule } from 'ng-apexcharts';

import CarouselComponent from './caroucel/carousel.component';
import GoldProfitCalculatorComponent from '../entities/gold-cal/gold/gold-profit-calculator.component';

import {
  ApexChart,
  ApexAxisChartSeries,
  ApexDataLabels,
  ApexFill,
  ApexLegend,
  ApexNonAxisChartSeries,
  ApexPlotOptions,
  ApexResponsive,
  ApexStroke,
  ApexTooltip,
  ApexXAxis,
} from 'ng-apexcharts';

export type ChartOptions = {
  series: ApexAxisChartSeries | ApexNonAxisChartSeries;
  chart: ApexChart;
  xaxis?: ApexXAxis;
  dataLabels: ApexDataLabels;
  stroke?: ApexStroke;
  fill?: ApexFill;
  legend?: ApexLegend;
  plotOptions?: ApexPlotOptions;
  tooltip?: ApexTooltip;
  labels?: string[];
  responsive?: ApexResponsive[];
  colors?: string[];
};

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [
    RouterModule,
    SharedModule,
    CommonModule,
    FormsModule,
    FullCalendarModule,
    NgApexchartsModule,
    CarouselComponent,
    GoldProfitCalculatorComponent,
  ],
  standalone: true,
})
export default class HomeComponent implements OnInit, OnDestroy {
  account = signal<Account | null>(null);
  isLoading = signal(false);
  showWarning = false;

  selectedPeriod = signal<'week' | 'month' | 'year'>('month');
  totalAssets = signal(0);
  totalIncome = signal(0);
  totalExpense = signal(0);
  profitPercentage = signal(0);
  assetsChangePercentage = signal(0);
  incomeChangePercentage = signal(0);
  expenseChangePercentage = signal(0);
  profitChangePercentage = signal(0);
  errorMessage = signal<string | null>(null);
  isMobile = signal(window.innerWidth < 768);

  progressRateChartOptions: Partial<ChartOptions> = {
    series: [{ name: 'Lợi nhuận', data: [] }],
    chart: {
      type: 'line',
      height: 280,
      animations: { enabled: true, speed: 800 },
      toolbar: {
        show: true,
        tools: {
          download: true,
          selection: false,
          zoom: false,
          zoomin: false,
          zoomout: false,
          pan: false,
          reset: false,
        },
      },
    },
    stroke: { curve: 'smooth', width: 2 },
    xaxis: { categories: [], labels: { style: { colors: '#6b7280', fontSize: '12px' } } },
    dataLabels: { enabled: false },
    colors: ['#1e90ff'],
    tooltip: { y: { formatter: HomeComponent.formatCurrencyShort } },
  };
  incomeVsExpenseDonutChartOptions: Partial<ChartOptions> = {
    series: [0, 0],
    chart: { type: 'donut', height: 240 },
    labels: ['Thu nhập', 'Chi tiêu'],
    colors: ['#00c4b4', '#ff6f61'],
    legend: { position: 'top', fontSize: '12px', labels: { colors: '#6b7280' } },
    dataLabels: {
      enabled: true,
      formatter(val: unknown): string {
        if (typeof val === 'number') {
          return `${val.toFixed(1)}%`;
        }
        return typeof val === 'string' ? `${val}%` : '';
      },
      style: { fontSize: '12px', colors: ['#fff'] },
    },
    plotOptions: {
      pie: {
        donut: {
          labels: {
            show: true,
            total: {
              show: true,
              label: 'Tổng',
              formatter(w: { globals: { seriesTotals: number[] } }) {
                const total = w.globals.seriesTotals.reduce((a: number, b: number) => a + b, 0);
                return HomeComponent.formatCurrencyShort(total);
              },
            },
          },
        },
      },
    },
    responsive: [{ breakpoint: 767, options: { chart: { height: 200 } } }],
  };
  incomeVsExpenseLineChartOptions: Partial<ChartOptions> = {
    series: [
      { name: 'Thu nhập', data: [] },
      { name: 'Chi tiêu', data: [] },
    ],
    chart: {
      type: 'line',
      height: 280,
      animations: { enabled: true, speed: 800 },
      toolbar: {
        show: true,
        tools: {
          download: true,
          selection: false,
          zoom: false,
          zoomin: false,
          zoomout: false,
          pan: false,
          reset: false,
        },
      },
    },
    stroke: { curve: 'smooth', width: 2 },
    xaxis: { categories: [], labels: { style: { colors: '#6b7280', fontSize: '12px' } } },
    dataLabels: { enabled: false },
    colors: ['#00c4b4', '#ff6f61'],
    legend: { position: 'top', fontSize: '12px', labels: { colors: '#6b7280' } },
    tooltip: { y: { formatter: HomeComponent.formatCurrencyShort } },
  };

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
        if (account) this.loadAllData();
      });
  }

  onPeriodChange(): void {
    if (this.account()) this.loadAllData();
  }

  private loadAllData(): void {
    this.isLoading.set(true);
    const period = this.selectedPeriod();
    forkJoin({
      summary: this.summaryService.getSummary(period),
      financialChange: this.summaryService.getFinancialChange(period),
      detailedData: this.summaryService.getDetailedFinancialData(period),
    }).subscribe({
      next: ({ summary, financialChange, detailedData }) => {
        this.errorMessage.set(null);
        this.updateSummaryData(summary);
        this.updateChangePercentages(financialChange);
        this.updateChartData(detailedData);
        this.isLoading.set(false);
      },
      error: error => this.handleError(error, period),
    });
  }

  private updateSummaryData(summary: any): void {
    if (!summary) {
      this.resetData();
      return;
    }
    this.totalAssets.set(summary.totalAssets ?? 0);
    this.totalIncome.set(summary.totalIncome ?? 0);
    this.totalExpense.set(summary.totalExpense ?? 0);
    this.profitPercentage.set(summary.profitPercentage ?? 0);
  }

  private updateChangePercentages(financialChange: any): void {
    this.assetsChangePercentage.set(financialChange.assetsChangePercentage);
    this.incomeChangePercentage.set(financialChange.incomeChangePercentage);
    this.expenseChangePercentage.set(financialChange.expenseChangePercentage);
    this.profitChangePercentage.set(financialChange.profitChangePercentage);
  }

  private updateChartData(detailedData: DetailedFinancialData): void {
    this.progressRateChartOptions.series = [{ name: 'Lợi nhuận', data: detailedData.progressRateData }];
    this.progressRateChartOptions.xaxis = { categories: detailedData.labels };
    this.incomeVsExpenseDonutChartOptions.series = [this.totalIncome() || 0, this.totalExpense() || 0];
    this.incomeVsExpenseLineChartOptions.series = [
      { name: 'Thu nhập', data: detailedData.incomeData },
      { name: 'Chi tiêu', data: detailedData.expenseData },
    ];
    this.incomeVsExpenseLineChartOptions.xaxis = { categories: detailedData.labels };
  }

  private handleError(error: any, period: string): void {
    this.resetData();
    this.isLoading.set(false);
    if (['Summary not found', 'Detailed data not found'].includes(error.message)) {
      this.errorMessage.set(`Không có dữ liệu cho kỳ: ${period}`);
    } else if (error.message === 'Unauthorized') {
      this.errorMessage.set('Bạn không có quyền truy cập dữ liệu này. Vui lòng đăng nhập lại.');
      this.router.navigate(['/login']);
    } else {
      this.errorMessage.set('Đã xảy ra lỗi khi tải dữ liệu.');
    }
  }

  private resetData(): void {
    this.totalAssets.set(0);
    this.totalIncome.set(0);
    this.totalExpense.set(0);
    this.profitPercentage.set(0);
    this.assetsChangePercentage.set(0);
    this.incomeChangePercentage.set(0);
    this.expenseChangePercentage.set(0);
    this.profitChangePercentage.set(0);
    this.progressRateChartOptions.series = [{ name: 'Lợi nhuận', data: [] }];
    this.progressRateChartOptions.xaxis = { categories: [] };
    this.incomeVsExpenseDonutChartOptions.series = [0, 0];
    this.incomeVsExpenseLineChartOptions.series = [
      { name: 'Thu nhập', data: [] },
      { name: 'Chi tiêu', data: [] },
    ];
    this.incomeVsExpenseLineChartOptions.xaxis = { categories: [] };
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  private static formatCurrencyShort(value: number): string {
    const absValue = Math.abs(value);
    if (absValue >= 1_000_000_000) {
      return (value / 1_000_000_000).toFixed(2).replace(/\.00$/, '') + ' tỷ';
    } else if (absValue >= 1_000_000) {
      return (value / 1_000_000).toFixed(2).replace(/\.00$/, '') + ' triệu';
    } else {
      return value.toLocaleString('vi-VN') + 'VND';
    }
  }

  formatCurrencyShort(value: number): string {
    return HomeComponent.formatCurrencyShort(value);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
