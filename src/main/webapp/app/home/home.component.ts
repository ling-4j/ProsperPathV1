// src/app/home/home.component.ts
import { Component, OnDestroy, OnInit, inject, signal, ViewChild, ChangeDetectorRef, Renderer2, AfterViewChecked, AfterViewInit, ElementRef } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { forkJoin } from 'rxjs';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { SummaryService, DetailedFinancialData } from 'app/entities/summary/service/summary.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// Import FullCalendar
import { CalendarOptions, DateSelectArg, EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import { FullCalendarModule } from '@fullcalendar/angular';

// Import Chart.js and ng2-charts
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [
    SharedModule,
    RouterModule,
    CommonModule,
    FormsModule,
    FullCalendarModule,
    NgChartsModule,
  ],
  standalone: true,
})
export default class HomeComponent implements OnInit, OnDestroy, AfterViewChecked, AfterViewInit {
  @ViewChild('donutChart') donutChart: BaseChartDirective | undefined;
  @ViewChild('lineChart') lineChart: BaseChartDirective | undefined;
  @ViewChild('progressRateChart') progressRateChart: BaseChartDirective | undefined;
  @ViewChild('dashboardContainer', { static: true }) dashboardContainer!: ElementRef;

  account = signal<Account | null>(null);
  isLoading = signal<boolean>(false);

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

