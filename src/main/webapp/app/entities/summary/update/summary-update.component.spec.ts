import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { SummaryService } from '../service/summary.service';
import { ISummary } from '../summary.model';
import { SummaryFormService } from './summary-form.service';

import { SummaryUpdateComponent } from './summary-update.component';

describe('Summary Management Update Component', () => {
  let comp: SummaryUpdateComponent;
  let fixture: ComponentFixture<SummaryUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let summaryFormService: SummaryFormService;
  let summaryService: SummaryService;
  let userService: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SummaryUpdateComponent],
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
      .overrideTemplate(SummaryUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(SummaryUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    summaryFormService = TestBed.inject(SummaryFormService);
    summaryService = TestBed.inject(SummaryService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call User query and add missing value', () => {
      const summary: ISummary = { id: 17423 };
      const user: IUser = { id: 3944 };
      summary.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ summary });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const summary: ISummary = { id: 17423 };
      const user: IUser = { id: 3944 };
      summary.user = user;

      activatedRoute.data = of({ summary });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContainEqual(user);
      expect(comp.summary).toEqual(summary);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISummary>>();
      const summary = { id: 9032 };
      jest.spyOn(summaryFormService, 'getSummary').mockReturnValue(summary);
      jest.spyOn(summaryService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ summary });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: summary }));
      saveSubject.complete();

      // THEN
      expect(summaryFormService.getSummary).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(summaryService.update).toHaveBeenCalledWith(expect.objectContaining(summary));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISummary>>();
      const summary = { id: 9032 };
      jest.spyOn(summaryFormService, 'getSummary').mockReturnValue({ id: null });
      jest.spyOn(summaryService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ summary: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: summary }));
      saveSubject.complete();

      // THEN
      expect(summaryFormService.getSummary).toHaveBeenCalled();
      expect(summaryService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISummary>>();
      const summary = { id: 9032 };
      jest.spyOn(summaryService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ summary });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(summaryService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('Should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
