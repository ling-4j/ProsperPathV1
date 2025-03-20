import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ICategory } from 'app/entities/category/category.model';
import { CategoryService } from 'app/entities/category/service/category.service';
import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IBudget } from '../budget.model';
import { BudgetService } from '../service/budget.service';
import { BudgetFormService } from './budget-form.service';

import { BudgetUpdateComponent } from './budget-update.component';

describe('Budget Management Update Component', () => {
  let comp: BudgetUpdateComponent;
  let fixture: ComponentFixture<BudgetUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let budgetFormService: BudgetFormService;
  let budgetService: BudgetService;
  let categoryService: CategoryService;
  let userService: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BudgetUpdateComponent],
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
      .overrideTemplate(BudgetUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(BudgetUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    budgetFormService = TestBed.inject(BudgetFormService);
    budgetService = TestBed.inject(BudgetService);
    categoryService = TestBed.inject(CategoryService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Category query and add missing value', () => {
      const budget: IBudget = { id: 31697 };
      const category: ICategory = { id: 6752 };
      budget.category = category;

      const categoryCollection: ICategory[] = [{ id: 6752 }];
      jest.spyOn(categoryService, 'query').mockReturnValue(of(new HttpResponse({ body: categoryCollection })));
      const additionalCategories = [category];
      const expectedCollection: ICategory[] = [...additionalCategories, ...categoryCollection];
      jest.spyOn(categoryService, 'addCategoryToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ budget });
      comp.ngOnInit();

      expect(categoryService.query).toHaveBeenCalled();
      expect(categoryService.addCategoryToCollectionIfMissing).toHaveBeenCalledWith(
        categoryCollection,
        ...additionalCategories.map(expect.objectContaining),
      );
      expect(comp.categoriesSharedCollection).toEqual(expectedCollection);
    });

    it('Should call User query and add missing value', () => {
      const budget: IBudget = { id: 31697 };
      const user: IUser = { id: 3944 };
      budget.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ budget });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const budget: IBudget = { id: 31697 };
      const category: ICategory = { id: 6752 };
      budget.category = category;
      const user: IUser = { id: 3944 };
      budget.user = user;

      activatedRoute.data = of({ budget });
      comp.ngOnInit();

      expect(comp.categoriesSharedCollection).toContainEqual(category);
      expect(comp.usersSharedCollection).toContainEqual(user);
      expect(comp.budget).toEqual(budget);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBudget>>();
      const budget = { id: 5320 };
      jest.spyOn(budgetFormService, 'getBudget').mockReturnValue(budget);
      jest.spyOn(budgetService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ budget });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: budget }));
      saveSubject.complete();

      // THEN
      expect(budgetFormService.getBudget).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(budgetService.update).toHaveBeenCalledWith(expect.objectContaining(budget));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBudget>>();
      const budget = { id: 5320 };
      jest.spyOn(budgetFormService, 'getBudget').mockReturnValue({ id: null });
      jest.spyOn(budgetService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ budget: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: budget }));
      saveSubject.complete();

      // THEN
      expect(budgetFormService.getBudget).toHaveBeenCalled();
      expect(budgetService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IBudget>>();
      const budget = { id: 5320 };
      jest.spyOn(budgetService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ budget });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(budgetService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareCategory', () => {
      it('Should forward to categoryService', () => {
        const entity = { id: 6752 };
        const entity2 = { id: 4374 };
        jest.spyOn(categoryService, 'compareCategory');
        comp.compareCategory(entity, entity2);
        expect(categoryService.compareCategory).toHaveBeenCalledWith(entity, entity2);
      });
    });

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
