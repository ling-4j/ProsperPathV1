import { Component, input, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { ITeam } from '../team.model';

import { inject } from '@angular/core';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import { MemberService } from 'app/entities/member/service/member.service';
import { ITeamMember, NewTeamMember } from 'app/entities/team-member/team-member.model';
import { IMember } from 'app/entities/member/member.model';
import dayjs from 'dayjs/esm';

@Component({
  selector: 'jhi-team-detail',
  templateUrl: './team-detail.component.html',
  styleUrls: ['./team-detail.component.scss'],
  imports: [SharedModule, RouterModule],
})
export class TeamDetailComponent implements OnInit {
  private teamMemberService = inject(TeamMemberService);
  private memberService = inject(MemberService);
  teamMembers: ITeamMember[] = [];
  allMembers: IMember[] = [];
  isLoading = false;
  ngOnInit(): void {
    if (this.team()?.id) {
      this.loadTeamMembers();
      this.loadAllMembers();
    }
  }

  loadTeamMembers(): void {
    const teamId = this.team()!.id;
    this.teamMemberService.query({ 'teamId.equals': teamId }).subscribe(res => {
      this.teamMembers = res.body ?? [];
    });
  }
  loadAllMembers(): void {
    this.memberService.query().subscribe(res => {
      this.allMembers = res.body ?? [];
    });
  }

  addMember(member: IMember): void {
    const teamId = this.team()!.id;

    const teamMember: NewTeamMember = {
      id: null,
      joinedAt: dayjs(),
      team: { id: teamId },
      member: { id: member.id },
    };

    this.teamMemberService.create(teamMember).subscribe(() => {
      this.loadTeamMembers();
    });
  }
  get availableMembers(): IMember[] {
    const usedIds = this.teamMembers.map(tm => tm.member?.id);
    return this.allMembers.filter(m => !usedIds.includes(m.id));
  }
  removeMember(teamMember: ITeamMember): void {
    this.teamMemberService.delete(teamMember.id).subscribe(() => {
      this.loadTeamMembers();
    });
  }

  team = input<ITeam | null>(null);

  previousState(): void {
    window.history.back();
  }
}
