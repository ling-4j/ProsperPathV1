import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select'; // Import NgSelectModule

import { CategoryType } from 'app/entities/enumerations/category-type.model';
import { ICategory } from '../category.model';
import { CategoryService } from '../service/category.service';
import { CategoryFormGroup, CategoryFormService } from './category-form.service';

@Component({
  selector: 'jhi-category-update',
  styleUrls: ['./category-update.component.scss'],
  templateUrl: './category-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule, NgSelectModule], // Thêm NgSelectModule
})
export class CategoryUpdateComponent implements OnInit {
  isSaving = false;
  category: ICategory | null = null;
  categoryTypeValues = Object.keys(CategoryType);

  // Danh sách icon để hiển thị trong dropdown
  availableIcons: { name: string; value: string }[] = [
    { name: 'Cart Shopping', value: 'fa-cart-shopping' },
    { name: 'Money Bill', value: 'fa-money-bill' },
    { name: 'Wallet', value: 'fa-wallet' },
    { name: 'Ellipsis H', value: 'fa-ellipsis-h' },
    { name: 'Utensils', value: 'fa-utensils' },
    { name: 'Car', value: 'fa-car' },
    { name: 'Film', value: 'fa-film' },
    { name: 'Piggy Bank', value: 'fa-piggy-bank' },
    { name: 'Chart Line', value: 'fa-chart-line' },
    { name: 'Heartbeat', value: 'fa-heartbeat' },
    { name: 'Plane', value: 'fa-plane' },
    { name: 'Credit Card', value: 'fa-credit-card' },
    { name: 'Gift', value: 'fa-gift' },
  ];

  protected categoryService = inject(CategoryService);
  protected categoryFormService = inject(CategoryFormService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: CategoryFormGroup = this.categoryFormService.createCategoryFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ category }) => {
      this.category = category;
      if (category) {
        this.updateForm(category);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const category = this.categoryFormService.getCategory(this.editForm);
    if (category.id !== null) {
      this.subscribeToSaveResponse(this.categoryService.update(category));
    } else {
      this.subscribeToSaveResponse(this.categoryService.create(category));
    }
  }

  onIconChange(selectedIcon: any): void {
    if (selectedIcon) {
      this.editForm.patchValue({ categoryIcon: selectedIcon.value });
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICategory>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(category: ICategory): void {
    this.category = category;
    this.categoryFormService.resetForm(this.editForm, category);
  }
}
