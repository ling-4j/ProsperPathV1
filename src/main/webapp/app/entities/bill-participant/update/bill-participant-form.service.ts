import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IBillParticipant, NewBillParticipant } from '../bill-participant.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IBillParticipant for edit and NewBillParticipantFormGroupInput for create.
 */
type BillParticipantFormGroupInput = IBillParticipant | PartialWithRequiredKeyOf<NewBillParticipant>;

type BillParticipantFormDefaults = Pick<NewBillParticipant, 'id'>;

type BillParticipantFormGroupContent = {
  id: FormControl<IBillParticipant['id'] | NewBillParticipant['id']>;
  shareAmount: FormControl<IBillParticipant['shareAmount']>;
  bill: FormControl<IBillParticipant['bill']>;
  member: FormControl<IBillParticipant['member']>;
};

export type BillParticipantFormGroup = FormGroup<BillParticipantFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class BillParticipantFormService {
  createBillParticipantFormGroup(billParticipant: BillParticipantFormGroupInput = { id: null }): BillParticipantFormGroup {
    const billParticipantRawValue = {
      ...this.getFormDefaults(),
      ...billParticipant,
    };
    return new FormGroup<BillParticipantFormGroupContent>({
      id: new FormControl(
        { value: billParticipantRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      shareAmount: new FormControl(billParticipantRawValue.shareAmount, {
        validators: [Validators.required],
      }),
      bill: new FormControl(billParticipantRawValue.bill),
      member: new FormControl(billParticipantRawValue.member),
    });
  }

  getBillParticipant(form: BillParticipantFormGroup): IBillParticipant | NewBillParticipant {
    return form.getRawValue() as IBillParticipant | NewBillParticipant;
  }

  resetForm(form: BillParticipantFormGroup, billParticipant: BillParticipantFormGroupInput): void {
    const billParticipantRawValue = { ...this.getFormDefaults(), ...billParticipant };
    form.reset(
      {
        ...billParticipantRawValue,
        id: { value: billParticipantRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): BillParticipantFormDefaults {
    return {
      id: null,
    };
  }
}
