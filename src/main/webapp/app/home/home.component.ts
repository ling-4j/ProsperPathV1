import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
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

// Import ApexCharts
import { NgApexchartsModule } from 'ng-apexcharts';
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

// Định nghĩa type cho biểu đồ ApexCharts
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
    SharedModule,
    RouterModule,
    CommonModule,
    FormsModule,
    FullCalendarModule,
    NgApexchartsModule, // Thêm NgApexchartsModule
  ],
  standalone: true,
})
export default class HomeComponent implements OnInit, OnDestroy {
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
  // ApexCharts configurations
  public progressRateChartOptions: Partial<ChartOptions> = {
    series: [
      {
        name: 'Lợi nhuận',
        data: [],
      },
    ],
    chart: {
      type: 'line',
      height: 280,
      animations: {
        enabled: true,
        speed: 800,
      },
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
    stroke: {
      curve: 'smooth',
      width: 2,
    },
    xaxis: {
      categories: [],
      labels: {
        style: {
          colors: '#6b7280',
          fontSize: '12px',
        },
      },
    },
    dataLabels: {
      enabled: false,
    },
    colors: ['#1e90ff'],
    tooltip: {
      y: {
        formatter: (val: number) => `${val.toLocaleString('vi-VN')} VND`, // Định dạng số tiền
      },
    },
  };

  public incomeVsExpenseDonutChartOptions: Partial<ChartOptions> = {
    series: [0, 0],
    chart: {
      type: 'donut',
      height: 240,
    },
    labels: ['Thu nhập', 'Chi phí'],
    colors: ['#00c4b4', '#ff6f61'],
    legend: {
      position: 'top',
      fontSize: '12px',
      labels: {
        colors: '#6b7280',
      },
    },
    dataLabels: {
      enabled: true,
      formatter: (val: number) => `${val.toFixed(1)}%`,
      style: {
        fontSize: '12px',
        colors: ['#fff'],
      },
    },
    plotOptions: {
      pie: {
        donut: {
          labels: {
            show: true,
            total: {
              show: true,
              label: 'Tổng',
              formatter: (w: any) => {
                const total = w.globals.seriesTotals.reduce((a: number, b: number) => a + b, 0);
                return `${total.toLocaleString('vi-VN')} VND`; // Định dạng số tiền
              },
            },
          },
        },
      },
    },
    responsive: [
      {
        breakpoint: 767,
        options: {
          chart: {
            height: 200,
          },
        },
      },
    ],
  };

  public incomeVsExpenseLineChartOptions: Partial<ChartOptions> = {
    series: [
      {
        name: 'Thu nhập',
        data: [],
      },
      {
        name: 'Chi phí',
        data: [],
      },
    ],
    chart: {
      type: 'line',
      height: 280,
      animations: {
        enabled: true,
        speed: 800,
      },
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
    stroke: {
      curve: 'smooth',
      width: 2,
    },
    xaxis: {
      categories: [],
      labels: {
        style: {
          colors: '#6b7280',
          fontSize: '12px',
        },
      },
    },
    dataLabels: {
      enabled: false,
    },
    colors: ['#00c4b4', '#ff6f61'],
    legend: {
      position: 'top',
      fontSize: '12px',
      labels: {
        colors: '#6b7280',
      },
    },
    tooltip: {
      y: {
        formatter: (val: number) => `${val.toLocaleString('vi-VN')} VND`, // Định dạng số tiền
      },
    },
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
        if (account) {
          this.loadAllData();
          this.loadCalendarEvents();
        }
      });

    const currentDate = new Date();
    this.currentMonthYear = currentDate.toLocaleString('vi-VN', { month: 'long', year: 'numeric' });
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
    this.progressRateChartOptions.series = [
      {
        name: 'Lợi nhuận',
        data: detailedData.progressRateData || [],
      },
    ];
    this.progressRateChartOptions.xaxis = {
      categories: detailedData.labels || [],
      labels: {
        style: {
          colors: '#6b7280',
          fontSize: '12px',
        },
      },
    };

    // Cập nhật biểu đồ Donut (Thu nhập vs Chi phí)
    this.incomeVsExpenseDonutChartOptions.series = [this.totalIncome() || 0, this.totalExpense() || 0];

    // Cập nhật biểu đồ Line (Thu nhập vs Chi phí theo thời gian)
    this.incomeVsExpenseLineChartOptions.series = [
      {
        name: 'Thu nhập',
        data: detailedData.incomeData || [],
      },
      {
        name: 'Chi phí',
        data: detailedData.expenseData || [],
      },
    ];
    this.incomeVsExpenseLineChartOptions.xaxis = {
      categories: detailedData.labels || [],
      labels: {
        style: {
          colors: '#6b7280',
          fontSize: '12px',
        },
      },
    };
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
    this.progressRateChartOptions.series = [{ name: 'Lợi nhuận', data: [] }];
    this.progressRateChartOptions.xaxis = { categories: [] };
    this.incomeVsExpenseDonutChartOptions.series = [0, 0];
    this.incomeVsExpenseLineChartOptions.series = [
      { name: 'Thu nhập', data: [] },
      { name: 'Chi phí', data: [] },
    ];
    this.incomeVsExpenseLineChartOptions.xaxis = { categories: [] };
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