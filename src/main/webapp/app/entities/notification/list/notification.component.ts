import { Component, NgZone, OnInit, inject, signal, computed } from '@angular/core'; // Thêm computed
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { FilterComponent, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { INotification } from '../notification.model';
import { EntityArrayResponseType, NotificationService } from '../service/notification.service';
import { NotificationDeleteDialogComponent } from '../delete/notification-delete-dialog.component';

@Component({
  selector: 'jhi-notification',
  styleUrls: ['./notification.component.scss'],
  templateUrl: './notification.component.html',
  imports: [
    RouterModule,
    FormsModule,
    SharedModule,
    // SortDirective,
    // SortByDirective,
    FormatMediumDatetimePipe,
    // FilterComponent,
    FontAwesomeModule
  ],
  standalone: true,
})
export class NotificationComponent implements OnInit {
  subscription: Subscription | null = null;
  notifications = signal<INotification[]>([]);
  filteredNotifications = computed(() => { // Sử dụng computed thay vì signal với hàm
    const allNotifications = this.notifications();
    return this.filterType() === 'all' ? allNotifications : allNotifications.filter(n =>
      this.filterType() === 'unread' ? !n.isRead : n.isRead
    );
  });
  isLoading = false;

  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();
  filterType = signal<'all' | 'unread' | 'read'>('all');

  itemsPerPage = 100;
  totalItems = 0;
  page = 1;

  public readonly router = inject(Router);
  protected readonly notificationService = inject(NotificationService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId = (item: INotification): number => this.notificationService.getNotificationIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => {
          // Nếu không có sort param trong URL, đặt sortState mặc định
          if (!this.activatedRoute.snapshot.queryParamMap.get(SORT)) {
            this.sortState.set({ predicate: 'createdAt', order: 'desc' });
          }
          this.load();
        }),
      )
      .subscribe();

    this.filters.filterChanges.subscribe(filterOptions => this.handleNavigation(1, this.sortState(), filterOptions));
  }

  delete(notification: INotification): void {
    const modalRef = this.modalService.open(NotificationDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.notification = notification;
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page, event, this.filters.filterOptions);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState(), this.filters.filterOptions);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    this.filters.initializeFromParams(params);
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.notifications.set(dataFromBody);
  }

  protected fillComponentAttributesFromResponseBody(data: INotification[] | null): INotification[] {
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  }

  protected queryBackend(): Observable<EntityArrayResponseType> {
    const { page, filters } = this;
    this.isLoading = true;
    const pageToLoad: number = page;
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage,
      eagerload: true,
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    filters.filterOptions.forEach(filterOption => {
      queryObject[filterOption.name] = filterOption.values;
    });
    return this.notificationService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(page: number, sortState: SortState, filterOptions?: IFilterOption[]): void {
    const queryParamsObj: any = {
      page,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(sortState),
    };
    filterOptions?.forEach(filterOption => {
      queryParamsObj[filterOption.nameAsQueryParam()] = filterOption.values;
    });
    this.ngZone.run(() => {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
      });
    });
  }

  // Tách phần văn bản của thông điệp (không bao gồm icon và categoryName)
  formatNotificationMessageText(message: string): string {
    const expenseRegex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const incomeRegex = /Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) vào ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
  
    const expenseMatch = message.match(expenseRegex);
    const incomeMatch = message.match(incomeRegex);
  
    if (expenseMatch) {
      const budgetAmount = expenseMatch[1]; 
      const exceededAmount = expenseMatch[2]; 
      let startDateIso = expenseMatch[3]; 
      let endDateIso = expenseMatch[4]; 
  
      return `Bạn đã chi tiêu vượt quá ngân sách có khối lượng ${budgetAmount}₫ với số tiền vượt là ${exceededAmount}₫ trong khoảng thời gian đã thiết lập từ ${startDateIso} đến ${endDateIso} của danh mục`;
    } else if (incomeMatch) {
      const budgetAmount = incomeMatch[1]; 
      let startDateIso = incomeMatch[2]; 
      let endDateIso = incomeMatch[3]; 
      let transactionDateIso = incomeMatch[4]; 
  
      return `Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ${budgetAmount}₫ trong khoảng thời gian đã thiết lập từ ${startDateIso} đến ${endDateIso} vào ngày ${transactionDateIso} của danh mục`;
    }
    console.log('No match for message:', message);
    return message;
  }

  // Lấy categoryIcon (loại bỏ tiền tố 'fa-')
  getCategoryIcon(message: string): string {
    const expenseRegex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const incomeRegex = /Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) vào ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
  
    const expenseMatch = message.match(expenseRegex);
    const incomeMatch = message.match(incomeRegex);
  
    if (incomeMatch && incomeMatch[5]) {
      return incomeMatch[5].replace('fa-', '');
    } else if (expenseMatch && expenseMatch[5]) {
      return expenseMatch[5].replace('fa-', '');
    }
    return 'null';
  }

  // Lấy categoryName
  getCategoryName(message: string): string {
    const expenseRegex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const incomeRegex = /Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) vào ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
  
    const expenseMatch = message.match(expenseRegex);
    const incomeMatch = message.match(incomeRegex);
  
    if (incomeMatch && incomeMatch[6]) {
      return incomeMatch[6];
    } else if (expenseMatch && expenseMatch[6]) {
      return expenseMatch[6];
    }
    return 'null';
  }

  // Phương thức để thay đổi filterType
  setFilterType(type: 'all' | 'unread' | 'read'): void {
    this.filterType.set(type);
  }

  handleNotificationClick(notification: INotification): void {
    if (!notification.isRead) {
      notification.isRead = true;
      this.notificationService.update(notification).subscribe();
    }
    this.router.navigate(['/notification', notification.id, 'view']);
  }
}