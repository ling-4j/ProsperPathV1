import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ISummary, NewSummary } from '../summary.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ISummary for edit and NewSummaryFormGroupInput for create.
 */
type SummaryFormGroupInput = ISummary | PartialWithRequiredKeyOf<NewSummary>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ISummary | NewSummary> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

type SummaryFormRawValue = FormValueOf<ISummary>;

type NewSummaryFormRawValue = FormValueOf<NewSummary>;

type SummaryFormDefaults = Pick<NewSummary, 'id' | 'createdAt' | 'updatedAt'>;

type SummaryFormGroupContent = {
  id: FormControl<SummaryFormRawValue['id'] | NewSummary['id']>;
  periodType: FormControl<SummaryFormRawValue['periodType']>;
  periodValue: FormControl<SummaryFormRawValue['periodValue']>;
  totalAssets: FormControl<SummaryFormRawValue['totalAssets']>;
  totalIncome: FormControl<SummaryFormRawValue['totalIncome']>;
  totalExpense: FormControl<SummaryFormRawValue['totalExpense']>;
  totalProfit: FormControl<SummaryFormRawValue['totalProfit']>;
  profitPercentage: FormControl<SummaryFormRawValue['profitPercentage']>;
  createdAt: FormControl<SummaryFormRawValue['createdAt']>;
  updatedAt: FormControl<SummaryFormRawValue['updatedAt']>;
  user: FormControl<SummaryFormRawValue['user']>;
};

export type SummaryFormGroup = FormGroup<SummaryFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class SummaryFormService {
  createSummaryFormGroup(summary: SummaryFormGroupInput = { id: null }): SummaryFormGroup {
    const summaryRawValue = this.convertSummaryToSummaryRawValue({
      ...this.getFormDefaults(),
      ...summary,
    });
    return new FormGroup<SummaryFormGroupContent>({
      id: new FormControl(
        { value: summaryRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      periodType: new FormControl(summaryRawValue.periodType, {
        validators: [Validators.required],
      }),
      periodValue: new FormControl(summaryRawValue.periodValue, {
        validators: [Validators.required],
      }),
      totalAssets: new FormControl(summaryRawValue.totalAssets),
      totalIncome: new FormControl(summaryRawValue.totalIncome),
      totalExpense: new FormControl(summaryRawValue.totalExpense),
      totalProfit: new FormControl(summaryRawValue.totalProfit),
      profitPercentage: new FormControl(summaryRawValue.profitPercentage),
      createdAt: new FormControl(summaryRawValue.createdAt),
      updatedAt: new FormControl(summaryRawValue.updatedAt),
      user: new FormControl(summaryRawValue.user, {
        validators: [Validators.required],
      }),
    });
  }

  getSummary(form: SummaryFormGroup): ISummary | NewSummary {
    return this.convertSummaryRawValueToSummary(form.getRawValue() as SummaryFormRawValue | NewSummaryFormRawValue);
  }

  resetForm(form: SummaryFormGroup, summary: SummaryFormGroupInput): void {
    const summaryRawValue = this.convertSummaryToSummaryRawValue({ ...this.getFormDefaults(), ...summary });
    form.reset(
      {
        ...summaryRawValue,
        id: { value: summaryRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): SummaryFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
    };
  }

  private convertSummaryRawValueToSummary(rawSummary: SummaryFormRawValue | NewSummaryFormRawValue): ISummary | NewSummary {
    return {
      ...rawSummary,
      createdAt: dayjs(rawSummary.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawSummary.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertSummaryToSummaryRawValue(
    summary: ISummary | (Partial<NewSummary> & SummaryFormDefaults),
  ): SummaryFormRawValue | PartialWithRequiredKeyOf<NewSummaryFormRawValue> {
    return {
      ...summary,
      createdAt: summary.createdAt ? summary.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: summary.updatedAt ? summary.updatedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
