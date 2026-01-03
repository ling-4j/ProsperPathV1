import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IEventBalance, NewEventBalance } from '../event-balance.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IEventBalance for edit and NewEventBalanceFormGroupInput for create.
 */
type EventBalanceFormGroupInput = IEventBalance | PartialWithRequiredKeyOf<NewEventBalance>;

type EventBalanceFormDefaults = Pick<NewEventBalance, 'id'>;

type EventBalanceFormGroupContent = {
  id: FormControl<IEventBalance['id'] | NewEventBalance['id']>;
  paid: FormControl<IEventBalance['paid']>;
  shouldPay: FormControl<IEventBalance['shouldPay']>;
  balance: FormControl<IEventBalance['balance']>;
  event: FormControl<IEventBalance['event']>;
  member: FormControl<IEventBalance['member']>;
};

export type EventBalanceFormGroup = FormGroup<EventBalanceFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class EventBalanceFormService {
  createEventBalanceFormGroup(eventBalance: EventBalanceFormGroupInput = { id: null }): EventBalanceFormGroup {
    const eventBalanceRawValue = {
      ...this.getFormDefaults(),
      ...eventBalance,
    };
    return new FormGroup<EventBalanceFormGroupContent>({
      id: new FormControl(
        { value: eventBalanceRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      paid: new FormControl(eventBalanceRawValue.paid, {
        validators: [Validators.required],
      }),
      shouldPay: new FormControl(eventBalanceRawValue.shouldPay, {
        validators: [Validators.required],
      }),
      balance: new FormControl(eventBalanceRawValue.balance, {
        validators: [Validators.required],
      }),
      event: new FormControl(eventBalanceRawValue.event),
      member: new FormControl(eventBalanceRawValue.member),
    });
  }

  getEventBalance(form: EventBalanceFormGroup): IEventBalance | NewEventBalance {
    return form.getRawValue() as IEventBalance | NewEventBalance;
  }

  resetForm(form: EventBalanceFormGroup, eventBalance: EventBalanceFormGroupInput): void {
    const eventBalanceRawValue = { ...this.getFormDefaults(), ...eventBalance };
    form.reset(
      {
        ...eventBalanceRawValue,
        id: { value: eventBalanceRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): EventBalanceFormDefaults {
    return {
      id: null,
    };
  }
}
