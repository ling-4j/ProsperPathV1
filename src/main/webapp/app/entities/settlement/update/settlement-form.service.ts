import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ISettlement, NewSettlement } from '../settlement.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ISettlement for edit and NewSettlementFormGroupInput for create.
 */
type SettlementFormGroupInput = ISettlement | PartialWithRequiredKeyOf<NewSettlement>;

type SettlementFormDefaults = Pick<NewSettlement, 'id'>;

type SettlementFormGroupContent = {
  id: FormControl<ISettlement['id'] | NewSettlement['id']>;
  amount: FormControl<ISettlement['amount']>;
  event: FormControl<ISettlement['event']>;
  fromMember: FormControl<ISettlement['fromMember']>;
  toMember: FormControl<ISettlement['toMember']>;
};

export type SettlementFormGroup = FormGroup<SettlementFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class SettlementFormService {
  createSettlementFormGroup(settlement: SettlementFormGroupInput = { id: null }): SettlementFormGroup {
    const settlementRawValue = {
      ...this.getFormDefaults(),
      ...settlement,
    };
    return new FormGroup<SettlementFormGroupContent>({
      id: new FormControl(
        { value: settlementRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      amount: new FormControl(settlementRawValue.amount, {
        validators: [Validators.required],
      }),
      event: new FormControl(settlementRawValue.event),
      fromMember: new FormControl(settlementRawValue.fromMember),
      toMember: new FormControl(settlementRawValue.toMember),
    });
  }

  getSettlement(form: SettlementFormGroup): ISettlement | NewSettlement {
    return form.getRawValue() as ISettlement | NewSettlement;
  }

  resetForm(form: SettlementFormGroup, settlement: SettlementFormGroupInput): void {
    const settlementRawValue = { ...this.getFormDefaults(), ...settlement };
    form.reset(
      {
        ...settlementRawValue,
        id: { value: settlementRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): SettlementFormDefaults {
    return {
      id: null,
    };
  }
}
