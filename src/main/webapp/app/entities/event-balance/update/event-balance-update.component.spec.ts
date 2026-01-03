import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IEvent } from 'app/entities/event/event.model';
import { EventService } from 'app/entities/event/service/event.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { IEventBalance } from '../event-balance.model';
import { EventBalanceService } from '../service/event-balance.service';
import { EventBalanceFormService } from './event-balance-form.service';

import { EventBalanceUpdateComponent } from './event-balance-update.component';

describe('EventBalance Management Update Component', () => {
  let comp: EventBalanceUpdateComponent;
  let fixture: ComponentFixture<EventBalanceUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let eventBalanceFormService: EventBalanceFormService;
  let eventBalanceService: EventBalanceService;
  let eventService: EventService;
  let memberService: MemberService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EventBalanceUpdateComponent],
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
      .overrideTemplate(EventBalanceUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(EventBalanceUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    eventBalanceFormService = TestBed.inject(EventBalanceFormService);
    eventBalanceService = TestBed.inject(EventBalanceService);
    eventService = TestBed.inject(EventService);
    memberService = TestBed.inject(MemberService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Event query and add missing value', () => {
      const eventBalance: IEventBalance = { id: 3958 };
      const event: IEvent = { id: 22576 };
      eventBalance.event = event;

      const eventCollection: IEvent[] = [{ id: 22576 }];
      jest.spyOn(eventService, 'query').mockReturnValue(of(new HttpResponse({ body: eventCollection })));
      const additionalEvents = [event];
      const expectedCollection: IEvent[] = [...additionalEvents, ...eventCollection];
      jest.spyOn(eventService, 'addEventToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ eventBalance });
      comp.ngOnInit();

      expect(eventService.query).toHaveBeenCalled();
      expect(eventService.addEventToCollectionIfMissing).toHaveBeenCalledWith(
        eventCollection,
        ...additionalEvents.map(expect.objectContaining),
      );
      expect(comp.eventsSharedCollection).toEqual(expectedCollection);
    });

    it('should call Member query and add missing value', () => {
      const eventBalance: IEventBalance = { id: 3958 };
      const member: IMember = { id: 17514 };
      eventBalance.member = member;

      const memberCollection: IMember[] = [{ id: 17514 }];
      jest.spyOn(memberService, 'query').mockReturnValue(of(new HttpResponse({ body: memberCollection })));
      const additionalMembers = [member];
      const expectedCollection: IMember[] = [...additionalMembers, ...memberCollection];
      jest.spyOn(memberService, 'addMemberToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ eventBalance });
      comp.ngOnInit();

      expect(memberService.query).toHaveBeenCalled();
      expect(memberService.addMemberToCollectionIfMissing).toHaveBeenCalledWith(
        memberCollection,
        ...additionalMembers.map(expect.objectContaining),
      );
      expect(comp.membersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const eventBalance: IEventBalance = { id: 3958 };
      const event: IEvent = { id: 22576 };
      eventBalance.event = event;
      const member: IMember = { id: 17514 };
      eventBalance.member = member;

      activatedRoute.data = of({ eventBalance });
      comp.ngOnInit();

      expect(comp.eventsSharedCollection).toContainEqual(event);
      expect(comp.membersSharedCollection).toContainEqual(member);
      expect(comp.eventBalance).toEqual(eventBalance);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IEventBalance>>();
      const eventBalance = { id: 26921 };
      jest.spyOn(eventBalanceFormService, 'getEventBalance').mockReturnValue(eventBalance);
      jest.spyOn(eventBalanceService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ eventBalance });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: eventBalance }));
      saveSubject.complete();

      // THEN
      expect(eventBalanceFormService.getEventBalance).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(eventBalanceService.update).toHaveBeenCalledWith(expect.objectContaining(eventBalance));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IEventBalance>>();
      const eventBalance = { id: 26921 };
      jest.spyOn(eventBalanceFormService, 'getEventBalance').mockReturnValue({ id: null });
      jest.spyOn(eventBalanceService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ eventBalance: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: eventBalance }));
      saveSubject.complete();

      // THEN
      expect(eventBalanceFormService.getEventBalance).toHaveBeenCalled();
      expect(eventBalanceService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IEventBalance>>();
      const eventBalance = { id: 26921 };
      jest.spyOn(eventBalanceService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ eventBalance });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(eventBalanceService.update).toHaveBeenCalled();
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
