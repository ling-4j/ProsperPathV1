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
    const regex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const match = message.match(regex);

    if (match) {
      const budgetAmount = match[1]; // Ví dụ: "1.000"
      const exceededAmount = match[2]; // Ví dụ: "1.222"
      let startDateIso = match[3]; // Ví dụ: "2025-05-03T07:45:00Z" hoặc "03/05/2025"
      let endDateIso = match[4]; // Ví dụ: "2025-05-12T07:45:00Z" hoặc "12/05/2025"

      // Định dạng ngày từ ISO sang dd/MM/yyyy nếu cần
      const startDateFormatted = startDateIso.includes('T')
        ? new Date(startDateIso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
        : startDateIso;
      const endDateFormatted = endDateIso.includes('T')
        ? new Date(endDateIso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
        : endDateIso;

      return `Bạn đã chi tiêu vượt quá ngân sách có khối lượng ${budgetAmount}₫ với số tiền vượt là ${exceededAmount}₫ trong khoảng thời gian đã thiết lập từ ngày ${startDateFormatted} đến ngày ${endDateFormatted} của danh mục`;
    }
    console.log('No match for message:', message); // Debug
    return message;
  }

  // Lấy categoryIcon (loại bỏ tiền tố 'fa-')
  getCategoryIcon(message: string): string {
    const regex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const match = message.match(regex);
    return match && match[5] ? match[5].replace('fa-', '') : '';
  }

  // Lấy categoryName
  getCategoryName(message: string): string {
    const regex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const match = message.match(regex);
    return match ? match[6] : '';
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