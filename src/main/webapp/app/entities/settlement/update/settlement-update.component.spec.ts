import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IEvent } from 'app/entities/event/event.model';
import { EventService } from 'app/entities/event/service/event.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { ISettlement } from '../settlement.model';
import { SettlementService } from '../service/settlement.service';
import { SettlementFormService } from './settlement-form.service';

import { SettlementUpdateComponent } from './settlement-update.component';

describe('Settlement Management Update Component', () => {
  let comp: SettlementUpdateComponent;
  let fixture: ComponentFixture<SettlementUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let settlementFormService: SettlementFormService;
  let settlementService: SettlementService;
  let eventService: EventService;
  let memberService: MemberService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SettlementUpdateComponent],
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
      .overrideTemplate(SettlementUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(SettlementUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    settlementFormService = TestBed.inject(SettlementFormService);
    settlementService = TestBed.inject(SettlementService);
    eventService = TestBed.inject(EventService);
    memberService = TestBed.inject(MemberService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Event query and add missing value', () => {
      const settlement: ISettlement = { id: 13938 };
      const event: IEvent = { id: 22576 };
      settlement.event = event;

      const eventCollection: IEvent[] = [{ id: 22576 }];
      jest.spyOn(eventService, 'query').mockReturnValue(of(new HttpResponse({ body: eventCollection })));
      const additionalEvents = [event];
      const expectedCollection: IEvent[] = [...additionalEvents, ...eventCollection];
      jest.spyOn(eventService, 'addEventToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ settlement });
      comp.ngOnInit();

      expect(eventService.query).toHaveBeenCalled();
      expect(eventService.addEventToCollectionIfMissing).toHaveBeenCalledWith(
        eventCollection,
        ...additionalEvents.map(expect.objectContaining),
      );
      expect(comp.eventsSharedCollection).toEqual(expectedCollection);
    });

    it('should call Member query and add missing value', () => {
      const settlement: ISettlement = { id: 13938 };
      const fromMember: IMember = { id: 17514 };
      settlement.fromMember = fromMember;
      const toMember: IMember = { id: 17514 };
      settlement.toMember = toMember;

      const memberCollection: IMember[] = [{ id: 17514 }];
      jest.spyOn(memberService, 'query').mockReturnValue(of(new HttpResponse({ body: memberCollection })));
      const additionalMembers = [fromMember, toMember];
      const expectedCollection: IMember[] = [...additionalMembers, ...memberCollection];
      jest.spyOn(memberService, 'addMemberToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ settlement });
      comp.ngOnInit();

      expect(memberService.query).toHaveBeenCalled();
      expect(memberService.addMemberToCollectionIfMissing).toHaveBeenCalledWith(
        memberCollection,
        ...additionalMembers.map(expect.objectContaining),
      );
      expect(comp.membersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const settlement: ISettlement = { id: 13938 };
      const event: IEvent = { id: 22576 };
      settlement.event = event;
      const fromMember: IMember = { id: 17514 };
      settlement.fromMember = fromMember;
      const toMember: IMember = { id: 17514 };
      settlement.toMember = toMember;

      activatedRoute.data = of({ settlement });
      comp.ngOnInit();

      expect(comp.eventsSharedCollection).toContainEqual(event);
      expect(comp.membersSharedCollection).toContainEqual(fromMember);
      expect(comp.membersSharedCollection).toContainEqual(toMember);
      expect(comp.settlement).toEqual(settlement);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISettlement>>();
      const settlement = { id: 4078 };
      jest.spyOn(settlementFormService, 'getSettlement').mockReturnValue(settlement);
      jest.spyOn(settlementService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ settlement });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: settlement }));
      saveSubject.complete();

      // THEN
      expect(settlementFormService.getSettlement).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(settlementService.update).toHaveBeenCalledWith(expect.objectContaining(settlement));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISettlement>>();
      const settlement = { id: 4078 };
      jest.spyOn(settlementFormService, 'getSettlement').mockReturnValue({ id: null });
      jest.spyOn(settlementService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ settlement: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: settlement }));
      saveSubject.complete();

      // THEN
      expect(settlementFormService.getSettlement).toHaveBeenCalled();
      expect(settlementService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISettlement>>();
      const settlement = { id: 4078 };
      jest.spyOn(settlementService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ settlement });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(settlementService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareEvent', () => {
      it('should forward to eventService', () => {
        const entity = { id: 22576 };
        const entity2 = { id: 3268 };
        jest.spyOn(eventService, 'compareEvent');
        comp.compareEvent(entity, entity2);
        expect(eventService.compareEvent).toHaveBeenCalledWith(entity, entity2);
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
