import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ITeam } from 'app/entities/team/team.model';
import { TeamService } from 'app/entities/team/service/team.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { TeamMemberService } from '../service/team-member.service';
import { ITeamMember } from '../team-member.model';
import { TeamMemberFormGroup, TeamMemberFormService } from './team-member-form.service';

@Component({
  selector: 'jhi-team-member-update',
  templateUrl: './team-member-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class TeamMemberUpdateComponent implements OnInit {
  isSaving = false;
  teamMember: ITeamMember | null = null;

  teamsSharedCollection: ITeam[] = [];
  membersSharedCollection: IMember[] = [];

  protected teamMemberService = inject(TeamMemberService);
  protected teamMemberFormService = inject(TeamMemberFormService);
  protected teamService = inject(TeamService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: TeamMemberFormGroup = this.teamMemberFormService.createTeamMemberFormGroup();

  compareTeam = (o1: ITeam | null, o2: ITeam | null): boolean => this.teamService.compareTeam(o1, o2);

  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ teamMember }) => {
      this.teamMember = teamMember;
      if (teamMember) {
        this.updateForm(teamMember);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const teamMember = this.teamMemberFormService.getTeamMember(this.editForm);
    if (teamMember.id !== null) {
      this.subscribeToSaveResponse(this.teamMemberService.update(teamMember));
    } else {
      this.subscribeToSaveResponse(this.teamMemberService.create(teamMember));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ITeamMember>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(teamMember: ITeamMember): void {
    this.teamMember = teamMember;
    this.teamMemberFormService.resetForm(this.editForm, teamMember);

    this.teamsSharedCollection = this.teamService.addTeamToCollectionIfMissing<ITeam>(this.teamsSharedCollection, teamMember.team);
    this.membersSharedCollection = this.memberService.addMemberToCollectionIfMissing<IMember>(
      this.membersSharedCollection,
      teamMember.member,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.teamService
      .query()
      .pipe(map((res: HttpResponse<ITeam[]>) => res.body ?? []))
      .pipe(map((teams: ITeam[]) => this.teamService.addTeamToCollectionIfMissing<ITeam>(teams, this.teamMember?.team)))
      .subscribe((teams: ITeam[]) => (this.teamsSharedCollection = teams));

    this.memberService
      .query()
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(map((members: IMember[]) => this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.teamMember?.member)))
      .subscribe((members: IMember[]) => (this.membersSharedCollection = members));
  }
}
