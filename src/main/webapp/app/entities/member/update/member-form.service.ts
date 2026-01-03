import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IMember, NewMember } from '../member.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IMember for edit and NewMemberFormGroupInput for create.
 */
type MemberFormGroupInput = IMember | PartialWithRequiredKeyOf<NewMember>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IMember | NewMember> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type MemberFormRawValue = FormValueOf<IMember>;

type NewMemberFormRawValue = FormValueOf<NewMember>;

type MemberFormDefaults = Pick<NewMember, 'id' | 'createdAt'>;

type MemberFormGroupContent = {
  id: FormControl<MemberFormRawValue['id'] | NewMember['id']>;
  name: FormControl<MemberFormRawValue['name']>;
  note: FormControl<MemberFormRawValue['note']>;
  createdAt: FormControl<MemberFormRawValue['createdAt']>;
};

export type MemberFormGroup = FormGroup<MemberFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class MemberFormService {
  createMemberFormGroup(member: MemberFormGroupInput = { id: null }): MemberFormGroup {
    const memberRawValue = this.convertMemberToMemberRawValue({
      ...this.getFormDefaults(),
      ...member,
    });
    return new FormGroup<MemberFormGroupContent>({
      id: new FormControl(
        { value: memberRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(memberRawValue.name, {
        validators: [Validators.required],
      }),
      note: new FormControl(memberRawValue.note),
      createdAt: new FormControl(memberRawValue.createdAt),
    });
  }

  getMember(form: MemberFormGroup): IMember | NewMember {
    return this.convertMemberRawValueToMember(form.getRawValue() as MemberFormRawValue | NewMemberFormRawValue);
  }

  resetForm(form: MemberFormGroup, member: MemberFormGroupInput): void {
    const memberRawValue = this.convertMemberToMemberRawValue({ ...this.getFormDefaults(), ...member });
    form.reset(
      {
        ...memberRawValue,
        id: { value: memberRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): MemberFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
    };
  }

  private convertMemberRawValueToMember(rawMember: MemberFormRawValue | NewMemberFormRawValue): IMember | NewMember {
    return {
      ...rawMember,
      createdAt: dayjs(rawMember.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertMemberToMemberRawValue(
    member: IMember | (Partial<NewMember> & MemberFormDefaults),
  ): MemberFormRawValue | PartialWithRequiredKeyOf<NewMemberFormRawValue> {
    return {
      ...member,
      createdAt: member.createdAt ? member.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
