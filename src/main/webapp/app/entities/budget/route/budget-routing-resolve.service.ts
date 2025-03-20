import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IBudget } from '../budget.model';
import { BudgetService } from '../service/budget.service';

const budgetResolve = (route: ActivatedRouteSnapshot): Observable<null | IBudget> => {
  const id = route.params.id;
  if (id) {
    return inject(BudgetService)
      .find(id)
      .pipe(
        mergeMap((budget: HttpResponse<IBudget>) => {
          if (budget.body) {
            return of(budget.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default budgetResolve;
