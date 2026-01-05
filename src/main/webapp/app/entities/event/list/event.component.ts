import { Component, NgZone, OnInit, inject, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormatMediumDatePipe } from 'app/shared/date';
import { FormsModule } from '@angular/forms';
import { PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { IEvent } from '../event.model';

import { EntityArrayResponseType, EventService } from '../service/event.service';
import { EventDeleteDialogComponent } from '../delete/event-delete-dialog.component';
import { TeamComponent } from 'app/entities/team/list/team.component';
import { MemberComponent } from 'app/entities/member/list/member.component';
import { TruncatePipe } from '../../../shared/truncate/truncate.pipe';

@Component({
  selector: 'jhi-event',
  templateUrl: './event.component.html',
  styleUrls: ['./event.component.scss'],
  imports: [RouterModule, FormsModule, SharedModule, FormatMediumDatePipe, TeamComponent, MemberComponent, TruncatePipe],
})
export class EventComponent implements OnInit {
  subscription: Subscription | null = null;
  events = signal<IEvent[]>([]);
  isLoading = false;

  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();

  itemsPerPage = 8;
  totalItems = 0;
  page = 1;

  public readonly router = inject(Router);
  protected readonly eventService = inject(EventService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId(index: number, item: IEvent): number {
    return item.id;
  }

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();

    this.filters.filterChanges.subscribe(filterOptions => this.handleNavigation(1, this.sortState(), filterOptions));
  }

  delete(event: IEvent): void {
    const modalRef = this.modalService.open(EventDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.event = event;

    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
        tap(() => {
          if (this.events().length === 0 && this.page !== 1) {
            this.page = 1;
            this.handleNavigation(1, this.sortState(), this.filters.filterOptions);
          }
        }),
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

  refresh(): void {
    this.page = 1;
    this.handleNavigation(1, this.sortState(), this.filters.filterOptions);
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
    this.events.set(dataFromBody);

    const currentPageHasNoData = dataFromBody.length === 0;
    const notOnFirstPage = this.page > 1;
    const thereAreSomeItemsInTotal = this.totalItems > 0;

    if (currentPageHasNoData && notOnFirstPage) {
      if (thereAreSomeItemsInTotal) {
        this.page = 1;
        this.handleNavigation(1, this.sortState(), this.filters.filterOptions);
      } else {
        this.page = 1;
        this.handleNavigation(1, this.sortState(), this.filters.filterOptions);
      }
    }
  }

  protected fillComponentAttributesFromResponseBody(data: IEvent[] | null): IEvent[] {
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
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    filters.filterOptions.forEach(filterOption => {
      queryObject[filterOption.name] = filterOption.values;
    });
    return this.eventService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
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

  handleEventClick(event: IEvent): void {
    this.router.navigate(['/event', event.id, 'view']);
  }
}
