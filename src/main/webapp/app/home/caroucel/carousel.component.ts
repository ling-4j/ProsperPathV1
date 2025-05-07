import { Component, AfterViewInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import TranslateDirective from '../../shared/language/translate.directive';

@Component({
  selector: 'jhi-carousel',
  standalone: true,
  templateUrl: './carousel.component.html',
  styleUrls: ['./carousel.component.scss'],
  imports: [RouterModule, TranslateDirective],
})
export default class CarouselComponent implements AfterViewInit {
  ngAfterViewInit(): void {
    const carouselElement = document.getElementById('prosperPathCarousel');
    if (carouselElement && typeof (window as any).bootstrap !== 'undefined') {
      // eslint-disable-next-line no-new
      new (window as any).bootstrap.Carousel(carouselElement);
    }
  }
}
