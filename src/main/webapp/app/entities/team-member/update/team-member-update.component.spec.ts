import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ITeam } from 'app/entities/team/team.model';
import { TeamService } from 'app/entities/team/service/team.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { ITeamMember } from '../team-member.model';
import { TeamMemberService } from '../service/team-member.service';
import { TeamMemberFormService } from './team-member-form.service';

import { TeamMemberUpdateComponent } from './team-member-update.component';

describe('TeamMember Management Update Component', () => {
  let comp: TeamMemberUpdateComponent;
  let fixture: ComponentFixture<TeamMemberUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let teamMemberFormService: TeamMemberFormService;
  let teamMemberService: TeamMemberService;
  let teamService: TeamService;
  let memberService: MemberService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TeamMemberUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(TeamMemberUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(TeamMemberUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    teamMemberFormService = TestBed.inject(TeamMemberFormService);
    teamMemberService = TestBed.inject(TeamMemberService);
    teamService = TestBed.inject(TeamService);
    memberService = TestBed.inject(MemberService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Team query and add missing value', () => {
      const teamMember: ITeamMember = { id: 22246 };
      const team: ITeam = { id: 1226 };
      teamMember.team = team;

      const teamCollection: ITeam[] = [{ id: 1226 }];
      jest.spyOn(teamService, 'query').mockReturnValue(of(new HttpResponse({ body: teamCollection })));
      const additionalTeams = [team];
      const expectedCollection: ITeam[] = [...additionalTeams, ...teamCollection];
      jest.spyOn(teamService, 'addTeamToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      expect(teamService.query).toHaveBeenCalled();
      expect(teamService.addTeamToCollectionIfMissing).toHaveBeenCalledWith(
        teamCollection,
        ...additionalTeams.map(expect.objectContaining),
      );
      expect(comp.teamsSharedCollection).toEqual(expectedCollection);
    });

    it('should call Member query and add missing value', () => {
      const teamMember: ITeamMember = { id: 22246 };
      const member: IMember = { id: 17514 };
      teamMember.member = member;

      const memberCollection: IMember[] = [{ id: 17514 }];
      jest.spyOn(memberService, 'query').mockReturnValue(of(new HttpResponse({ body: memberCollection })));
      const additionalMembers = [member];
      const expectedCollection: IMember[] = [...additionalMembers, ...memberCollection];
      jest.spyOn(memberService, 'addMemberToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      expect(memberService.query).toHaveBeenCalled();
      expect(memberService.addMemberToCollectionIfMissing).toHaveBeenCalledWith(
        memberCollection,
        ...additionalMembers.map(expect.objectContaining),
      );
      expect(comp.membersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const teamMember: ITeamMember = { id: 22246 };
      const team: ITeam = { id: 1226 };
      teamMember.team = team;
      const member: IMember = { id: 17514 };
      teamMember.member = member;

      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      expect(comp.teamsSharedCollection).toContainEqual(team);
      expect(comp.membersSharedCollection).toContainEqual(member);
      expect(comp.teamMember).toEqual(teamMember);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITeamMember>>();
      const teamMember = { id: 11287 };
      jest.spyOn(teamMemberFormService, 'getTeamMember').mockReturnValue(teamMember);
      jest.spyOn(teamMemberService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: teamMember }));
      saveSubject.complete();

      // THEN
      expect(teamMemberFormService.getTeamMember).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(teamMemberService.update).toHaveBeenCalledWith(expect.objectContaining(teamMember));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITeamMember>>();
      const teamMember = { id: 11287 };
      jest.spyOn(teamMemberFormService, 'getTeamMember').mockReturnValue({ id: null });
      jest.spyOn(teamMemberService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ teamMember: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: teamMember }));
      saveSubject.complete();

      // THEN
      expect(teamMemberFormService.getTeamMember).toHaveBeenCalled();
      expect(teamMemberService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITeamMember>>();
      const teamMember = { id: 11287 };
      jest.spyOn(teamMemberService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(teamMemberService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareTeam', () => {
      it('should forward to teamService', () => {
        const entity = { id: 1226 };
        const entity2 = { id: 14592 };
        jest.spyOn(teamService, 'compareTeam');
        comp.compareTeam(entity, entity2);
        expect(teamService.compareTeam).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareMember', () => {
      it('should forward to memberService', () => {
        const entity = { id: 17514 };
        const entity2 = { id: 30790 };
        jest.spyOn(memberService, 'compareMember');
        comp.compareMember(entity, entity2);
        expect(memberService.compareMember).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
