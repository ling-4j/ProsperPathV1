import { Component, NgZone, OnInit, inject, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal, NgbDateStruct, NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';
import { Injectable } from '@angular/core';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { FilterComponent, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { ITransaction } from '../transaction.model';
import { TruncatePipe } from 'app/shared/truncate/truncate.pipe';
import { EntityArrayResponseType, TransactionService } from '../service/transaction.service';
import { TransactionDeleteDialogComponent } from '../delete/transaction-delete-dialog.component';
import { ICategory } from '../../category/category.model';
import { NgSelectModule } from '@ng-select/ng-select';
import { CategoryService } from '../../category/service/category.service';
import { CategoryComponent } from '../../category/list/category.component';
import { BudgetComponent } from 'app/entities/budget/list/budget.component';
import { IBudget } from 'app/entities/budget/budget.model';
import { BudgetService } from 'app/entities/budget/service/budget.service';

// Định nghĩa kiểu cho queryObject
interface QueryObject {
  page: number;
  size: number;
  eagerload?: boolean;
  sort: string | string[];
  [key: string]: any;
}

// Custom Date Parser Formatter
@Injectable()
export class CustomDateParserFormatter extends NgbDateParserFormatter {
  parse(value: string): NgbDateStruct | null {
    if (!value) return null;
    const parts = value.split('/').map(part => parseInt(part, 10));
    return { day: parts[0] || 1, month: parts[1] || 1, year: parts[2] || new Date().getFullYear() };
  }

  format(date: NgbDateStruct | null): string {
    return date ? `${date.day.toString().padStart(2, '0')}/${date.month.toString().padStart(2, '0')}/${date.year}` : '';
  }
}

@Component({
  selector: 'jhi-transaction',
  styleUrls: ['./transaction.component.scss'],
  templateUrl: './transaction.component.html',
  imports: [
    RouterModule,
    FormsModule,
    SharedModule,
    SortDirective,
    SortByDirective,
    FormatMediumDatetimePipe,
    FilterComponent,
    ItemCountComponent,
    NgSelectModule,
    CategoryComponent,
    BudgetComponent,
    TruncatePipe,
  ],
  providers: [{ provide: NgbDateParserFormatter, useClass: CustomDateParserFormatter }],
})
export class TransactionComponent implements OnInit {
  // Signals
  transactions = signal<ITransaction[]>([]);
  categories = signal<ICategory[]>([]);
  budgets = signal<IBudget[]>([]);
  isLoading = false;

  // Sort States
  sortState = sortStateSignal({});
  categorySortState = sortStateSignal({});
  budgetSortState = sortStateSignal({});

  // Pagination
  page = 1;
  categoryPage = 1;
  budgetPage = 1;

  // Totals
  transactionTotalItems = signal<number>(0);
  categoryTotalItems = signal<number>(0);
  budgetTotalItems = signal<number>(0);

  // Filters
  filters: IFilterOptions = new FilterOptions();
  itemsPerPage = ITEMS_PER_PAGE;

  // Search Fields
  category: number | null = null;
  fromDate: Date | null = null;
  toDate: Date | null = null;
  type: string | null = null;
  fromDateStruct: NgbDateStruct | null = null;
  toDateStruct: NgbDateStruct | null = null;

  // Search State
  private searchState: {
    category: number | null;
    fromDate: Date | null;
    toDate: Date | null;
    type: string | null;
    sortState: SortState;
  } = {
    category: null,
    fromDate: null,
    toDate: null,
    type: null,
    sortState: { predicate: 'transactionDate', order: 'desc' },
  };

  // Dependencies
  private readonly router = inject(Router);
  private readonly transactionService = inject(TransactionService);
  private readonly categoryService = inject(CategoryService);
  private readonly budgetService = inject(BudgetService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly sortService = inject(SortService);
  private readonly modalService = inject(NgbModal);
  private readonly ngZone = inject(NgZone);

  private subscription: Subscription | null = null;

  trackId = (item: ITransaction): number => this.transactionService.getTransactionIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => {
          if (!this.activatedRoute.snapshot.queryParamMap.get(SORT)) {
            this.sortState.set({ predicate: 'transactionDate', order: 'desc' });
            this.categorySortState.set({ predicate: 'createdAt', order: 'desc' });
            this.budgetSortState.set({ predicate: 'createdAt', order: 'desc' });
          }
          this.loadTransactions();
          this.loadCategories();
          this.loadBudgets();
        }),
      )
      .subscribe();

    this.filters.filterChanges.subscribe(filterOptions => this.handleNavigation(1, this.sortState(), filterOptions));
  }

  delete(transaction: ITransaction): void {
    const modalRef = this.modalService.open(TransactionDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.transaction = transaction;
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.loadTransactions()),
      )
      .subscribe();
  }

  refresh(): void {
    this.category = null;
    this.fromDate = null;
    this.toDate = null;
    this.type = null;
    this.fromDateStruct = null;
    this.toDateStruct = null;
    this.page = 1;
    this.filters = new FilterOptions();
    this.sortState.set({ predicate: 'transactionDate', order: 'desc' });

    this.searchState = {
      category: null,
      fromDate: null,
      toDate: null,
      type: null,
      sortState: { predicate: 'transactionDate', order: 'desc' },
    };

    this.categoryPage = 1;
    this.categorySortState.set({ predicate: 'createdAt', order: 'desc' });

    this.budgetPage = 1;
    this.budgetSortState.set({ predicate: 'createdAt', order: 'desc' });

    this.loadTransactions();
    this.loadCategories();
    this.loadBudgets();
  }

  loadTransactions(): void {
    this.queryBackend().subscribe({ next: (res: EntityArrayResponseType) => this.onResponseSuccess(res) });
  }

  loadCategories(): void {
    const queryObject: QueryObject = {
      page: this.categoryPage - 1,
      size: this.itemsPerPage,
      sort: this.validateSortParam(this.categorySortState(), ['createdAt', 'categoryName', 'categoryType']),
    };
    this.categoryService.query(queryObject).subscribe({
      next: (res: EntityArrayResponseType) => {
        this.categories.set(res.body ?? []);
        this.categoryTotalItems.set(Number(res.headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
      },
    });
  }

  loadBudgets(): void {
    const queryObject: QueryObject = {
      page: this.budgetPage - 1,
      size: this.itemsPerPage,
      eagerload: true,
      sort: this.validateSortParam(this.budgetSortState(), ['createdAt', 'budgetAmount', 'categoryId', 'startDate', 'endDate', 'status']),
    };
    this.budgetService.query(queryObject).subscribe({
      next: (res: EntityArrayResponseType) => {
        this.budgets.set(res.body ?? []);
        this.budgetTotalItems.set(Number(res.headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
      },
    });
  }

  navigateToWithComponentValues(event: SortState): void {
    this.sortState.set(event);
    if (this.isSearchActive()) {
      this.searchState.sortState = event;
      this.queryS().subscribe({ next: (res: EntityArrayResponseType) => this.onResponseSuccess(res) });
    } else {
      this.handleNavigation(this.page, event, this.filters.filterOptions);
    }
  }

  navigateToPage(page: number): void {
    this.page = page;
    if (this.isSearchActive()) {
      this.queryS().subscribe({ next: (res: EntityArrayResponseType) => this.onResponseSuccess(res) });
    } else {
      this.handleNavigation(page, this.sortState(), this.filters.filterOptions);
    }
  }

  onCategorySort(event: SortState): void {
    this.categorySortState.set(event);
    this.loadCategories();
  }

  onBudgetSort(event: SortState): void {
    this.budgetSortState.set(event);
    this.loadBudgets();
  }

  onCategoryPageChange(page: number): void {
    this.categoryPage = page;
    this.loadCategories();
  }

  onBudgetPageChange(page: number): void {
    this.budgetPage = page;
    this.loadBudgets();
  }

  onCategoryDelete(category: ICategory): void {
    this.loadCategories();
  }

  onBudgetDelete(budget: IBudget): void {
    this.loadBudgets();
  }

  search(): void {
    this.page = 1;
    this.searchState = {
      category: this.category,
      fromDate: this.fromDate,
      toDate: this.toDate,
      type: this.type,
      sortState: this.sortState(),
    };
    this.queryS().subscribe({ next: (res: EntityArrayResponseType) => this.onResponseSuccess(res) });
  }

  onFromDateSelect(date: NgbDateStruct): void {
    this.fromDateStruct = date;
    this.fromDate = new Date(Date.UTC(date.year, date.month - 1, date.day));
  }

  onToDateSelect(date: NgbDateStruct): void {
    this.toDateStruct = date;
    this.toDate = new Date(Date.UTC(date.year, date.month - 1, date.day));
  }

  exportTransactions(): void {
    const queryParams: QueryObject = {
      category: this.category,
      fromDate: this.fromDate?.toISOString() ?? null,
      toDate: this.toDate?.toISOString() ?? null,
      type: this.type,
      page: 0,
      size: 0,
      sort: 'transactionDate',
    };
    this.transactionService.export(queryParams).subscribe({
      next(response: Blob) {
        const url = window.URL.createObjectURL(response);
        const a = document.createElement('a');
        a.href = url;
        a.download = `transactions.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => console.error('Export failed'),
    });
  }

  exportToPDF(): void {
    const queryParams: QueryObject = {
      category: this.category,
      fromDate: this.fromDate?.toISOString() ?? null,
      toDate: this.toDate?.toISOString() ?? null,
      type: this.type,
      page: 0,
      size: 0,
      sort: 'transactionDate',
    };
    this.transactionService.exportToPDF(queryParams).subscribe({
      next(response: Blob) {
        const url = window.URL.createObjectURL(response);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'transactions.pdf';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => console.error('Export to PDF failed'),
    });
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.page = +(params.get(PAGE_HEADER) ?? 1);
    const sortParam = params.get(SORT);
    if (sortParam) this.sortState.set(this.sortService.parseSortParam(sortParam));
    this.filters.initializeFromParams(params);
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers, 'transaction');
    this.transactions.set(this.fillComponentAttributesFromResponseBody(response.body));
  }

  protected fillComponentAttributesFromResponseBody(data: ITransaction[] | null): ITransaction[] {
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders, type: string): void {
    const total = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
    if (type === 'transaction') this.transactionTotalItems.set(total);
    else if (type === 'category') this.categoryTotalItems.set(total);
    else if (type === 'budget') this.budgetTotalItems.set(total);
  }

  protected queryBackend(): Observable<EntityArrayResponseType> {
    const { page, filters } = this;
    this.isLoading = true;
    const queryObject: QueryObject = {
      page: page - 1,
      size: this.itemsPerPage,
      eagerload: true,
      sort: this.validateSortParam(this.sortState(), ['transactionDate', 'categoryId', 'transactionType', 'description', 'amount']),
    };
    filters.filterOptions.forEach(filterOption => (queryObject[filterOption.name] = filterOption.values));
    return this.transactionService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected queryS(): Observable<EntityArrayResponseType> {
    this.isLoading = true;
    const queryObject: QueryObject = {
      page: this.page - 1,
      size: this.itemsPerPage,
      eagerload: true,
      sort: this.validateSortParam(this.searchState.sortState, [
        'transactionDate',
        'categoryId',
        'transactionType',
        'description',
        'amount',
      ]),
    };

    if (this.searchState.fromDate) queryObject['transactionDate.greaterThanOrEqual'] = this.searchState.fromDate.toISOString();
    if (this.searchState.toDate) queryObject['transactionDate.lessThanOrEqual'] = this.searchState.toDate.toISOString();
    if (this.searchState.type) queryObject['transactionType.equals'] = this.searchState.type;
    if (this.searchState.category) queryObject['categoryId.equals'] = this.searchState.category.toString();

    return this.transactionService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(page: number, sortState: SortState, filterOptions?: IFilterOption[]): void {
    const queryParams: QueryObject = {
      page,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(sortState),
    };

    filterOptions?.forEach(filterOption => (queryParams[filterOption.nameAsQueryParam()] = filterOption.values));

    if (this.isSearchActive()) {
      if (this.searchState.category) queryParams['category'] = this.searchState.category;
      if (this.searchState.fromDate) queryParams['fromDate'] = this.searchState.fromDate.toISOString();
      if (this.searchState.toDate) queryParams['toDate'] = this.searchState.toDate.toISOString();
      if (this.searchState.type) queryParams['type'] = this.searchState.type;
    }

    this.ngZone.run(() => this.router.navigate(['./'], { relativeTo: this.activatedRoute, queryParams }));
  }

  handleBudgetClick(transaction: ITransaction): void {
    this.router.navigate(['/transaction', transaction.id, 'view']);
  }

  private validateSortParam(sortState: SortState, validFields: string[]): string {
    return sortState.predicate && validFields.includes(sortState.predicate)
      ? `${sortState.predicate},${sortState.order}`
      : `${validFields[0]},desc`;
  }

  private isSearchActive(): boolean {
    return !!(this.searchState.category ?? this.searchState.fromDate ?? this.searchState.toDate ?? this.searchState.type);
  }
}
