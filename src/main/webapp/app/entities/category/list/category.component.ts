import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';
import { ITEMS_PER_PAGE, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { FilterComponent, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { ICategory } from '../category.model';
import { CategoryService } from '../service/category.service';
import { CategoryDeleteDialogComponent } from '../delete/category-delete-dialog.component';

@Component({
  selector: 'jhi-category',
  styleUrls: ['./category.component.scss'],
  templateUrl: './category.component.html',
  standalone: true,
  imports: [
    RouterModule,
    FormsModule,
    SharedModule,
    SortDirective,
    SortByDirective,
    // FilterComponent,
    // ItemCountComponent,
  ],
})
export class CategoryComponent {
  @Input() categories: ICategory[] = [];
  @Input() totalItems: number = 0;
  @Output() sortChange = new EventEmitter<SortState>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() deleteCategory = new EventEmitter<ICategory>();

  sortState = sortStateSignal({ predicate: 'id', order: 'desc' });
  filters: IFilterOptions = new FilterOptions();
  itemsPerPage = ITEMS_PER_PAGE;
  page = 1;

  protected readonly categoryService = inject(CategoryService);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  public readonly router = inject(Router);

  trackId = (item: ICategory): number => this.categoryService.getCategoryIdentifier(item);

  delete(category: ICategory): void {
    const modalRef = this.modalService.open(CategoryDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.category = category;
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.deleteCategory.emit(category)),
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
}