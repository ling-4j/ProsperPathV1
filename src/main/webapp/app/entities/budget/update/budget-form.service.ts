import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IBudget, NewBudget } from '../budget.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IBudget for edit and NewBudgetFormGroupInput for create.
 */
type BudgetFormGroupInput = IBudget | PartialWithRequiredKeyOf<NewBudget>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IBudget | NewBudget> = Omit<T, 'startDate' | 'endDate' | 'createdAt' | 'updatedAt'> & {
  startDate?: string | null;
  endDate?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

type BudgetFormRawValue = FormValueOf<IBudget>;

type NewBudgetFormRawValue = FormValueOf<NewBudget>;

type BudgetFormDefaults = Pick<NewBudget, 'id' | 'startDate' | 'endDate' | 'createdAt' | 'updatedAt'>;

type BudgetFormGroupContent = {
  id: FormControl<BudgetFormRawValue['id'] | NewBudget['id']>;
  budgetAmount: FormControl<BudgetFormRawValue['budgetAmount']>;
  startDate: FormControl<BudgetFormRawValue['startDate']>;
  endDate: FormControl<BudgetFormRawValue['endDate']>;
  createdAt: FormControl<BudgetFormRawValue['createdAt']>;
  updatedAt: FormControl<BudgetFormRawValue['updatedAt']>;
  status: FormControl<BudgetFormRawValue['status']>;
  category: FormControl<BudgetFormRawValue['category']>;
  user: FormControl<BudgetFormRawValue['user']>;
};

export type BudgetFormGroup = FormGroup<BudgetFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class BudgetFormService {
  createBudgetFormGroup(budget: BudgetFormGroupInput = { id: null }): BudgetFormGroup {
    const budgetRawValue = this.convertBudgetToBudgetRawValue({
      ...this.getFormDefaults(),
      ...budget,
    });
    return new FormGroup<BudgetFormGroupContent>({
      id: new FormControl(
        { value: budgetRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      budgetAmount: new FormControl(budgetRawValue.budgetAmount, {
        validators: [Validators.required],
      }),
      startDate: new FormControl(budgetRawValue.startDate, {
        validators: [Validators.required],
      }),
      endDate: new FormControl(budgetRawValue.endDate, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(budgetRawValue.createdAt),
      updatedAt: new FormControl(budgetRawValue.updatedAt),
      status: new FormControl(budgetRawValue.status),
      category: new FormControl(budgetRawValue.category),
      user: new FormControl(budgetRawValue.user),
    });
  }

  getBudget(form: BudgetFormGroup): IBudget | NewBudget {
    return this.convertBudgetRawValueToBudget(form.getRawValue() as BudgetFormRawValue | NewBudgetFormRawValue);
  }

  resetForm(form: BudgetFormGroup, budget: BudgetFormGroupInput): void {
    const budgetRawValue = this.convertBudgetToBudgetRawValue({ ...this.getFormDefaults(), ...budget });
    form.reset(
      {
        ...budgetRawValue,
        id: { value: budgetRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): BudgetFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      startDate: currentTime,
      endDate: currentTime,
      createdAt: currentTime,
      updatedAt: currentTime,
    };
  }

  private convertBudgetRawValueToBudget(rawBudget: BudgetFormRawValue | NewBudgetFormRawValue): IBudget | NewBudget {
    return {
      ...rawBudget,
      startDate: dayjs(rawBudget.startDate, DATE_TIME_FORMAT),
      endDate: dayjs(rawBudget.endDate, DATE_TIME_FORMAT),
      createdAt: dayjs(rawBudget.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawBudget.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertBudgetToBudgetRawValue(
    budget: IBudget | (Partial<NewBudget> & BudgetFormDefaults),
  ): BudgetFormRawValue | PartialWithRequiredKeyOf<NewBudgetFormRawValue> {
    return {
      ...budget,
      startDate: budget.startDate ? budget.startDate.format(DATE_TIME_FORMAT) : undefined,
      endDate: budget.endDate ? budget.endDate.format(DATE_TIME_FORMAT) : undefined,
      createdAt: budget.createdAt ? budget.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: budget.updatedAt ? budget.updatedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
