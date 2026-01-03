import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IBill } from 'app/entities/bill/bill.model';
import { BillService } from 'app/entities/bill/service/bill.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { IBillParticipant } from '../bill-participant.model';
import { BillParticipantService } from '../service/bill-participant.service';
import { BillParticipantFormService } from './bill-participant-form.service';

import { BillParticipantUpdateComponent } from './bill-participant-update.component';

describe('BillParticipant Management Update Component', () => {
  let comp: BillParticipantUpdateComponent;
  let fixture: ComponentFixture<BillParticipantUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let billParticipantFormService: BillParticipantFormService;
  let billParticipantService: BillParticipantService;
  let billService: BillService;
  let memberService: MemberService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BillParticipantUpdateComponent],
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
      .overrideTemplate(BillParticipantUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(BillParticipantUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    billParticipantFormService = TestBed.inject(BillParticipantFormService);
    billParticipantService = TestBed.inject(BillParticipantService);
    billService = TestBed.inject(BillService);
    memberService = TestBed.inject(MemberService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Bill query and add missing value', () => {
      const billParticipant: IBillParticipant = { id: 937 };
      const bill: IBill = { id: 8530 };
      billParticipant.bill = bill;

      const billCollection: IBill[] = [{ id: 8530 }];
      jest.spyOn(billService, 'query').mockReturnValue(of(new HttpResponse({ body: billCollection })));
      const additionalBills = [bill];
      const expectedCollection: IBill[] = [...additionalBills, ...billCollection];
      jest.spyOn(billService, 'addBillToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ billParticipant });
      comp.ngOnInit();

      expect(billService.query).toHaveBeenCalled();
      expect(billService.addBillToCollectionIfMissing).toHaveBeenCalledWith(
        billCollection,
        ...additionalBills.map(expect.objectContaining),
      );
      expect(comp.billsSharedCollection).toEqual(expectedCollection);
    });

    it('should call Member query and add missing value', () => {
      const billParticipant: IBillParticipant = { id: 937 };
      const member: IMember = { id: 17514 };
      billParticipant.member = member;

      const memberCollection: IMember[] = [{ id: 17514 }];
      jest.spyOn(memberService, 'query').mockReturnValue(of(new HttpResponse({ body: memberCollection })));
      const additionalMembers = [member];
      const expectedCollection: IMember[] = [...additionalMembers, ...memberCollection];
      jest.spyOn(memberService, 'addMemberToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ billParticipant });
      comp.ngOnInit();

      expect(memberService.query).toHaveBeenCalled();
      expect(memberService.addMemberToCollectionIfMissing).toHaveBeenCalledWith(
        memberCollection,
        ...additionalMembers.map(expect.objectContaining),
      );
      expect(comp.membersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const billParticipant: IBillParticipant = { id: 937 };
      const bill: IBill = { id: 8530 };
      billParticipant.bill = bill;
      const member: IMember = { id: 17514 };
      billParticipant.member = member;

      activatedRoute.data = of({ billParticipant });
      comp.ngOnInit();

      expect(comp.billsSharedCollection).toContainEqual(bill);
      expect(comp.membersSharedCollection).toContainEqual(member);
      expect(comp.billParticipant).toEqual(billParticipant);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBillParticipant>>();
      const billParticipant = { id: 5254 };
      jest.spyOn(billParticipantFormService, 'getBillParticipant').mockReturnValue(billParticipant);
      jest.spyOn(billParticipantService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ billParticipant });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: billParticipant }));
      saveSubject.complete();

      // THEN
      expect(billParticipantFormService.getBillParticipant).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(billParticipantService.update).toHaveBeenCalledWith(expect.objectContaining(billParticipant));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBillParticipant>>();
      const billParticipant = { id: 5254 };
      jest.spyOn(billParticipantFormService, 'getBillParticipant').mockReturnValue({ id: null });
      jest.spyOn(billParticipantService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ billParticipant: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: billParticipant }));
      saveSubject.complete();

      // THEN
      expect(billParticipantFormService.getBillParticipant).toHaveBeenCalled();
      expect(billParticipantService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBillParticipant>>();
      const billParticipant = { id: 5254 };
      jest.spyOn(billParticipantService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ billParticipant });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(billParticipantService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareBill', () => {
      it('should forward to billService', () => {
        const entity = { id: 8530 };
        const entity2 = { id: 14455 };
        jest.spyOn(billService, 'compareBill');
        comp.compareBill(entity, entity2);
        expect(billService.compareBill).toHaveBeenCalledWith(entity, entity2);
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
