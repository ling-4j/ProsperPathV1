import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormatMediumDatePipe } from 'app/shared/date';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';
import { ITEMS_PER_PAGE, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { FilterComponent, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { IBudget } from '../budget.model';
import { BudgetService } from '../service/budget.service';
import { BudgetDeleteDialogComponent } from '../delete/budget-delete-dialog.component';
import { CurrencyShortPipe } from 'app/shared/truncate/currencyShort';

@Component({
  selector: 'jhi-budget',
  styleUrls: ['./budget.component.scss'],
  templateUrl: './budget.component.html',
  imports: [RouterModule, FormsModule, SharedModule, SortDirective, SortByDirective, FormatMediumDatePipe, CurrencyShortPipe],
})
export class BudgetComponent {
  @Input() budgets: IBudget[] = [];
  @Input() totalItems = 0;
  @Output() sortChange = new EventEmitter<SortState>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() deleteBudget = new EventEmitter<IBudget>();

  sortState = sortStateSignal({ predicate: 'createdAt', order: 'desc' });
  filters: IFilterOptions = new FilterOptions();
  itemsPerPage = ITEMS_PER_PAGE;
  page = 1;

  public readonly router = inject(Router);
  protected readonly budgetService = inject(BudgetService);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);

  trackId = (item: IBudget): number => this.budgetService.getBudgetIdentifier(item);

  delete(budget: IBudget): void {
    const modalRef = this.modalService.open(BudgetDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.budget = budget;
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.deleteBudget.emit(budget)),
      )
      .subscribe();
  }

  navigateToWithComponentValues(event: SortState): void {
    this.sortState.set(event);
    this.sortChange.emit(event);
  }

  navigateToPage(page: number): void {
    this.page = page;
    this.pageChange.emit(page);
  }

  handleBudgetClick(budget: IBudget): void {
    this.router.navigate(['/budget', budget.id, 'view']);
  }
}
