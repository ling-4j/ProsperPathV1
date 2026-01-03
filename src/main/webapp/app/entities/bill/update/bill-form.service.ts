import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IBill, NewBill } from '../bill.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IBill for edit and NewBillFormGroupInput for create.
 */
type BillFormGroupInput = IBill | PartialWithRequiredKeyOf<NewBill>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IBill | NewBill> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type BillFormRawValue = FormValueOf<IBill>;

type NewBillFormRawValue = FormValueOf<NewBill>;

type BillFormDefaults = Pick<NewBill, 'id' | 'createdAt'>;

type BillFormGroupContent = {
  id: FormControl<BillFormRawValue['id'] | NewBill['id']>;
  name: FormControl<BillFormRawValue['name']>;
  amount: FormControl<BillFormRawValue['amount']>;
  createdAt: FormControl<BillFormRawValue['createdAt']>;
  event: FormControl<BillFormRawValue['event']>;
  payer: FormControl<BillFormRawValue['payer']>;
};

export type BillFormGroup = FormGroup<BillFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class BillFormService {
  createBillFormGroup(bill: BillFormGroupInput = { id: null }): BillFormGroup {
    const billRawValue = this.convertBillToBillRawValue({
      ...this.getFormDefaults(),
      ...bill,
    });
    return new FormGroup<BillFormGroupContent>({
      id: new FormControl(
        { value: billRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(billRawValue.name, {
        validators: [Validators.required],
      }),
      amount: new FormControl(billRawValue.amount, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(billRawValue.createdAt),
      event: new FormControl(billRawValue.event),
      payer: new FormControl(billRawValue.payer),
    });
  }

  getBill(form: BillFormGroup): IBill | NewBill {
    return this.convertBillRawValueToBill(form.getRawValue() as BillFormRawValue | NewBillFormRawValue);
  }

  resetForm(form: BillFormGroup, bill: BillFormGroupInput): void {
    const billRawValue = this.convertBillToBillRawValue({ ...this.getFormDefaults(), ...bill });
    form.reset(
      {
        ...billRawValue,
        id: { value: billRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): BillFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
    };
  }

  private convertBillRawValueToBill(rawBill: BillFormRawValue | NewBillFormRawValue): IBill | NewBill {
    return {
      ...rawBill,
      createdAt: dayjs(rawBill.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertBillToBillRawValue(
    bill: IBill | (Partial<NewBill> & BillFormDefaults),
  ): BillFormRawValue | PartialWithRequiredKeyOf<NewBillFormRawValue> {
    return {
      ...bill,
      createdAt: bill.createdAt ? bill.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
