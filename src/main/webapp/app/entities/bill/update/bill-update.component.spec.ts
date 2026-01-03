import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IEvent } from 'app/entities/event/event.model';
import { EventService } from 'app/entities/event/service/event.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { IBill } from '../bill.model';
import { BillService } from '../service/bill.service';
import { BillFormService } from './bill-form.service';

import { BillUpdateComponent } from './bill-update.component';

describe('Bill Management Update Component', () => {
  let comp: BillUpdateComponent;
  let fixture: ComponentFixture<BillUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let billFormService: BillFormService;
  let billService: BillService;
  let eventService: EventService;
  let memberService: MemberService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BillUpdateComponent],
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
      .overrideTemplate(BillUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(BillUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    billFormService = TestBed.inject(BillFormService);
    billService = TestBed.inject(BillService);
    eventService = TestBed.inject(EventService);
    memberService = TestBed.inject(MemberService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Event query and add missing value', () => {
      const bill: IBill = { id: 14455 };
      const event: IEvent = { id: 22576 };
      bill.event = event;

      const eventCollection: IEvent[] = [{ id: 22576 }];
      jest.spyOn(eventService, 'query').mockReturnValue(of(new HttpResponse({ body: eventCollection })));
      const additionalEvents = [event];
      const expectedCollection: IEvent[] = [...additionalEvents, ...eventCollection];
      jest.spyOn(eventService, 'addEventToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ bill });
      comp.ngOnInit();

      expect(eventService.query).toHaveBeenCalled();
      expect(eventService.addEventToCollectionIfMissing).toHaveBeenCalledWith(
        eventCollection,
        ...additionalEvents.map(expect.objectContaining),
      );
      expect(comp.eventsSharedCollection).toEqual(expectedCollection);
    });

    it('should call Member query and add missing value', () => {
      const bill: IBill = { id: 14455 };
      const payer: IMember = { id: 17514 };
      bill.payer = payer;

      const memberCollection: IMember[] = [{ id: 17514 }];
      jest.spyOn(memberService, 'query').mockReturnValue(of(new HttpResponse({ body: memberCollection })));
      const additionalMembers = [payer];
      const expectedCollection: IMember[] = [...additionalMembers, ...memberCollection];
      jest.spyOn(memberService, 'addMemberToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ bill });
      comp.ngOnInit();

      expect(memberService.query).toHaveBeenCalled();
      expect(memberService.addMemberToCollectionIfMissing).toHaveBeenCalledWith(
        memberCollection,
        ...additionalMembers.map(expect.objectContaining),
      );
      expect(comp.membersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const bill: IBill = { id: 14455 };
      const event: IEvent = { id: 22576 };
      bill.event = event;
      const payer: IMember = { id: 17514 };
      bill.payer = payer;

      activatedRoute.data = of({ bill });
      comp.ngOnInit();

      expect(comp.eventsSharedCollection).toContainEqual(event);
      expect(comp.membersSharedCollection).toContainEqual(payer);
      expect(comp.bill).toEqual(bill);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBill>>();
      const bill = { id: 8530 };
      jest.spyOn(billFormService, 'getBill').mockReturnValue(bill);
      jest.spyOn(billService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ bill });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: bill }));
      saveSubject.complete();

      // THEN
      expect(billFormService.getBill).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(billService.update).toHaveBeenCalledWith(expect.objectContaining(bill));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBill>>();
      const bill = { id: 8530 };
      jest.spyOn(billFormService, 'getBill').mockReturnValue({ id: null });
      jest.spyOn(billService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ bill: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: bill }));
      saveSubject.complete();

      // THEN
      expect(billFormService.getBill).toHaveBeenCalled();
      expect(billService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBill>>();
      const bill = { id: 8530 };
      jest.spyOn(billService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ bill });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(billService.update).toHaveBeenCalled();
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
