import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NumberFormatDirective } from 'app/shared/directive/number-format.directive';
import { ICategory } from 'app/entities/category/category.model';
import { CategoryService } from 'app/entities/category/service/category.service';
import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { BudgetService } from '../service/budget.service';
import { IBudget } from '../budget.model';
import { BudgetFormGroup, BudgetFormService } from './budget-form.service';

@Component({
  selector: 'jhi-budget-update',
  styleUrls: ['./budget-update.component.scss'],
  templateUrl: './budget-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule, NumberFormatDirective],
})
export class BudgetUpdateComponent implements OnInit {
  isSaving = false;
  budget: IBudget | null = null;

  categoriesSharedCollection: ICategory[] = [];
  usersSharedCollection: IUser[] = [];

  protected budgetService = inject(BudgetService);
  protected budgetFormService = inject(BudgetFormService);
  protected categoryService = inject(CategoryService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: BudgetFormGroup = this.budgetFormService.createBudgetFormGroup();

  compareCategory = (o1: ICategory | null, o2: ICategory | null): boolean => this.categoryService.compareCategory(o1, o2);

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ budget }) => {
      this.budget = budget;
      if (budget) {
        this.updateForm(budget);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const budget = this.budgetFormService.getBudget(this.editForm);
    if (budget.id !== null) {
      this.subscribeToSaveResponse(this.budgetService.update(budget));
    } else {
      this.subscribeToSaveResponse(this.budgetService.create(budget));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IBudget>>): void {
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

  protected updateForm(budget: IBudget): void {
    this.budget = budget;
    this.budgetFormService.resetForm(this.editForm, budget);

    this.categoriesSharedCollection = this.categoryService.addCategoryToCollectionIfMissing<ICategory>(
      this.categoriesSharedCollection,
      budget.category,
    );
    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, budget.user);
  }

  protected loadRelationshipsOptions(): void {
    this.categoryService
      .query()
      .pipe(map((res: HttpResponse<ICategory[]>) => res.body ?? []))
      .pipe(
        map((categories: ICategory[]) =>
          this.categoryService.addCategoryToCollectionIfMissing<ICategory>(categories, this.budget?.category),
        ),
      )
      .subscribe((categories: ICategory[]) => {
        this.categoriesSharedCollection = categories;
      });

    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.budget?.user)))
      .subscribe((users: IUser[]) => {
        this.usersSharedCollection = users;
      });
  }
  getTranslatedStatus(): string {
    const status = this.editForm.get('status')?.value;
    if (!status) return '';
    return status === 'ACTIVE' ? 'Active' : status === 'INACTIVE' ? 'Inactive' : status === 'ENDED' ? 'Ended' : 'Pending';
  }
}
