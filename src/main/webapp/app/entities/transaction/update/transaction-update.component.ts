import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ICategory } from 'app/entities/category/category.model';
import { CategoryService } from 'app/entities/category/service/category.service';
import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { TransactionType } from 'app/entities/enumerations/transaction-type.model';
import { TransactionService } from '../service/transaction.service';
import { ITransaction } from '../transaction.model';
import { TransactionFormGroup, TransactionFormService } from './transaction-form.service';

@Component({
  selector: 'jhi-transaction-update',
  styleUrls: ['./transaction-update.component.scss'],
  templateUrl: './transaction-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class TransactionUpdateComponent implements OnInit {
  isSaving = false;
  transaction: ITransaction | null = null;
  transactionTypeValues = Object.keys(TransactionType);

  categoriesSharedCollection: ICategory[] = [];
  usersSharedCollection: IUser[] = [];

  protected transactionService = inject(TransactionService);
  protected transactionFormService = inject(TransactionFormService);
  protected categoryService = inject(CategoryService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: TransactionFormGroup = this.transactionFormService.createTransactionFormGroup();

  compareCategory = (o1: ICategory | null, o2: ICategory | null): boolean => this.categoryService.compareCategory(o1, o2);

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ transaction }) => {
      this.transaction = transaction;
      if (transaction) {
        this.updateForm(transaction);
      }

      this.loadRelationshipsOptions();

      // Tự động fill transactionType theo category.categoryType
      this.editForm.get('category')?.valueChanges.subscribe((category: ICategory | null | undefined) => {
        if (category && category.categoryType) {
          this.editForm.get('transactionType')?.setValue(category.categoryType, { emitEvent: false });
        } else {
          this.editForm.get('transactionType')?.setValue(null, { emitEvent: false });
        }
      });
    });
  }


  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const transaction = this.transactionFormService.getTransaction(this.editForm);
    if (transaction.id !== null) {
      this.subscribeToSaveResponse(this.transactionService.update(transaction));
    } else {
      this.subscribeToSaveResponse(this.transactionService.create(transaction));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ITransaction>>): void {
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

  protected updateForm(transaction: ITransaction): void {
    this.transaction = transaction;
    this.transactionFormService.resetForm(this.editForm, transaction);

    this.categoriesSharedCollection = this.categoryService.addCategoryToCollectionIfMissing<ICategory>(
      this.categoriesSharedCollection,
      transaction.category,
    );
    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, transaction.user);
  }

  protected loadRelationshipsOptions(): void {
    this.categoryService
      .query()
      .pipe(map((res: HttpResponse<ICategory[]>) => res.body ?? []))
      .pipe(
        map((categories: ICategory[]) =>
          this.categoryService.addCategoryToCollectionIfMissing<ICategory>(categories, this.transaction?.category),
        ),
      )
      .subscribe((categories: ICategory[]) => (this.categoriesSharedCollection = categories));

    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.transaction?.user)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  }

  formatAmount(value: number | string | null): string {
    if (value === null || value === undefined || value === '') return '';
    const numeric = Number(value.toString().replace(/,/g, ''));
    return isNaN(numeric) ? '' : numeric.toLocaleString('en-US');
  }

  onAmountInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const rawValue = input.value.replace(/,/g, '');
    const numericValue = parseFloat(rawValue);

    if (!isNaN(numericValue)) {
      this.editForm.get('amount')?.setValue(numericValue, { emitEvent: false });
    } else {
      this.editForm.get('amount')?.setValue(null, { emitEvent: false });
    }

    // Re-render formatted string
    input.value = this.formatAmount(rawValue);
  }
  onAmountBlur(event: Event): void {
    const input = event.target as HTMLInputElement;
    const amountValue = this.editForm.get('amount')?.value ?? null;
    input.value = this.formatAmount(amountValue);
  }
}