  // FullCalendar configuration
  currentMonthYear: string = '';
  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, listPlugin],
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev',
      center: 'title',
      right: 'next today',
    },
    events: [],
    editable: true,
    selectable: true,
    eventClick: this.handleEventClick.bind(this),
    select: this.handleDateClick.bind(this),
  };

  // Chart configurations
  public progressRateChartData: ChartData<'line'> = {
    datasets: [
      {
        data: [],
        label: 'Lợi nhuận',
        fill: false,
        borderColor: '#1e90ff',
        tension: 0.4,
      },
    ],
    labels: [],
  };
  public progressRateChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: { legend: { display: false } },
  };
  public progressRateChartType: ChartType = 'line';

  public incomeVsExpenseDonutChartData: ChartData<'doughnut'> = {
    datasets: [
      {
        data: [0, 0],
        backgroundColor: ['#00c4b4', '#ff6f61'],
      },
    ],
    labels: ['Thu nhập', 'Chi phí'],
  };
  public incomeVsExpenseDonutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: { legend: { position: 'top' } },
  };
  public incomeVsExpenseDonutChartType: ChartType = 'doughnut';

  public incomeVsExpenseLineChartData: ChartData<'line'> = {
    datasets: [
      {
        data: [],
        label: 'Thu nhập',
        borderColor: '#00c4b4',
        fill: false,
        tension: 0.4,
      },
      {
        data: [],
        label: 'Chi phí',
        borderColor: '#ff6f61',
        fill: false,
        tension: 0.4,
      },
    ],
    labels: [],
  };
  public incomeVsExpenseLineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: { legend: { position: 'top' } },
  };
  public incomeVsExpenseLineChartType: ChartType = 'line';

  private readonly destroy$ = new Subject<void>();
  private readonly accountService = inject(AccountService);
  private readonly summaryService = inject(SummaryService);
  private readonly router = inject(Router);

  private chartsRendered = false;

  constructor(private cdr: ChangeDetectorRef, private renderer: Renderer2) {}

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => {
        this.account.set(account);
        if (account) {
          this.loadAllData();
          this.loadCalendarEvents();
        }
      });

    const currentDate = new Date();
    this.currentMonthYear = currentDate.toLocaleString('vi-VN', { month: 'long', year: 'numeric' });
  }

  ngAfterViewInit(): void {
    // Ensure charts are rendered correctly after the view is fully initialized
    setTimeout(() => {
      this.updateAllCharts();
      window.dispatchEvent(new Event('resize'));
    }, 1000);

    // Listen for layout changes and trigger chart updates
    const observer = new MutationObserver(() => {
      this.updateAllCharts();
      window.dispatchEvent(new Event('resize'));
    });

    observer.observe(document.body, { attributes: true, childList: true, subtree: true });

    // Force chart updates after a delay to ensure proper rendering
    setTimeout(() => {
      this.updateAllCharts();
    }, 2000);

    // Additional manual resize trigger for stubborn rendering issues
    window.addEventListener('load', () => {
      setTimeout(() => {
        window.dispatchEvent(new Event('resize'));
        this.updateAllCharts();
      }, 500);
    });
  }

  private updateAllCharts(): void {
    this.progressRateChart?.update();
    this.donutChart?.update();
    this.lineChart?.update();
  }

  ngAfterViewChecked(): void {
    if (!this.chartsRendered) {
      setTimeout(() => {
        window.dispatchEvent(new Event('resize'));
        this.chartsRendered = true;
      }, 1);
    }
  }

  onPeriodChange(): void {
    if (this.account()) {
      this.loadAllData();
    }
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

        // Cập nhật dữ liệu tổng quan
        this.totalAssets.set(summary.totalAssets ?? 0);
        this.totalIncome.set(summary.totalIncome ?? 0);
        this.totalExpense.set(summary.totalExpense ?? 0);
        this.totalProfit.set(summary.totalProfit ?? 0);
        this.profitPercentage.set(summary.profitPercentage ?? 0);

        // Cập nhật phần trăm thay đổi
        this.assetsChangePercentage.set(financialChange.assetsChangePercentage ?? 0);
        this.incomeChangePercentage.set(financialChange.incomeChangePercentage ?? 0);
        this.expenseChangePercentage.set(financialChange.expenseChangePercentage ?? 0);
        this.profitChangePercentage.set(financialChange.profitChangePercentage ?? 0);

        // Cập nhật dữ liệu biểu đồ
        this.updateChartData(detailedData);
        this.isLoading.set(false);
      },
      error: error => {
        this.resetData();
        this.isLoading.set(false);
        if (error.message === 'Summary not found' || error.message === 'Detailed data not found') {
          this.errorMessage.set(`Không có dữ liệu cho kỳ: ${period}`);
        } else if (error.message === 'Unauthorized') {
          this.errorMessage.set('Bạn không có quyền truy cập dữ liệu này. Vui lòng đăng nhập lại.');
          this.router.navigate(['/login']);
        } else {
          this.errorMessage.set('Đã xảy ra lỗi khi tải dữ liệu.');
        }
      },
    });
  }

  private updateChartData(detailedData: DetailedFinancialData): void {
    // Cập nhật biểu đồ Progress Rate (Lợi nhuận)
    this.progressRateChartData.datasets[0].data = detailedData.progressRateData || [];
    this.progressRateChartData.labels = detailedData.labels || [];
    this.progressRateChart?.update();

    // Cập nhật biểu đồ Donut (Thu nhập vs Chi phí)
    this.incomeVsExpenseDonutChartData.datasets[0].data = [this.totalIncome() || 0, this.totalExpense() || 0];
    this.donutChart?.update();

    // Cập nhật biểu đồ Line (Thu nhập vs Chi phí theo thời gian)
    this.incomeVsExpenseLineChartData.datasets[0].data = detailedData.incomeData || [];
    this.incomeVsExpenseLineChartData.datasets[1].data = detailedData.expenseData || [];
    this.incomeVsExpenseLineChartData.labels = detailedData.labels || [];
    this.lineChart?.update();
  }

  private resetData(): void {
    this.totalAssets.set(0);
    this.totalIncome.set(0);
    this.totalExpense.set(0);
    this.totalProfit.set(0);
    this.profitPercentage.set(0);
    this.assetsChangePercentage.set(0);
    this.incomeChangePercentage.set(0);
    this.expenseChangePercentage.set(0);
    this.profitChangePercentage.set(0);

    // Reset biểu đồ
    this.progressRateChartData.datasets[0].data = [];
    this.progressRateChartData.labels = [];
    this.incomeVsExpenseDonutChartData.datasets[0].data = [0, 0];
    this.incomeVsExpenseLineChartData.datasets[0].data = [];
    this.incomeVsExpenseLineChartData.datasets[1].data = [];
    this.incomeVsExpenseLineChartData.labels = [];
    this.progressRateChart?.update();
    this.donutChart?.update();
    this.lineChart?.update();
  }

  private loadCalendarEvents(): void {
    this.calendarOptions.events = [
      { title: 'Thanh toán hóa đơn', date: '2025-04-26' },
      { title: 'Xem xét ngân sách', date: '2025-04-28' },
    ];
  }

  handleEventClick(info: EventClickArg): void {
    alert(`Sự kiện: ${info.event.title}\nNgày: ${info.event.start?.toISOString().split('T')[0]}`);
  }

  handleDateClick(info: DateSelectArg): void {
    const title = prompt('Nhập tiêu đề sự kiện:');
    if (title) {
      const calendarApi = info.view.calendar;
      calendarApi.addEvent({
        title,
        start: info.startStr,
        allDay: info.allDay,
      });
    }
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}