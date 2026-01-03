import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ITeamMember, NewTeamMember } from '../team-member.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITeamMember for edit and NewTeamMemberFormGroupInput for create.
 */
type TeamMemberFormGroupInput = ITeamMember | PartialWithRequiredKeyOf<NewTeamMember>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ITeamMember | NewTeamMember> = Omit<T, 'joinedAt'> & {
  joinedAt?: string | null;
};

type TeamMemberFormRawValue = FormValueOf<ITeamMember>;

type NewTeamMemberFormRawValue = FormValueOf<NewTeamMember>;

type TeamMemberFormDefaults = Pick<NewTeamMember, 'id' | 'joinedAt'>;

type TeamMemberFormGroupContent = {
  id: FormControl<TeamMemberFormRawValue['id'] | NewTeamMember['id']>;
  joinedAt: FormControl<TeamMemberFormRawValue['joinedAt']>;
  team: FormControl<TeamMemberFormRawValue['team']>;
  member: FormControl<TeamMemberFormRawValue['member']>;
};

export type TeamMemberFormGroup = FormGroup<TeamMemberFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TeamMemberFormService {
  createTeamMemberFormGroup(teamMember: TeamMemberFormGroupInput = { id: null }): TeamMemberFormGroup {
    const teamMemberRawValue = this.convertTeamMemberToTeamMemberRawValue({
      ...this.getFormDefaults(),
      ...teamMember,
    });
    return new FormGroup<TeamMemberFormGroupContent>({
      id: new FormControl(
        { value: teamMemberRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      joinedAt: new FormControl(teamMemberRawValue.joinedAt),
      team: new FormControl(teamMemberRawValue.team),
      member: new FormControl(teamMemberRawValue.member),
    });
  }

  getTeamMember(form: TeamMemberFormGroup): ITeamMember | NewTeamMember {
    return this.convertTeamMemberRawValueToTeamMember(form.getRawValue() as TeamMemberFormRawValue | NewTeamMemberFormRawValue);
  }

  resetForm(form: TeamMemberFormGroup, teamMember: TeamMemberFormGroupInput): void {
    const teamMemberRawValue = this.convertTeamMemberToTeamMemberRawValue({ ...this.getFormDefaults(), ...teamMember });
    form.reset(
      {
        ...teamMemberRawValue,
        id: { value: teamMemberRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): TeamMemberFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      joinedAt: currentTime,
    };
  }

  private convertTeamMemberRawValueToTeamMember(
    rawTeamMember: TeamMemberFormRawValue | NewTeamMemberFormRawValue,
  ): ITeamMember | NewTeamMember {
    return {
      ...rawTeamMember,
      joinedAt: dayjs(rawTeamMember.joinedAt, DATE_TIME_FORMAT),
    };
  }

  private convertTeamMemberToTeamMemberRawValue(
    teamMember: ITeamMember | (Partial<NewTeamMember> & TeamMemberFormDefaults),
  ): TeamMemberFormRawValue | PartialWithRequiredKeyOf<NewTeamMemberFormRawValue> {
    return {
      ...teamMember,
      joinedAt: teamMember.joinedAt ? teamMember.joinedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
