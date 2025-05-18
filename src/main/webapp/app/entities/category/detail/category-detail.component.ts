import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ICategory } from '../category.model';

@Component({
  selector: 'jhi-category-detail',
  styleUrls: ['./category-detail.component.scss'],
  templateUrl: './category-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class CategoryDetailComponent {
  category = input<ICategory | null>(null);

  previousState(): void {
    window.history.back();
  }
}
