import { Component, ElementRef, NgZone, OnInit, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormsModule } from '@angular/forms';
import { FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';

import { IMember } from '../member.model';
import { EntityArrayResponseType, MemberService } from '../service/member.service';
import { MemberDeleteDialogComponent } from '../delete/member-delete-dialog.component';

@Component({
  selector: 'jhi-member',
  templateUrl: './member.component.html',
  styleUrls: ['./member.component.scss'],
  imports: [RouterModule, FormsModule, SharedModule],
})
export class MemberComponent implements OnInit {
  @ViewChild('scrollContainer') scrollContainer!: ElementRef;

  subscription: Subscription | null = null;

  members = signal<IMember[]>([]);
  displayedMembers = signal<IMember[]>([]);
  isLoading = false;

  pageSize = 20;
  pageIndex = 0;

  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();

  public readonly router = inject(Router);
  protected readonly memberService = inject(MemberService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId(index: number, item: IMember): number {
    return item.id!;
  }

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();

    this.filters.filterChanges.subscribe(filterOptions => {
      this.handleNavigation(1, this.sortState(), filterOptions);
    });
  }

  handleMemberClick(member: IMember): void {
    this.router.navigate(['/member', member.id, 'view']);
  }

  delete(member: IMember): void {
    const modalRef = this.modalService.open(MemberDeleteDialogComponent, {
      size: 'lg',
      backdrop: 'static',
    });
    modalRef.componentInstance.member = member;

    modalRef.closed
      .pipe(
        filter(reason => reason === 'deleted'),
        tap(() => this.load()),
      )
      .subscribe();
  }

  onScroll(): void {
    const el = this.scrollContainer.nativeElement;
    if (el.scrollHeight - el.scrollTop <= el.clientHeight + 10) {
      this.loadNextBatch();
    }
  }

  loadNextBatch(): void {
    const start = this.pageIndex * this.pageSize;
    const end = start + this.pageSize;
    const allMembers = this.members();

    if (start >= allMembers.length) return;

    this.displayedMembers.set([...this.displayedMembers(), ...allMembers.slice(start, end)]);

    this.pageIndex++;
  }

  load(): void {
    this.queryBackend().subscribe({
      next: res => this.onResponseSuccess(res),
    });
  }

  protected queryBackend(): Observable<EntityArrayResponseType> {
    this.isLoading = true;

    const queryObject: any = {
      page: 0,
      size: 9999,
      sort: this.sortService.buildSortParam(this.sortState()),
    };

    this.filters.filterOptions.forEach(f => (queryObject[f.name] = f.values));

    return this.memberService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    const data = response.body ?? [];
    this.members.set(data);

    this.displayedMembers.set([]);
    this.pageIndex = 0;
    this.loadNextBatch();
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.sortState.set(this.sortService.parseSortParam(params.get('sort') ?? data['defaultSort']));
    this.filters.initializeFromParams(params);
  }

  protected handleNavigation(page: number, sortState: SortState, filterOptions?: IFilterOption[]): void {
    const queryParamsObj: any = {
      sort: this.sortService.buildSortParam(sortState),
    };

    filterOptions?.forEach(f => {
      queryParamsObj[f.nameAsQueryParam()] = f.values;
    });

    this.ngZone.run(() =>
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
      }),
    );
  }
}
