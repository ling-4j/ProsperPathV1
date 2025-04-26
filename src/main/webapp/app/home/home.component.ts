import { Component, OnDestroy, OnInit, inject, signal, ViewChild } from '@angular/core';
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
export default class HomeComponent implements OnInit, OnDestroy {
  @ViewChild('donutChart') donutChart: BaseChartDirective | undefined;

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

  // FullCalendar configuration
  currentMonthYear: string = '';
  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, listPlugin],
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek',
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
        data: [1000, 900, 1200, 1100, 1500, 1700, 2000, 2100, 2300, 2400, 2500, 2523],
        label: 'Progress Rate',
        fill: false,
        borderColor: '#1e90ff',
        tension: 0.4,
      },
    ],
    labels: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
  };
  public progressRateChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: { legend: { display: false } },
  };
  public progressRateChartType: ChartType = 'line';

  public incomeVsExpenseDonutChartData: ChartData<'doughnut'> = {
    datasets: [
      {
        data: [1000000, 1000000],
        backgroundColor: ['#00c4b4', '#ff6f61'],
      },
    ],
    labels: ['Income', 'Expenses'],
  };
  public incomeVsExpenseDonutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: { legend: { position: 'top' } },
  };
  public incomeVsExpenseDonutChartType: ChartType = 'doughnut';

  public incomeVsExpenseLineChartData: ChartData<'line'> = {
    datasets: [
      {
        data: [1200, 1500, 1100, 1300, 1600, 1400, 1700],
        label: 'Income',
        borderColor: '#00c4b4',
        fill: false,
        tension: 0.4,
      },
      {
        data: [1000, 1200, 1300, 1100, 1400, 1200, 1300],
        label: 'Expenses',
        borderColor: '#ff6f61',
        fill: false,
        tension: 0.4,
      },
    ],
    labels: ['Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep'],
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

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => {
        this.account.set(account);
        if (account) {
          this.loadSummary();
          this.loadCalendarEvents();
          this.updateChartData();
        }
      });

    const currentDate = new Date();
    this.currentMonthYear = currentDate.toLocaleString('default', { month: 'long', year: 'numeric' });
  }

  onPeriodChange(): void {
    if (this.account()) {
      this.loadSummary();
      this.updateChartData();
    }
  }

  private updateChartData(): void {
    const income = this.totalIncome() || 1000000;
    const expense = this.totalExpense() || 1000000;
    this.incomeVsExpenseDonutChartData.datasets[0].data = [income, expense];
    this.donutChart?.update();
  }

  private loadSummary(): void {
    const period = this.selectedPeriod();
    forkJoin({
      summary: this.summaryService.getSummary(period),
      financialChange: this.summaryService.getFinancialChange(period),
    }).subscribe({
      next: ({ summary, financialChange }) => {
        this.errorMessage.set(null);

        this.totalAssets.set(summary.totalAssets ?? 0);
        this.totalIncome.set(summary.totalIncome ?? 0);
        this.totalExpense.set(summary.totalExpense ?? 0);
        this.totalProfit.set(summary.totalProfit ?? 0);
        this.profitPercentage.set(summary.profitPercentage ?? 0);

        this.assetsChangePercentage.set(financialChange.assetsChangePercentage ?? 0);
        this.incomeChangePercentage.set(financialChange.incomeChangePercentage ?? 0);
        this.expenseChangePercentage.set(financialChange.expenseChangePercentage ?? 0);
        this.profitChangePercentage.set(financialChange.profitChangePercentage ?? 0);
      },
      error: error => {
        this.totalAssets.set(0);
        this.totalIncome.set(0);
        this.totalExpense.set(0);
        this.totalProfit.set(0);
        this.profitPercentage.set(0);
        this.assetsChangePercentage.set(0);
        this.incomeChangePercentage.set(0);
        this.expenseChangePercentage.set(0);
        this.profitChangePercentage.set(0);

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

  private loadCalendarEvents(): void {
    this.calendarOptions.events = [
      { title: 'Payment Due', date: '2025-04-26' },
      { title: 'Budget Review', date: '2025-04-28' },
    ];
  }

  handleEventClick(info: EventClickArg): void {
    alert(`Event: ${info.event.title}\nDate: ${info.event.start?.toISOString().split('T')[0]}`);
  }

  handleDateClick(info: DateSelectArg): void {
    const title = prompt('Enter event title:');
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