import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ICategory, NewCategory } from '../category.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICategory for edit and NewCategoryFormGroupInput for create.
 */
type CategoryFormGroupInput = ICategory | PartialWithRequiredKeyOf<NewCategory>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ICategory | NewCategory> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

type CategoryFormRawValue = FormValueOf<ICategory>;

type NewCategoryFormRawValue = FormValueOf<NewCategory>;

type CategoryFormDefaults = Pick<NewCategory, 'id' | 'createdAt' | 'updatedAt'>;

type CategoryFormGroupContent = {
  id: FormControl<CategoryFormRawValue['id'] | NewCategory['id']>;
  categoryName: FormControl<CategoryFormRawValue['categoryName']>;
  categoryType: FormControl<CategoryFormRawValue['categoryType']>;
  createdAt: FormControl<CategoryFormRawValue['createdAt']>;
  updatedAt: FormControl<CategoryFormRawValue['updatedAt']>;
  categoryIcon: FormControl<CategoryFormRawValue['categoryIcon']>;
};

export type CategoryFormGroup = FormGroup<CategoryFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CategoryFormService {
  createCategoryFormGroup(category: CategoryFormGroupInput = { id: null }): CategoryFormGroup {
    const categoryRawValue = this.convertCategoryToCategoryRawValue({
      ...this.getFormDefaults(),
      ...category,
    });
    return new FormGroup<CategoryFormGroupContent>({
      id: new FormControl(
        { value: categoryRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      categoryName: new FormControl(categoryRawValue.categoryName, {
        validators: [Validators.required],
      }),
      categoryType: new FormControl(categoryRawValue.categoryType, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(categoryRawValue.createdAt),
      updatedAt: new FormControl(categoryRawValue.updatedAt),
      categoryIcon: new FormControl(categoryRawValue.categoryIcon),
    });
  }

  getCategory(form: CategoryFormGroup): ICategory | NewCategory {
    return this.convertCategoryRawValueToCategory(form.getRawValue() as CategoryFormRawValue | NewCategoryFormRawValue);
  }

  resetForm(form: CategoryFormGroup, category: CategoryFormGroupInput): void {
    const categoryRawValue = this.convertCategoryToCategoryRawValue({ ...this.getFormDefaults(), ...category });
    form.reset(
      {
        ...categoryRawValue,
        id: { value: categoryRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): CategoryFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
    };
  }

  private convertCategoryRawValueToCategory(rawCategory: CategoryFormRawValue | NewCategoryFormRawValue): ICategory | NewCategory {
    return {
      ...rawCategory,
      createdAt: dayjs(rawCategory.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawCategory.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertCategoryToCategoryRawValue(
    category: ICategory | (Partial<NewCategory> & CategoryFormDefaults),
  ): CategoryFormRawValue | PartialWithRequiredKeyOf<NewCategoryFormRawValue> {
    return {
      ...category,
      createdAt: category.createdAt ? category.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: category.updatedAt ? category.updatedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
