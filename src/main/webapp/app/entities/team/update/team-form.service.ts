import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ITeam, NewTeam } from '../team.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITeam for edit and NewTeamFormGroupInput for create.
 */
type TeamFormGroupInput = ITeam | PartialWithRequiredKeyOf<NewTeam>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ITeam | NewTeam> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type TeamFormRawValue = FormValueOf<ITeam>;

type NewTeamFormRawValue = FormValueOf<NewTeam>;

type TeamFormDefaults = Pick<NewTeam, 'id' | 'createdAt'>;

type TeamFormGroupContent = {
  id: FormControl<TeamFormRawValue['id'] | NewTeam['id']>;
  name: FormControl<TeamFormRawValue['name']>;
  createdAt: FormControl<TeamFormRawValue['createdAt']>;
};

export type TeamFormGroup = FormGroup<TeamFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TeamFormService {
  createTeamFormGroup(team: TeamFormGroupInput = { id: null }): TeamFormGroup {
    const teamRawValue = this.convertTeamToTeamRawValue({
      ...this.getFormDefaults(),
      ...team,
    });
    return new FormGroup<TeamFormGroupContent>({
      id: new FormControl(
        { value: teamRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(teamRawValue.name, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(teamRawValue.createdAt),
    });
  }

  getTeam(form: TeamFormGroup): ITeam | NewTeam {
    return this.convertTeamRawValueToTeam(form.getRawValue() as TeamFormRawValue | NewTeamFormRawValue);
  }

  resetForm(form: TeamFormGroup, team: TeamFormGroupInput): void {
    const teamRawValue = this.convertTeamToTeamRawValue({ ...this.getFormDefaults(), ...team });
    form.reset(
      {
        ...teamRawValue,
        id: { value: teamRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): TeamFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
    };
  }

  private convertTeamRawValueToTeam(rawTeam: TeamFormRawValue | NewTeamFormRawValue): ITeam | NewTeam {
    return {
      ...rawTeam,
      createdAt: dayjs(rawTeam.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertTeamToTeamRawValue(
    team: ITeam | (Partial<NewTeam> & TeamFormDefaults),
  ): TeamFormRawValue | PartialWithRequiredKeyOf<NewTeamFormRawValue> {
    return {
      ...team,
      createdAt: team.createdAt ? team.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
