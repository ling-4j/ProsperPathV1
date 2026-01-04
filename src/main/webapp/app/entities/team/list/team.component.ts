import { Component, NgZone, OnInit, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';

import SharedModule from 'app/shared/shared.module';
import { SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';

import { ITeam } from '../team.model';
import { EntityArrayResponseType, TeamService } from '../service/team.service';
import { TeamDeleteDialogComponent } from '../delete/team-delete-dialog.component';

@Component({
  selector: 'jhi-team',
  templateUrl: './team.component.html',
  styleUrls: ['./team.component.scss'],
  imports: [RouterModule, FormsModule, SharedModule],
})
export class TeamComponent implements OnInit {
  @ViewChild('scrollContainer') scrollContainer!: ElementRef;

  subscription: Subscription | null = null;

  teams = signal<ITeam[]>([]);
  displayedTeams = signal<ITeam[]>([]);
  isLoading = false;

  pageSize = 20;
  pageIndex = 0;

  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();

  private readonly router = inject(Router);
  private readonly teamService = inject(TeamService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly sortService = inject(SortService);
  private readonly modalService = inject(NgbModal);
  private readonly ngZone = inject(NgZone);

  trackId(_: number, item: ITeam): number {
    return item.id;
  }

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();

    this.filters.filterChanges.subscribe(filters => this.handleNavigation(1, this.sortState(), filters));
  }

  handleTeamClick(team: ITeam): void {
    this.router.navigate(['/team', team.id, 'view']);
  }

  delete(team: ITeam): void {
    const modalRef = this.modalService.open(TeamDeleteDialogComponent, {
      size: 'lg',
      backdrop: 'static',
    });
    modalRef.componentInstance.team = team;

    modalRef.closed
      .pipe(
        filter(reason => reason === 'deleted'),
        tap(() => this.load()),
      )
      .subscribe();
  }

  onScroll(): void {
    const el = this.scrollContainer.nativeElement as HTMLElement;
    if (el.scrollHeight - el.scrollTop <= el.clientHeight + 10) {
      this.loadNextBatch();
    }
  }

  private loadNextBatch(): void {
    const start = this.pageIndex * this.pageSize;
    const end = start + this.pageSize;
    const allTeams = this.teams();

    if (start >= allTeams.length) return;

    this.displayedTeams.set([...this.displayedTeams(), ...allTeams.slice(start, end)]);

    this.pageIndex++;
  }

  private load(): void {
    this.queryBackend().subscribe({
      next: res => this.onResponseSuccess(res),
    });
  }

  private onResponseSuccess(response: EntityArrayResponseType): void {
    const data = response.body ?? [];
    this.teams.set(data);

    this.displayedTeams.set([]);
    this.pageIndex = 0;
    this.loadNextBatch();
  }

  private queryBackend(): Observable<EntityArrayResponseType> {
    this.isLoading = true;

    const queryObject: any = {
      page: 0,
      size: 9999,
      sort: this.sortService.buildSortParam(this.sortState()),
    };

    this.filters.filterOptions.forEach(f => {
      queryObject[f.name] = f.values;
    });

    return this.teamService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  private fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.sortState.set(this.sortService.parseSortParam(params.get('sort') ?? data['defaultSort']));
    this.filters.initializeFromParams(params);
  }

  private handleNavigation(page: number, sortState: SortState, filterOptions?: IFilterOption[]): void {
    const queryParams: any = {
      sort: this.sortService.buildSortParam(sortState),
    };

    filterOptions?.forEach(f => {
      queryParams[f.nameAsQueryParam()] = f.values;
    });

    this.ngZone.run(() =>
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams,
      }),
    );
  }
}
